/*
 * File Name    : EnergyReservation.cs
 * Description  : Temporary shared model representing an energy reservation
 *                required by the Grid Operations component.
 *                TODO: Align with Member 3's final reservation model before integration.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using MongoDB.Bson;
using MongoDB.Bson.Serialization.Attributes;

namespace SmartGrid.API.Models
{
    public class EnergyReservation
    {
        [BsonId]
        [BsonRepresentation(BsonType.ObjectId)]
        public string? Id { get; set; }

        public string ReservationCode { get; set; } = string.Empty;

        public string ProsumerNic { get; set; } = string.Empty;

        public string BatterySlotId { get; set; } = string.Empty;

        public DateTime ScheduledDateTime { get; set; }

        public string Status { get; set; } = string.Empty;

        public QrCodeInfo QrCode { get; set; } = new QrCodeInfo();
    }

    public class QrCodeInfo
    {
        public string? Code { get; set; }

        public DateTime? GeneratedAt { get; set; }

        public DateTime? VerifiedAt { get; set; }

        public string? VerifiedBy { get; set; }
    }
}
