/*
* File Name    : GeoLocation.cs
* Description  : Embedded document for storing GPS coordinates of microgrid nodes.
* Author       : De Silva W.Y.S
* IT Number    : IT23235038
* Date         : 2026-09-20
*/
using MongoDB.Bson.Serialization.Attributes;

namespace SmartGrid.API.Models
{
    public class GeoLocation
    {
        [BsonElement("latitude")]
        public double Latitude { get; set; }

        [BsonElement("longitude")]
        public double Longitude { get; set; }
        
        // Store original address text for admin reference (from Web App geocoding)
        [BsonElement("address")] 
        public string? Address { get; set; }
    }
}