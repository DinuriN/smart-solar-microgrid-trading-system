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
//get all web users
        public async Task<List<User>> GetAllWebUsersAsync()
        {
            var users = await _usersCollection.Find(u => u.Role == UserRole.GridOperator || u.Role == UserRole.BackOfficeUser).ToListAsync();
//password hash before returning
            foreach(var user in users)
            {
                user.PasswordHash = string.Empty;
            }
            return users;
        }
//admin password reset
        public async Task<bool> ResetPasswordAsync(string userId, string newPassword)
        {
            var hashedPassword = BCrypt.Net.BCrypt.HashPassword(newPassword);
            var update = Builders<User>.Update.Set(u => u.PasswordHash, hashedPassword);
            var result = await _usersCollection.UpdateOneAsync(u => u.Id == userId, update);
            return result.ModifiedCount > 0;
        }


    }
}
