﻿using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.Web;
using DomainObjects;
using FindNDriveInfrastructureDataAccessLayer;

namespace FindNDriveDataAccessLayer
{
    public class FindNDriveUnitOfWork : IUnitOfWork
    {
        private readonly DbContext _dbContext;

        /**
        * Each IRepository<T> repesents a repository available in the database. 
        **/
        public IRepository<User> UserRepository { get; set; }
        public IRepository<CarShare> CarShare { get; set; }

        public FindNDriveUnitOfWork(DbContext dbContext, IRepository<User> userRepository, IRepository<CarShare> carShareRepository)
        {
            _dbContext = dbContext;
            UserRepository = userRepository;
            CarShare = carShareRepository;
        }

        public void Commit()
        {
            _dbContext.SaveChanges();
        }

        public void Dispose()
        {
            _dbContext.Dispose();
        }
    }
}