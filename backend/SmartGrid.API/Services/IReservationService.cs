/*
 * File Name    : IReservationService.cs
 * Description  : Contract for the reservation business logic used by the reservations controller.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

using SmartGrid.API.DTOs.Reservation;

namespace SmartGrid.API.Services
{
    public interface IReservationService
    {
        Task<ReservationResponseDto> CreateAsync(CreateReservationDto dto, string requesterNic, string requesterRole);
        Task<ReservationResponseDto> UpdateAsync(string id, UpdateReservationDto dto, string requesterNic, string requesterRole);
        Task CancelAsync(string id, string requesterNic, string requesterRole);
        Task<List<ReservationResponseDto>> GetHistoryAsync(string nic);
        Task<ReservationCountsDto> GetCountsAsync(string nic);
        Task<List<ReservationResponseDto>> GetOperatorReservationsAsync(string operatorId, List<string>? statuses);
        Task<ReservationResponseDto?> GetNextForOperatorAsync(string operatorId);

        // Read-only list for BackOffice.
        Task<List<ReservationResponseDto>> GetBackOfficeQueueAsync();
        Task<List<ReservationResponseDto>> SearchAsync(string criteria, string requesterRole, string requesterNic, string? requesterOperatorId);
    }
}