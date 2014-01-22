﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IGroupChat.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IGroupChat type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.ObjectModel;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using DomainObjects.Domains;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The group chat.
    /// </summary>
    [ServiceContract]
    public interface IJourneyChatService
    {
        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="journeyMessageDTO">
        /// The car share message dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/send")]
        ServiceResponse<bool> SendMessage(JourneyMessageDTO journeyMessageDTO);

        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="journeyId">
        /// The car Share Id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getall")]
        ServiceResponse<Collection<JourneyMessage>> RetrieveMessages(int journeyId);
    }
}