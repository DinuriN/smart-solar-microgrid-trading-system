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
        // True if the slot on this node can be booked at the given time
        Task<bool> IsSlotAvailableAsync(string nodeId, string slotId, DateTime scheduledDateTime);

        // Marks the slot booked (true) or available again (false)
        Task<bool> SetSlotBookedAsync(string nodeId, string slotId, bool booked);

        // IDs of the nodes the Grid Operator is bound to
        Task<List<string>> GetOperatorNodeIdsAsync(string operatorId);
    }
}