/*
 * File Name    : DataSeeder.cs
 * Description  : Seeds the database with a default BackOffice Admin if none exists.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using MongoDB.Driver;
using SmartGrid.API.Models;
using System.Threading.Tasks;
using Microsoft.Extensions.DependencyInjection;
using System;

namespace SmartGrid.API.Database
{
    public static class DataSeeder
    {
        public static async Task SeedAdminUserAsync(IServiceProvider serviceProvider)
        {
            using var scope = serviceProvider.CreateScope();
            var context = scope.ServiceProvider.GetRequiredService<MongoDbContext>();

            // Check if ANY BackOfficeUser already exists
            var existingAdmin = await context.Users.OfType<BackOfficeUser>().Find(u => true).FirstOrDefaultAsync();
            var adminExists = existingAdmin != null;

            if (!adminExists)
            {
                var defaultAdmin = new BackOfficeUser
                {
                    Name = "System Admin",
                    Email = "g@gmail.com",
                    Phone = "0000000000",
                    PasswordHash = BCrypt.Net.BCrypt.HashPassword("12345"),
                    CreatedBy = "System Initialization",
                    IsActive = true
                };

                await context.Users.InsertOneAsync(defaultAdmin);
                Console.WriteLine("backOffice Admin created successfully!");
            }
        }
    }
}
