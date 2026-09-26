/*
 * File Name    : NodeGateway.cs
 * Description  : Connects the reservation component to NodeService.
 *                Keeps all node-related dependencies in one place.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

namespace SmartGrid.API.Services
{
    public class NodeGateway : INodeGateway
    {
        private readonly NodeService _nodeService;

        // Constructor injects node service
        public NodeGateway(NodeService nodeService)
        {
            _nodeService = nodeService;
        }

        // Checks that the slot exists on the node and has "Available" status
        public async Task<bool> IsSlotAvailableAsync(string nodeId, string slotId, DateTime scheduledDateTime)
        {
            return await _nodeService.IsBatterySlotAvailableAsync(nodeId, slotId);
        }

        // Marks a slot as "Booked" or releases it back to "Available" after a reservation change
        public async Task<bool> SetSlotBookedAsync(string nodeId, string slotId, bool booked)
        {
            var status = booked ? "Booked" : "Available";
            return await _nodeService.UpdateSlotStatusAsync(nodeId, slotId, status);
        }

        // Gets the nodes the operator is bound to
        public Task<List<string>> GetOperatorNodeIdsAsync(string operatorId)
        {
            // TODO(Member 2): needs an operator ID on nodes and a lookup method.
            // TEMPORARY empty list: operators see no reservations until this is replaced.
            return Task.FromResult(new List<string>());
        }

        // Gets all slot IDs that are currently "Available" on a given node
        public async Task<List<string>> GetAvailableSlotsAsync(string nodeId)
        {
            var slots = await _nodeService.GetBatterySlotsByNodeIdAsync(nodeId);
            return slots
                .Where(s => s.Status == "Available")
                .Select(s => s.Id)
                .ToList();
        }
    }
}