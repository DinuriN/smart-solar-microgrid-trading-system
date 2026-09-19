/*
 * File Name    : UserService.cs
 * Description  : Contains logic for managing Web Users (Grid Operators & BackOffice).
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.User;
using SmartGrid.API.Models;
using System;
using System.Threading.Tasks;

namespace SmartGrid.API.Services
{
    public class UserService
    {
        private readonly IMongoCollection<User> _usersCollection;

        public UserService(MongoDbContext context)
        {
            _usersCollection = context.Users;
        }

        public async Task<User> CreateWebUserAsync(CreateWebUserDto dto, string createdByUsername)
        {
            var existingUser = await _usersCollection.Find(u => u.Email == dto.Email).FirstOrDefaultAsync();
            if (existingUser != null)
                throw new Exception("A user with this Email already exists.");

            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(dto.Password);

            User newUser;
            if (dto.Role == UserRole.GridOperator)
            {
                newUser = new GridOperator
                {
                    Name = dto.Name,
                    Email = dto.Email,
                    Phone = dto.Phone,
                    PasswordHash = hashedPassword
                };
            }
            else if (dto.Role == UserRole.BackOfficeUser)
            {
                newUser = new BackOfficeUser
                {
                    Name = dto.Name,
                    Email = dto.Email,
                    Phone = dto.Phone,
                    PasswordHash = hashedPassword,
                    CreatedBy = createdByUsername
                };
            }
            else
            {
                throw new Exception("Invalid role for Web User creation.");
            }

            await _usersCollection.InsertOneAsync(newUser);
            newUser.PasswordHash = string.Empty; 
            return newUser;
        }
    }
}
