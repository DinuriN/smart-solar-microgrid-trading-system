/*
 * File Name    : ReservationResponseDto.cs
 * Description  : Data transfer object returned to clients describing a reservation.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */
 
 namespace SmartGrid.API.DTOs.Reservation
{
    public class ReservationResponseDto
    {
        public string Id { get; set; } = null!;
        public string ProsumerNic { get; set; } = null!;
        public string NodeId { get; set; } = null!;
        public string BatterySlotId { get; set; } = null!;
        public string Type { get; set; } = null!;
        public DateTime ScheduledDateTime { get; set; }
        public string Status { get; set; } = null!;
        public string? QrCode { get; set; }
        public DateTime? QrGeneratedAt { get; set; }
        public DateTime CreatedAt { get; set; }
    }
}