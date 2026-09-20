/*
 * File Name    : ProsumerService.cs
 * Description  : Contains FAT business logic for Prosumer operations (register, update, etc.).
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.Prosumer;
using SmartGrid.API.Models;
using System;
using System.Threading.Tasks;

namespace SmartGrid.API.Services
{
    public class ProsumerService
    {
        private readonly IMongoCollection<User> _usersCollection;

        // Injects the MongoDbContext to access the database.
        public ProsumerService(MongoDbContext context)
        {
            _usersCollection = context.Users;
        }

        // Registers a new Prosumer. Validates that NIC and Email are unique.
        public async Task<ProsumerResponseDto> RegisterAsync(RegisterProsumerDto dto)
        {
            // 1. Business Logic: Check if NIC is unique (Because it's the logical primary key!)
            // We use OfType<Prosumer>() to specifically search through Prosumers inside the Users collection.
            var existingByNic = await _usersCollection.OfType<Prosumer>().Find(p => p.Nic == dto.Nic).FirstOrDefaultAsync();
            if (existingByNic != null)
                throw new Exception("A Prosumer with this NIC already exists.");

            // 2. Map DTO to Model and Hash Password
            var prosumer = new Prosumer
            {
                Nic = dto.Nic,
                Name = dto.Name,
                Email = dto.Email,
                Phone = dto.Phone,
                Address = dto.Address,
                PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.Password), // Securely hashed!
                IsActive = true
            };

            // 3. Save to MongoDB
            await _usersCollection.InsertOneAsync(prosumer);

            // 4. Return secure DTO
            return MapToResponseDto(prosumer);
        }

        // Retrieves a Prosumer using their NIC.
        public async Task<ProsumerResponseDto?> GetByNicAsync(string nic)
        {
            var prosumer = await _usersCollection.OfType<Prosumer>().Find(p => p.Nic == nic).FirstOrDefaultAsync();
            return prosumer == null ? null : MapToResponseDto(prosumer);
        }

        public async Task<Prosumer> RequestDeactivationAsync(string nic)
        {
            var update = Builders<User>.Update.Set(u => ((Prosumer)u).DeactivationRequested, true);
            var result = await _usersCollection.FindOneAndUpdateAsync<User, User>(
                u => u is Prosumer && ((Prosumer)u).Nic == nic,
                update,
                new FindOneAndUpdateOptions<User, User> { ReturnDocument = ReturnDocument.After }
            );

            if (result == null)
                throw new Exception("Prosumer not found.");

            return (Prosumer)result;
        }

        // Updates allowable profile fields. NIC cannot be updated.
        public async Task<bool> UpdateProfileAsync(string nic, UpdateProsumerDto dto)
        {
            var update = Builders<User>.Update
                .Set(p => p.Name, dto.Name)
                .Set(p => p.Phone, dto.Phone)
                // We have to cast to Prosumer to update Prosumer-specific fields
                .Set(nameof(Prosumer.Address), dto.Address);

            var result = await _usersCollection.UpdateOneAsync(
                Builders<User>.Filter.OfType<Prosumer>(p => p.Nic == nic), 
                update);

            return result.ModifiedCount > 0;
        }

        // Toggles the IsActive status (used for deactivate/reactivate).
        public async Task<bool> SetActiveStatusAsync(string nic, bool isActive)
        {
            var update = Builders<User>.Update.Set(p => p.IsActive, isActive);
            var result = await _usersCollection.UpdateOneAsync(
                Builders<User>.Filter.OfType<Prosumer>(p => p.Nic == nic), 
                update);
            return result.ModifiedCount > 0;
        }

        // Helper to convert Model to Response DTO securely
        private ProsumerResponseDto MapToResponseDto(Prosumer prosumer)
        {
            return new ProsumerResponseDto
            {
                Nic = prosumer.Nic,
                Name = prosumer.Name,
                Email = prosumer.Email,
                Phone = prosumer.Phone,
                Address = prosumer.Address,
                IsActive = prosumer.IsActive,
                DeactivationRequested = prosumer.DeactivationRequested,
                Role = prosumer.Role.ToString()
            };
        }
    }
}
