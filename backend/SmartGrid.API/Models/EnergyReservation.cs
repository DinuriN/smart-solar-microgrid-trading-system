/*
 * File Name    : EnergyReservation.cs
 * Description  : Represents an energy slot reservation made by a Prosumer at a Microgrid Node.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartGrid.API.Models
{
    // Status flow: Pending -> Approved/Blocked -> Completed/Cancelled
    public enum ReservationStatus
    {
        Pending,
        Approved,
        Blocked,
        Completed,
        Cancelled
    }

    public enum ReservationType
    {
        Charging,
        EnergyDropOff
    }

    // Nested QR subdocument
    public class QrInfo
    {
        [BsonElement("code")]
        public string? Code { get; set; }

        [BsonElement("generatedAt")]
        public DateTime? GeneratedAt { get; set; }

        [BsonElement("verifiedAt")]
        public DateTime? VerifiedAt { get; set; }
    }

    public class EnergyReservation
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string Id { get; set; } = ObjectId.GenerateNewId().ToString();

        // Links to Prosumer.Nic 
        [BsonElement("prosumerNic")]
        public string ProsumerNic { get; set; } = string.Empty;

        // Links to the Solar_MicroGrid node this reservation is made at
        [BsonElement("nodeId")]
        public string NodeId { get; set; } = string.Empty;

        // Links to the BatterySlot reserved on that node
        [BsonElement("batterySlotId")]
        public string BatterySlotId { get; set; } = string.Empty;

        [BsonElement("type")]
        [BsonRepresentation(BsonType.String)]
        public ReservationType Type { get; set; }

        [BsonElement("scheduledDateTime")]
        public DateTime ScheduledDateTime { get; set; }

        [BsonElement("status")]
        [BsonRepresentation(BsonType.String)]
        public ReservationStatus Status { get; set; } = ReservationStatus.Pending;

        // Populated by approve/QR-generation logic
        [BsonElement("qrCode")]
        public QrInfo? QrCode { get; set; }

        [BsonElement("createdAt")]
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

        [BsonElement("updatedAt")]
        public DateTime? UpdatedAt { get; set; }
    }
}