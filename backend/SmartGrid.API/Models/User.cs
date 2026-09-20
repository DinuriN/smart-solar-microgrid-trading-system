/*
 * File Name    : User.cs
 * Description  : The base class for all users in the MongoDB database.
 * Author       : [Student Name]
 * IT Number    : [Student IT Number]
 * Date         : 2026-09-18
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace SmartGrid.API.Models
{
    [BsonDiscriminator(RootClass = true)]
    [BsonKnownTypes(typeof(Prosumer), typeof(GridOperator), typeof(BackOfficeUser))]
    public abstract class User
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        public string Name { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty; // We use Email for Backoffice/Grid Operator login
        public string Phone { get; set; } = string.Empty;
        public bool IsActive { get; set; } = true;
        public string PasswordHash { get; set; } = string.Empty;
        
        [BsonRepresentation(BsonType.String)] // Save the enum as text (e.g. "Prosumer") instead of numbers
        public UserRole Role { get; set; }
        
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}
