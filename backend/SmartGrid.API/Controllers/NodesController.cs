/*
* File Name    : NodesController.cs
* Description  : Thin client controller for Microgrid Node management. Routes requests to NodeService.
* Author       : De Silva W.Y.S
* IT Number    : IT23235038
* Date         : 2026-09-20
*/
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.Nodes;
using SmartGrid.API.Models;
using SmartGrid.API.Services;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SmartGrid.API.Controllers
{
    // Inline comment: API route prefix for all node-related endpoints
    [ApiController]
    [Route("api/[controller]")]
    public class NodesController : ControllerBase
    {
        private readonly NodeService _nodeService;

        // Inline comment: Injects the FAT service containing all node business logic
        public NodesController(NodeService nodeService)
        {
            _nodeService = nodeService;
        }

        // Inline comment: Creates a new microgrid node (BackOffice only)
        [HttpPost]
        [Authorize(Roles = "BackOfficeUser")]
        public async Task<IActionResult> CreateNode([FromBody] CreateNodeDto dto)
        {
            try
            {
                var node = await _nodeService.CreateNodeAsync(dto);
                return Ok(new { success = true, data = node });
            }
            catch (Exception ex)
            {
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        // Inline comment: Retrieves all microgrid nodes (public access for mobile map)
        [HttpGet]
        public async Task<IActionResult> GetAllNodes()
        {
            var nodes = await _nodeService.GetAllActiveNodesAsync();
            return Ok(new { success = true, data = nodes });
        }

        // Inline comment: Retrieves a single node by its MongoDB ObjectId
        [HttpGet("{id}")]
        public async Task<IActionResult> GetNodeById(string id)
        {
            var node = await _nodeService.GetNodeByIdAsync(id);
            if (node == null)
                return NotFound(new { success = false, message = "Node not found" });

            return Ok(new { success = true, data = node });
        }

        // Inline comment: Updates node details including schedule and capacity (BackOffice/GridOperator)
        [HttpPut("{id}")]
        [Authorize(Roles = "BackOfficeUser,GridOperator")]
        public async Task<IActionResult> UpdateNode(string id, [FromBody] UpdateNodeDto dto)
        {
            var success = await _nodeService.UpdateNodeAsync(id, dto);
            if (!success)
                return NotFound(new { success = false, message = "Node not found or no changes made" });

            return Ok(new { success = true, message = "Node updated successfully" });
        }

        // Inline comment: Updates only the operational schedule of a node (GridOperator)
        [HttpPatch("{id}/schedule")]
        [Authorize(Roles = "BackOfficeUser,GridOperator")]
        public async Task<IActionResult> UpdateSchedule(string id, [FromBody] List<string> schedule)
        {
            var dto = new UpdateNodeDto { Schedule = schedule };
            var success = await _nodeService.UpdateNodeAsync(id, dto);
            if (!success)
                return NotFound(new { success = false, message = "Node not found" });

            return Ok(new { success = true, message = "Schedule updated successfully" });
        }

        // Inline comment: Publicly exposes battery slot availability check for Reservation Service integration
        [HttpGet("{id}/battery-slots/{slotId}/availability")]
        public async Task<IActionResult> CheckBatterySlotAvailability(string id, string slotId)
        {
            var isAvailable = await _nodeService.IsBatterySlotAvailableAsync(id, slotId);
            
            return Ok(new { 
                success = true, 
                data = new { 
                    nodeId = id, 
                    slotId = slotId, 
                    isAvailable = isAvailable 
                } 
            });
        }

        // Inline comment: Updates battery slot availability for a specific node (GridOperator)
        [HttpPut("{id}/battery-slots")]
        [Authorize(Roles = "BackOfficeUser,GridOperator")]
        public async Task<IActionResult> UpdateBatterySlots(string id, [FromBody] List<BatterySlot> slots)
        {
            var success = await _nodeService.UpdateBatterySlotsAsync(id, slots);
            if (!success)
                return NotFound(new { success = false, message = "Node not found" });

            return Ok(new { success = true, message = "Battery slots updated successfully" });
        }

        // Inline comment: Deactivates a node ONLY after checking for active reservations (BackOffice)
        [HttpDelete("{id}")]
        [Authorize(Roles = "BackOfficeUser")]
        public async Task<IActionResult> DeactivateNode(string id)
        {
            try
            {
                var success = await _nodeService.DeactivateNodeAsync(id);
                if (!success)
                    return NotFound(new { success = false, message = "Node not found" });

                return Ok(new { success = true, message = "Node deactivated successfully" });
            }
            catch (Exception ex)
            {
                // Inline comment: Returns 400 if active reservations block deactivation
                return BadRequest(new { success = false, message = ex.Message });
            }
        }

        // Inline comment: Finds nearby nodes based on GPS coordinates for mobile map display
        [HttpGet("nearby")]
        public async Task<IActionResult> GetNearbyNodes(
            [FromQuery] double lat,
            [FromQuery] double lng,
            [FromQuery] double radius = 5.0)
        {
            var nodes = await _nodeService.GetNearbyNodesAsync(lat, lng, radius);
            return Ok(new { success = true, data = nodes });
        }
    }
}