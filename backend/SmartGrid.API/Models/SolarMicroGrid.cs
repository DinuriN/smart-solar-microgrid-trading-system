/*
 * File Name    : SolarMicroGrid.cs
 * Description  : Represents a physical solar grid hub/node in the system.
 * Author       : De Silva W.Y.S
 * IT Number    : IT23235038
 * Date         : 2026-09-20
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System.Collections.Generic;

namespace SmartGrid.API.Models
{
    [BsonDiscriminator(RootClass = true)]
    public class SolarMicroGrid
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonElement("nodeName")]
        public string NodeName { get; set; } = string.Empty;

        [BsonElement("location")]
        public GeoLocation Location { get; set; } = new();

        [BsonElement("capacityKWh")]
        public double CapacityKWh { get; set; }

        [BsonElement("isActive")]
        public bool IsActive { get; set; } = true;

        [BsonElement("schedule")]
        public List<string> Schedule { get; set; } = new();
    }
}