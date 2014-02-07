﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendRequestDecision.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendRequestDecision type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The friend request decision.
    /// </summary>
    public enum FriendRequestDecision
    {
        /// <summary>
        /// The undecided.
        /// </summary>
        [EnumMember(Value = "Undecided")]
        Undecided = 0,

        /// <summary>
        /// The accepted.
        /// </summary>
        [EnumMember(Value = "Accepted")]
        Accepted = 1,

        /// <summary>
        /// The rejected.
        /// </summary>
        [EnumMember(Value = "Rejected")]
        Rejected = 2
    }
}
