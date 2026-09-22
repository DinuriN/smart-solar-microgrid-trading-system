/*
 * File Name    : ReservationCountDto.cs
 * Description  : Data transfer object holding a prosumer's active and pending reservation counts.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */
 
 namespace SmartGrid.API.DTOs.Reservation
{
    public class ReservationCountsDto
    {
        public int ActiveCount { get; set; }
        public int PendingCount { get; set; }
    }
}