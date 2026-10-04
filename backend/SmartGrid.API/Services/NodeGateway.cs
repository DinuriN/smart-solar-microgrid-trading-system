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

        // Checks that the slot is "Available" and its time window covers the requested time
        public async Task<bool> IsSlotAvailableAsync(string nodeId, string slotId, DateTime scheduledDateTime)
        {
            var free = await _nodeService.GetAvailableSlotsAtTimeAsync(nodeId, scheduledDateTime);
            return free.Any(s => s.Id == slotId);
        }

        // Checks that the time falls inside the slot's window, whatever the slot's status (used when a reservation keeps its own, already booked, slot and only the time changes)
        public async Task<bool> SlotCoversTimeAsync(string nodeId, string slotId, DateTime time)
        {
            var slot = await _nodeService.GetSlotAsync(nodeId, slotId);
            return slot != null && slot.StartTime <= time && slot.EndTime >= time;
        }

        // Marks a slot as "Booked" or releases it back to "Available" after a reservation change
        public async Task<bool> SetSlotBookedAsync(string nodeId, string slotId, bool booked)
        {
            var status = booked ? "Booked" : "Available";
            return await _nodeService.UpdateSlotStatusAsync(nodeId, slotId, status);
        }

        // Gets the nodes a grid operator works with. (Operators are not tied to one node for now)
        public async Task<List<string>> GetOperatorNodeIdsAsync(string operatorId)
        {
            var nodes = await _nodeService.GetAllActiveNodesAsync();
            return nodes.Select(n => n.Id).ToList();
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