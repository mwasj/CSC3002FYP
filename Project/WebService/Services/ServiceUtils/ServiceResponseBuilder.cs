﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ServiceResponseBuilder.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the ServiceResponseBuilder type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------
namespace Services.ServiceUtils
{
    using System.Collections.Generic;

    using global::Services.ServiceResponses;

    /// <summary>
    /// Provides a set of extension methods to speed up the creation of a ServiceResponse object.
    /// </summary>
    public class ServiceResponseBuilder
    {
        public static ServiceResponse Failure()
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = new List<string>()
            };
        }

        public static ServiceResponse Failure(List<string> errorMessages)
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = errorMessages
            };
        }

        public static ServiceResponse<T> Failure<T>(List<string> errorMessages)
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = errorMessages
            };
        }

        public static ServiceResponse<T> Failure<T>(string errorMessage)
        {
            return Failure<T>(new List<string>() { errorMessage });
        }

        public static ServiceResponse Failure(string errorMessage)
        {
            return Failure(new List<string>() { errorMessage });
        }

        public static ServiceResponse Success()
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Success,
                ErrorMessages = new List<string>()
            };
        }

        public static ServiceResponse<T> Success<T>(T result)
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Success,
                Result = result,
                ErrorMessages = new List<string>()
            };
        }

        public static ServiceResponse<T> Unauthorised<T>()
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Unauthorized
            };
        }

        public static ServiceResponse Unauthorised()
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Unauthorized
            };
        }

        public static ServiceResponse<T> Failure<T>()
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Failure
            };
        }
    }
}