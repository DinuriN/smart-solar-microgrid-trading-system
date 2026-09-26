/*
 * File Name    : INodeGateway.cs
 * Description  : Contract for everything the reservation component needs from the microgrid node component.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

namespace SmartGrid.API.Services
{
    public interface INodeGateway
    {
        Task<bool> IsSlotAvailableAsync(string nodeId, string slotId, DateTime scheduledDateTime);
        Task<bool> SetSlotBookedAsync(string nodeId, string slotId, bool booked);
        Task<List<string>> GetOperatorNodeIdsAsync(string operatorId);
        Task<List<string>> GetAvailableSlotsAsync(string nodeId);
    }
}