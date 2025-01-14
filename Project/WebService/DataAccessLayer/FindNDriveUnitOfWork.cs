﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FindNDriveUnitOfWork.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FindNDriveUnitOfWork type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DataAccessLayer
{
    using System;
    using System.Data.Entity;
    using System.Data.Entity.Validation;
    using System.Diagnostics;

    using DomainObjects.Domains;

    /// <summary>
    /// The find n drive unit of work.
    /// </summary>
    public class FindNDriveUnitOfWork : IUnitOfWork
    {
        /// <summary>
        /// The _db context.
        /// </summary>
        private readonly DbContext dbContext;

        /// <summary>
        /// Gets or sets the user repository.
        /// </summary>
        public IRepository<User> UserRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share repository.
        /// </summary>
        public IRepository<Journey> JourneyRepository { get; set; }

        /// <summary>
        /// Gets or sets the session repository.
        /// </summary>
        public IRepository<Session> SessionRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share request repository.
        /// </summary>
        public IRepository<JourneyRequest> JourneyRequestRepository { get; set; }

        /// <summary>
        /// Gets or sets the chat message repository.
        /// </summary>
        public IRepository<ChatMessage> ChatMessageRepository { get; set; }

        /// <summary>
        /// Gets or sets the notification repository.
        /// </summary>
        public IRepository<Notification> NotificationRepository { get; set; }

        /// <summary>
        /// Gets or sets the friend requests repository.
        /// </summary>
        public IRepository<FriendRequest> FriendRequestsRepository { get; set; }

        /// <summary>
        /// Gets or sets the journey message repository.
        /// </summary>
        public IRepository<JourneyMessage> JourneyMessageRepository { get; set; }

        /// <summary>
        /// Gets or sets the geo address repository.
        /// </summary>
        public IRepository<GeoAddress> GeoAddressRepository { get; set; }

        /// <summary>
        /// Gets or sets the ratings repository.
        /// </summary>
        public IRepository<Rating> RatingsRepository { get; set; }

        /// <summary>
        /// Gets or sets the profile pictures repository.
        /// </summary>
        public IRepository<ProfilePicture> ProfilePicturesRepository { get; set; }

        /// <summary>
        /// Gets or sets the journey template repository.
        /// </summary>
        public IRepository<JourneyTemplate> JourneyTemplateRepository { get; set; }

        /// <summary>
        /// Initializes a new instance of the <see cref="FindNDriveUnitOfWork"/> class.
        /// </summary>
        /// <param name="dbContext">
        /// The db context.
        /// </param>
        /// <param name="userRepository">
        /// The user repository.
        /// </param>
        /// <param name="journeyRepository">
        /// The car share repository.
        /// </param>
        /// <param name="sessionRepository">
        /// The session repository.
        /// </param>
        /// <param name="journeyRequestRepository">
        /// The car Share Request Repository.
        /// </param>
        /// <param name="chatMessageRepository">
        /// The chat Message Repository.
        /// </param>
        /// <param name="notificationRepository">
        /// The notification Repository.
        /// </param>
        /// <param name="friendRequestsRepository">
        /// </param>
        /// <param name="journeyMessageRepository">
        /// The journey Message Repository.
        /// </param>
        /// <param name="geoAddressRepository"></param>
        public FindNDriveUnitOfWork(
            DbContext dbContext,
            IRepository<User> userRepository,
            IRepository<Journey> journeyRepository,
            IRepository<Session> sessionRepository,
            IRepository<JourneyRequest> journeyRequestRepository,
                                    IRepository<ChatMessage> chatMessageRepository,
            IRepository<Notification> notificationRepository,
            IRepository<FriendRequest> friendRequestsRepository,
            IRepository<JourneyMessage> journeyMessageRepository,
            IRepository<GeoAddress> geoAddressRepository,
            IRepository<Rating> ratingsRepository,
            IRepository<ProfilePicture> profilePicturesRepository,
            IRepository<JourneyTemplate> journeyTemplateRepository)
        {
            this.dbContext = dbContext;
            this.UserRepository = userRepository;
            this.JourneyRepository = journeyRepository;
            this.SessionRepository = sessionRepository;
            this.JourneyRequestRepository = journeyRequestRepository;
            this.ChatMessageRepository = chatMessageRepository;
            this.NotificationRepository = notificationRepository;
            this.FriendRequestsRepository = friendRequestsRepository;
            this.JourneyMessageRepository = journeyMessageRepository;
            this.GeoAddressRepository = geoAddressRepository;
            this.RatingsRepository = ratingsRepository;
            this.ProfilePicturesRepository = profilePicturesRepository;
            this.JourneyMessageRepository = journeyMessageRepository;
            this.JourneyTemplateRepository = journeyTemplateRepository;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FindNDriveUnitOfWork"/> class.
        /// </summary>
        public FindNDriveUnitOfWork()
        {
            this.dbContext = new ApplicationContext();
            this.UserRepository = new EntityFrameworkRepository<User>(this.dbContext);
            this.JourneyRepository = new EntityFrameworkRepository<Journey>(this.dbContext);
            this.SessionRepository = new EntityFrameworkRepository<Session>(this.dbContext);
            this.JourneyRequestRepository = new EntityFrameworkRepository<JourneyRequest>(this.dbContext);
            this.ChatMessageRepository = new EntityFrameworkRepository<ChatMessage>(this.dbContext);
            this.NotificationRepository = new EntityFrameworkRepository<Notification>(this.dbContext);
            this.FriendRequestsRepository = new EntityFrameworkRepository<FriendRequest>(this.dbContext);
            this.JourneyMessageRepository = new EntityFrameworkRepository<JourneyMessage>(this.dbContext);
            this.GeoAddressRepository = new EntityFrameworkRepository<GeoAddress>(this.dbContext);
            this.RatingsRepository = new EntityFrameworkRepository<Rating>(this.dbContext);
            this.ProfilePicturesRepository = new EntityFrameworkRepository<ProfilePicture>(this.dbContext);
            this.JourneyTemplateRepository = new EntityFrameworkRepository<JourneyTemplate>(this.dbContext);
        }

        /// <summary>
        /// The commit.
        /// </summary>
        public void Commit()
        {
            try
            {
                this.dbContext.SaveChanges();
            }
            catch (DbEntityValidationException dbEx)
            {
                var file = new System.IO.StreamWriter("c:\\CSC3002FYP\\db_context_error.txt");
                var error = "";
                foreach (var validationErrors in dbEx.EntityValidationErrors)
                {
                    foreach (var validationError in validationErrors.ValidationErrors)
                    {
                        error = error + " " + string.Format("Class: {0}, Property: {1}, Error: {2}", validationErrors.Entry.Entity.GetType().FullName,
                        validationError.PropertyName, validationError.ErrorMessage);
                    }
                    
                    
                    

                }

                file.WriteLine(error);
                file.Close();
            }
            
        }

        /// <summary>
        /// The dispose.
        /// </summary>
        public void Dispose()
        {
            this.dbContext.Dispose();
        }
    }
}