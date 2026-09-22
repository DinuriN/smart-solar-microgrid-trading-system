/*
 * File Name    : ReservationsController.cs
 * Description  : Endpoints for creating, modifying and
 *                cancelling and viewing energy slot reservations.(Reservation history,Counts for Prosumer; Reservations queues and Upcoming Reservation lookups for Grid Operator; read-only Reservation Queue for BackOffice user)
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-22
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.Reservation;
using SmartGrid.API.Services;
using System.Security.Claims;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/reservations")]
    [Authorize]
    public class ReservationsController : ControllerBase
    {
        private readonly IReservationService _reservationService;

        //Constructor injects the reservation service
        public ReservationsController(IReservationService reservationService)
        {
            _reservationService = reservationService;
        }

        
        private string RequesterNic => User.FindFirstValue("nic") ?? string.Empty;
        private string RequesterRole => User.FindFirstValue(ClaimTypes.Role) ?? string.Empty;
        private string RequesterId => User.FindFirstValue(ClaimTypes.NameIdentifier) ?? string.Empty;

        /// <summary>Creates a new energy slot reservation</summary>
        [HttpPost]
        [Authorize(Roles = "Prosumer,GridOperator")]
        [ProducesResponseType(typeof(ReservationResponseDto), 201)]
        [ProducesResponseType(400)]
        public async Task<IActionResult> Create(CreateReservationDto dto)
        {
            //Delegates validation and slot-availability checks to the service
            try
            {
                var result = await _reservationService.CreateAsync(dto, RequesterNic, RequesterRole);
                return CreatedAtAction(nameof(Create), new { id = result.Id }, result);
            }
            catch (ReservationRuleException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
        }

        /// <summary>Modifies an existing reservation (requires 12h notice)</summary>
        [HttpPut("{id}")]
        [Authorize(Roles = "Prosumer,GridOperator")]
        [ProducesResponseType(typeof(ReservationResponseDto), 200)]
        [ProducesResponseType(400)]
        [ProducesResponseType(404)]
        public async Task<IActionResult> Update(string id, UpdateReservationDto dto)
        {
            //Only BatterySlotId, ScheduledDateTime and Type are editable
            try
            {
                var result = await _reservationService.UpdateAsync(id, dto, RequesterNic, RequesterRole);
                return Ok(result);
            }
            catch (ReservationRuleException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        /// <summary>Cancels a reservation (requires 12h notice)</summary>
        [HttpDelete("{id}")]
        [Authorize(Roles = "Prosumer,GridOperator")]
        [ProducesResponseType(204)]
        [ProducesResponseType(400)]
        [ProducesResponseType(404)]
        public async Task<IActionResult> Cancel(string id)
        {
            //Releases the battery slot via the service before marking the reservation Cancelled
            try
            {
                await _reservationService.CancelAsync(id, RequesterNic, RequesterRole);
                return NoContent();
            }
            catch (ReservationRuleException ex)
            {
                return BadRequest(new { message = ex.Message });
            }
            catch (KeyNotFoundException)
            {
                return NotFound();
            }
        }

        /// <summary>Returns a prosumer's full booking history</summary>
        [HttpGet("history/{nic}")]
        [Authorize(Roles = "Prosumer")]
        [ProducesResponseType(typeof(List<ReservationResponseDto>), 200)]
        public async Task<IActionResult> GetHistory(string nic)
        {
            //Returns every reservation for this prosumer
            var history = await _reservationService.GetHistoryAsync(nic);
            return Ok(history);
        }

        /// <summary>Returns active/pending reservation counts for a prosumer</summary>
        [HttpGet("counts/{nic}")]
        [Authorize(Roles = "Prosumer")]
        [ProducesResponseType(typeof(ReservationCountsDto), 200)]
        public async Task<IActionResult> GetCounts(string nic)
        {
            //Active = Approved, Pending = awaiting Backoffice User approval
            var counts = await _reservationService.GetCountsAsync(nic);
            return Ok(counts);
        }

        /// <summary>Returns reservations assigned to a Grid Operator, optionally filtered by status</summary>
        [HttpGet("operator/{operatorId}")]
        [Authorize(Roles = "GridOperator")]
        [ProducesResponseType(typeof(List<ReservationResponseDto>), 200)]
        public async Task<IActionResult> GetOperatorReservations(string operatorId, [FromQuery] string? status)
        {
            //Splits a comma-separated status query into a list; null means no filter
            var statuses = string.IsNullOrEmpty(status)
                ? null
                : status.Split(',', StringSplitOptions.RemoveEmptyEntries | StringSplitOptions.TrimEntries).ToList();

            var results = await _reservationService.GetOperatorReservationsAsync(operatorId, statuses);
            return Ok(results);
        }

        /// <summary>Returns the Grid Operator's next upcoming approved booking</summary>
        [HttpGet("operator/{operatorId}/next")]
        [Authorize(Roles = "GridOperator")]
        [ProducesResponseType(typeof(ReservationResponseDto), 200)]
        [ProducesResponseType(204)]
        public async Task<IActionResult> GetNextForOperator(string operatorId)
        {
            //Returns 204 if the operator has no upcoming approved reservation
            var next = await _reservationService.GetNextForOperatorAsync(operatorId);
            return next is null ? NoContent() : Ok(next);
        }

        /// <summary>Returns the full reservation queue for BackOffice review (read-only —
        /// Approve/Block actions are owned by Member 4 which updates the same EnergyReservation document's Status field.)</summary>
        [HttpGet("queue")]
        [Authorize(Roles = "BackOfficeUser")]
        [ProducesResponseType(typeof(List<ReservationResponseDto>), 200)]
        public async Task<IActionResult> GetQueue()
        {
            //Unscoped list of every reservation, sorted by scheduled time
            var queue = await _reservationService.GetBackOfficeQueueAsync();
            return Ok(queue);
        }

        /// <summary>Searches reservations scoped by the caller's role</summary>
        [HttpGet("search")]
        [ProducesResponseType(typeof(List<ReservationResponseDto>), 200)]
        public async Task<IActionResult> Search([FromQuery] string criteria, [FromQuery] string? status)
        {
            //Grid Operator's own id, used to scope results to their nodes
            var operatorId = RequesterRole == "GridOperator" ? RequesterId : null;

            var results = await _reservationService.SearchAsync(criteria, RequesterRole, RequesterNic, operatorId, status);
            return Ok(results);
        }
    }

}
