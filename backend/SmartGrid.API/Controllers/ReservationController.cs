/*
 * File Name    : ReservationsController.cs
 * Description  : Prosumer-facing endpoints for creating, modifying and
 *                cancelling energy slot reservations.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
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
    }
}