// TODO: uncomment once NodeService exists

/*
 * File Name    : NodeGateway.cs
 * Description  : Connects the reservation component to  NodeService.
 *                Keeps all node-related dependencies in one place.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

 /*

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

        // Checks that the slot exists on the node and is available
        public async Task<bool> IsSlotAvailableAsync(string nodeId, string slotId, DateTime scheduledDateTime)
        {
            // TODO(Member 2): once a slot's date/start/end time can be read, also check that
            // scheduledDateTime falls inside the slot's window. For now only the status is checked.
            return await _nodeService.IsBatterySlotAvailableAsync(nodeId, slotId);
        }

        //Marks a slot booked or available after a reservation change
        public Task<bool> SetSlotBookedAsync(string nodeId, string slotId, bool booked)
        {
            // TODO(Member 2): call her UpdateSlotStatusAsync once it exists.
            // TEMPORARY no-op so the reservation flow can be tested: the slot is NOT marked booked yet.
            return Task.FromResult(true);
        }

        //Gets the nodes the operator is bound to
        public Task<List<string>> GetOperatorNodeIdsAsync(string operatorId)
        {
            // TODO(Member 2): needs an operator ID on nodes and a lookup method.
            // TEMPORARY empty list: operators see no reservations until this is replaced.
            return Task.FromResult(new List<string>());
        }
    }
} */