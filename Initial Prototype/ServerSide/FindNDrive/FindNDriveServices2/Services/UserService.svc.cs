﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   The user service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Drawing;
    using System.IO;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Web;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Newtonsoft.Json;

    using WebMatrix.WebData;

    /// <summary>
    /// The user service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class UserService : IUserService
    {

        /// <summary>
        /// The unit of work, which provides access to the required Repositories, and exposes
        /// a commit method to complete the unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification Manager.
        /// </param>
        public UserService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// Logs a user in.
        /// </summary>
        /// <param name="login"></param>
        /// <returns></returns>
        public ServiceResponse<User> ManualUserLogin(LoginDTO login)
        {
            var validatedUser = ValidationHelper.Validate(login);
       
            if (!validatedUser.IsValid || !WebSecurity.Login(login.UserName, login.Password))
            {   
                return ServiceResponseBuilder.Failure<User>("Invalid Username or Password.");
            }

            var userId = WebSecurity.GetUserId(login.UserName);
            var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);

            if (loggedInUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User does not exist!");
            }

            // Check if another user has been logged on on the same device before.
            var userToReset =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .FirstOrDefault(
                        _ =>
                        _.GCMRegistrationID == loggedInUser.GCMRegistrationID
                        && !_.UserName.Equals(loggedInUser.UserName));

            // If yes, reset this user's GCM registration ID to 0 to prevent GCM notifications from being sent to the wrong device.
            if (userToReset != null)
            {
                userToReset.GCMRegistrationID = null;
                userToReset.Status = Status.Offline;
            }

            this.sessionManager.GenerateNewSession(loggedInUser.UserId);
            loggedInUser.GCMRegistrationID = login.GCMRegistrationID;
            loggedInUser.Status = Status.Online;
            loggedInUser.LastLogon = DateTime.Now;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(loggedInUser);
        }

        /// <summary>
        /// The auto user login.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> AutoUserLogin()
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Failure<User>("MANUAL LOGIN");
            }

            var userId = this.sessionManager.GetUserId();

            if (userId == -1)
            {
                return ServiceResponseBuilder.Failure<User>("MANUAL LOGIN");
            }

            var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);

            if (loggedInUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User with this id does not exist.");
            }

            loggedInUser.Status = Status.Online;
            loggedInUser.LastLogon = DateTime.Now;
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(loggedInUser);
        }

        /// <summary>
        /// The register user.
        /// </summary>
        /// <param name="register">
        /// The register.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> RegisterUser(RegisterDTO register)
        {
            // Check if an account with the same username already exists.
            if (WebSecurity.UserExists(register.User.UserName))
            {
                return ServiceResponseBuilder.Failure<User>("Account with this username already exists.");
            }

            // Check if an account with the same username already exists.
            if (
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Any(_ => _.EmailAddress.Equals(register.User.EmailAddress)))
            {
                return ServiceResponseBuilder.Failure<User>("Account with this email address already exists.");
            }

            // Prepare the default profile picture.
            var img = Properties.Resources.default_picture;
            byte[] arr;

            using (var ms = new MemoryStream())
            {
                img.Save(ms, System.Drawing.Imaging.ImageFormat.Png);
                arr = ms.ToArray();
            }

            try
            {
                WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
                register.User.UserId = WebSecurity.GetUserId(register.User.UserName);

                var newUser = new User
                {
                    EmailAddress = register.User.EmailAddress,
                    UserName = register.User.UserName,
                    UserId = register.User.UserId,
                    FirstName = string.Empty,
                    LastName = string.Empty,
                    GCMRegistrationID = register.User.GCMRegistrationID,
                    MemberSince = DateTime.Now,
                    AverageRating = 0,
                    LastLogon = DateTime.Now,
                    ProfilePicture = new ProfilePicture
                                         {  
                                             ProfilePictureId = register.User.UserId,
                                             ProfilePictureBytes = arr
                                         }
                };

                // Check if another user has been logged on on the same device before.
                var userToReset =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .FirstOrDefault(
                            _ =>
                            _.GCMRegistrationID == register.User.GCMRegistrationID);

                // If yes, reset this user's GCM registration ID to 0 to prevent GCM notifications from being sent to the wrong device.
                if (userToReset != null)
                {
                    userToReset.GCMRegistrationID = "0";
                    userToReset.Status = Status.Offline;
                }

                this.findNDriveUnitOfWork.UserRepository.Add(newUser);
                this.findNDriveUnitOfWork.Commit();

                this.sessionManager.GenerateNewSession(newUser.UserId);

                return ServiceResponseBuilder.Success(newUser);
            }
            catch (Exception)
            {
                return ServiceResponseBuilder.Failure<User>("Account with this username already exists.");
            }     
        }

        /// <summary>
        /// The logout user.
        /// </summary>
        /// <param name="forceInvalidate">
        /// The force invalidate.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> LogoutUser(bool forceInvalidate)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var success = this.sessionManager.InvalidateSession(forceInvalidate);
            return ServiceResponseBuilder.Success(success);
        }

        /// <summary>
        /// The refresh user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> RefreshUser(int userId)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new User());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == userId);

            return user != null ? ServiceResponseBuilder.Success(user) : ServiceResponseBuilder.Unauthorised(new User());
        }

        public ServiceResponse<User> UpdateUser(UserDTO userDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new User());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                   .IncludeAll()
                   .FirstOrDefault(_ => _.UserId == userDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<User>("Invalid user id.");
            }

            if (!string.IsNullOrWhiteSpace(userDTO.FirstName) && !string.Equals(user.FirstName, userDTO.FirstName))
            {
                user.FirstName = userDTO.FirstName;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.LastName) && !string.Equals(user.LastName, userDTO.LastName))
            {
                user.LastName = userDTO.LastName;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.EmailAddress) && !string.Equals(user.EmailAddress, userDTO.EmailAddress))
            {
                user.EmailAddress = userDTO.EmailAddress;
            }

            if ((userDTO.Gender == Gender.Female || userDTO.Gender == Gender.Male) && userDTO.Gender != user.Gender)
            {
                user.Gender = userDTO.Gender;
            }

            if (userDTO.DateOfBirth != DateTime.MinValue)
            {   
                user.DateOfBirth = userDTO.DateOfBirth;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.PhoneNumber)
                && !string.Equals(user.PhoneNumber, userDTO.PhoneNumber))
            {
                user.PhoneNumber = userDTO.PhoneNumber;
            }
            
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(user);
        }

        public Stream GetUserProfilePicture(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return new MemoryStream(0);
            }

            var user =
                this.findNDriveUnitOfWork.ProfilePicturesRepository.Find(id);

            if (user == null)
            {
                return new MemoryStream(0);
            }

            if (WebOperationContext.Current != null)
            {
                WebOperationContext.Current.OutgoingResponse.ContentType = "image/png";
            }

            return new MemoryStream(user.ProfilePictureBytes);
        }

        public ServiceResponse<bool> UpdateProfilePicture(ProfilePictureUpdaterDTO profilePictureUpdaterDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == profilePictureUpdaterDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<bool>("User with this id does not exist.");
            }

            var imageBytes = Convert.FromBase64String(profilePictureUpdaterDTO.Picture);
            user.ProfilePicture.ProfilePictureBytes = imageBytes;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(true);
        }
    }

}

