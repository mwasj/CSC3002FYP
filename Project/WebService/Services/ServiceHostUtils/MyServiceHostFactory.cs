﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MyServiceHostFactory.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the MyServiceHostFactory type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.ServiceHostUtils
{
    using System;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Channels;
    using System.ServiceModel.Description;
    using System.ServiceModel.Dispatcher;

    using DataAccessLayer;

    using DomainObjects.Domains;

    using global::Services.ServiceUtils;

    using WebMatrix.WebData;

    /// <summary>
    /// The my service host factory.
    /// </summary>
    public class MyServiceHostFactory : ServiceHostFactory
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="MyServiceHostFactory"/> class.
        /// </summary>
        public MyServiceHostFactory()
        {
            var connectionString = "";

            #if DEBUG
                connectionString = "TestConnectionString";
            #else
                connectionString = "ProductionConnectionString";
            #endif

            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection(connectionString, "User", "Id", "UserName", true);
            }
        }

        /// <summary>
        /// The create service host.
        /// </summary>
        /// <param name="serviceType">
        /// The service type.
        /// </param>
        /// <param name="baseAddresses">
        /// The base addresses.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceHost"/>.
        /// </returns>
        protected override ServiceHost CreateServiceHost(Type serviceType, Uri[] baseAddresses)
        {
            return new MyServiceHost(serviceType, baseAddresses);
        }
    }

    /// <summary>
    /// The my instance provider.
    /// </summary>
    public class MyInstanceProvider : IInstanceProvider, IContractBehavior
    {
        /// <summary>
        /// The _service type.
        /// </summary>
        private readonly Type serviceType;
       

        /// <summary>
        /// Initializes a new instance of the <see cref="MyInstanceProvider"/> class.
        /// </summary>
        /// <param name="serviceType">
        /// The service Type.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// </exception>
        public MyInstanceProvider(Type serviceType)
        {
            this.serviceType = serviceType;
        }

        #region IInstanceProvider Members

        /// <summary>
        /// The get instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        public object GetInstance(InstanceContext instanceContext, Message message)
        {
            return this.GetInstance(instanceContext);
        }

        /// <summary>
        /// The get instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        public object GetInstance(InstanceContext instanceContext)
        {
            var connectionString = "";

            #if DEBUG
                connectionString = "TestConnectionString";
            #else
                connectionString = "ProductionConnectionString";
            #endif

            var dbContext = new ApplicationContext(connectionString);
            var userRepository = new EntityFrameworkRepository<User>(dbContext);
            var journeyRepository = new EntityFrameworkRepository<Journey>(dbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(dbContext);
            var journeyRequestRepository = new EntityFrameworkRepository<JourneyRequest>(dbContext);
            var chatMessageRepository = new EntityFrameworkRepository<ChatMessage>(dbContext);
            var notificationRepository = new EntityFrameworkRepository<Notification>(dbContext);
            var friendsRequestRepository = new EntityFrameworkRepository<FriendRequest>(dbContext);
            var journeyMessageRepository = new EntityFrameworkRepository<JourneyMessage>(dbContext);
            var geoAddressRepository = new EntityFrameworkRepository<GeoAddress>(dbContext);
            var ratingsRepository = new EntityFrameworkRepository<Rating>(dbContext);
            var profilePictureRepository = new EntityFrameworkRepository<ProfilePicture>(dbContext);
            var journeyTemplateRepository = new EntityFrameworkRepository<JourneyTemplate>(dbContext);
            var findNDriveUnitOfWork = new FindNDriveUnitOfWork(
                dbContext,
                userRepository,
                journeyRepository,
                sessionEntityFrameworkRepository,
                journeyRequestRepository,
                chatMessageRepository,
                notificationRepository,
                friendsRequestRepository,
                journeyMessageRepository,
                geoAddressRepository,
                ratingsRepository,
                profilePictureRepository,
                journeyTemplateRepository);

            var sessionManager = new SessionManager(findNDriveUnitOfWork);
            var notificationManager = new NotificationManager(findNDriveUnitOfWork, sessionManager);

            var service = this.serviceType.GetConstructor(new[] { typeof(FindNDriveUnitOfWork), typeof(SessionManager), typeof(NotificationManager) });
            return service != null ? service.Invoke(new object[] { findNDriveUnitOfWork, sessionManager, notificationManager }) : null;
        }

        /// <summary>
        /// The release instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <param name="instance">
        /// The instance.
        /// </param>
        public void ReleaseInstance(InstanceContext instanceContext, object instance)
        {
        }

        #endregion

        #region IContractBehavior Members

        /// <summary>
        /// The add binding parameters.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="bindingParameters">
        /// The binding parameters.
        /// </param>
        public void AddBindingParameters(ContractDescription contractDescription, ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
        }

        /// <summary>
        /// The apply client behavior.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="clientRuntime">
        /// The client runtime.
        /// </param>
        public void ApplyClientBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(new CustomMessageInspector());
        }

        /// <summary>
        /// The apply dispatch behavior.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="dispatchRuntime">
        /// The dispatch runtime.
        /// </param>
        public void ApplyDispatchBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, DispatchRuntime dispatchRuntime)
        {
            dispatchRuntime.InstanceProvider = this;
        }

        /// <summary>
        /// The validate.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        public void Validate(ContractDescription contractDescription, ServiceEndpoint endpoint)
        {
        }

        #endregion
    }

}