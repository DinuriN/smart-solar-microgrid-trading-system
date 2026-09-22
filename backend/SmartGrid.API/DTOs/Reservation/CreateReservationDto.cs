/*
 * File Name    : CreateReservationDto.cs
 * Description  : Data transfer object carrying the details needed to create an energy slot reservation.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */
 
 namespace SmartGrid.API.DTOs.Reservation
{
    public class CreateReservationDto
    {
        public string NodeId { get; set; } = null!;
        public string BatterySlotId { get; set; } = null!;
        public string Type { get; set; } = null!; // "Charging" | "EnergyDropOff"
        public DateTime ScheduledDateTime { get; set; }

        // Only used when a Grid Operator creates on behalf of a prosumer. (Prosumer requests infer this from their own JWT instead.)
        public string? ProsumerNic { get; set; }
    }
}