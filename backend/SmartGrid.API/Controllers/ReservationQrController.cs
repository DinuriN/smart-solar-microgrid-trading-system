/*
 * File Name    : ReservationQrController.cs
 * Description  : Handles QR generation for approved energy reservations.
 *                TODO: Align shared reservation model and status values
 *                with Member 3's final implementation.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.Models;
using SmartGrid.API.Services;
using Microsoft.AspNetCore.Authorization;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/reservations")]

    public class ReservationQrController : ControllerBase
    {
        private readonly GridOperationsService _gridOperationsService;

        // Initializes the controller with the Grid Operations service
        public ReservationQrController(
            GridOperationsService gridOperationsService)
        {
            _gridOperationsService = gridOperationsService;
        }

        // Generates and stores a unique QR code for an approved reservation.
        [HttpPost("{id}/generate-qr")]
        public async Task<IActionResult> GenerateQr(string id)
        {
            if (string.IsNullOrWhiteSpace(id))
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Reservation ID is required."
                });
            }

            var reservation =
                await _gridOperationsService
                    .GetByReservationIdAsync(id);

            if (reservation == null)
            {
                return NotFound(new
                {
                    success = false,
                    message = "Reservation not found."
                });
            }

            // TODO: Confirm the final reservation status values with Member 3.
            if (reservation.Status != ReservationStatus.Approved)
            {
                return BadRequest(new
                {
                    success = false,
                    message =
                        "QR code can only be generated for approved reservations."
                });
            }

            string qrCode =
                $"QR-{id}-{Guid.NewGuid():N}";

            DateTime generatedAt = DateTime.UtcNow;

            bool updated =
                await _gridOperationsService.UpdateQrCodeAsync(
                    id,
                    qrCode,
                    generatedAt);

            if (!updated)
            {
                return StatusCode(500, new
                {
                    success = false,
                    message = "Failed to save QR code."
                });
            }

            return Ok(new
            {
                success = true,
                reservationId = id,
                qrCode = qrCode,
                generatedAt = generatedAt,
                message = "QR code generated successfully."
            });
        }

        // Allows a Backoffice user to approve a pending reservation.
        [HttpPatch("{id}/approve")]
        [Authorize(Roles = "BackOfficeUser")]
        public async Task<IActionResult> ApproveReservation(string id)
        {
            var reservation = await _gridOperationsService.GetByReservationIdAsync(id);

            if (reservation == null)
                return NotFound(new { message = "Reservation not found." });

            if (reservation.Status != ReservationStatus.Pending)
                return Conflict(new { message = "Only pending reservations can be approved." });

            var approved = await _gridOperationsService.ApprovePendingReservationAsync(id);

            if (!approved)
                return Conflict(new { message = "Reservation status changed. Refresh and try again." });

            return Ok(new { success = true, reservationId = id, status = "Approved" });
        }
    }
}
