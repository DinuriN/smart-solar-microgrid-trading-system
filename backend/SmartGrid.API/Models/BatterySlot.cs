/*
 * File Name    : BatterySlot.cs
 * Description  : Represents an individual battery storage slot within a microgrid node.
 * Author       : DE SILVA W.Y.S
 * IT Number    : IT23235038
 * Date         : 2026-09-20
 */
using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;
using System;

namespace SmartGrid.API.Models
{
    public class BatterySlot
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = string.Empty;

        [BsonElement("microGridId")]
        public string MicroGridId { get; set; } = string.Empty;

        [BsonElement("startTime")]
        public DateTime StartTime { get; set; }

        [BsonElement("endTime")]
        public DateTime EndTime { get; set; }

        [BsonElement("status")]
        public string Status { get; set; } = "Available"; // Available, Booked, Maintenance
    }
}