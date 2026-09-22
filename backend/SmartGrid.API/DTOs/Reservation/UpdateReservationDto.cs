/*
 * File Name    : UpdateReservationDto.cs
 * Description  : Data transfer object carrying the details needed to update an energy slot reservation.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */
 namespace SmartGrid.API.DTOs.Reservation
{
    public class UpdateReservationDto
    {
        public string? BatterySlotId { get; set; }
        public DateTime? ScheduledDateTime { get; set; }
        public string? Type { get; set; }
    }
}