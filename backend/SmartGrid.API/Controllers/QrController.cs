/*
 * File Name    : QrController.cs
 * Description  : Handles QR verification and energy transfer finalization
 *                for Grid Operator operations.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MongoDB.Bson;
using SmartGrid.API.DTOs.GridOperations;
using SmartGrid.API.Models;
using SmartGrid.API.Services;
using System.Security.Claims;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/qr")]
    [Authorize(Roles = "GridOperator")]
    public class QrController : ControllerBase
    {
        private readonly GridOperationsService _gridOperationsService;

        private string? OperatorId =>
            User.FindFirstValue(ClaimTypes.NameIdentifier)
            ?? User.FindFirstValue("sub");

        public QrController(GridOperationsService gridOperationsService)
        {
            _gridOperationsService = gridOperationsService;
        }

        [HttpPost("verify")]
        public async Task<IActionResult> VerifyQr(
            [FromBody] VerifyQrRequestDto request)
        {
            if (string.IsNullOrWhiteSpace(request.QrCode))
            {
                return BadRequest(new
                {
                    verified = false,
                    message = "QR code is required."
                });
            }

            if (string.IsNullOrWhiteSpace(OperatorId))
            {
                return BadRequest(new
                {
                    verified = false,
                    message = "Operator ID is required."
                });
            }

            var reservation = await _gridOperationsService
                .GetByQrCodeAsync(request.QrCode);

            if (reservation == null)
            {
                return NotFound(new
                {
                    verified = false,
                    message = "Invalid QR code."
                });
            }

            // A QR verified earlier by this operator can be scanned again.
            // Completed transfers can be viewed, but cannot be finalized again.
            if (reservation.QrCode?.VerifiedAt != null)
            {
                if (reservation.Status != ReservationStatus.Approved &&
                    reservation.Status != ReservationStatus.Completed)
                {
                    return BadRequest(new
                    {
                        verified = false,
                        message = "This reservation is not available for energy transfer."
                    });
                }

                if (reservation.QrCode.VerifiedBy != OperatorId)
                {
                    return Conflict(new
                    {
                        verified = false,
                        message = "This QR code was verified by another operator."
                    });
                }

                return Ok(new
                {
                    verified = true,
                    alreadyVerified = true,
                    reservationId = reservation.Id,
                    prosumerNic = reservation.ProsumerNic,
                    nodeId = reservation.NodeId,
                    batterySlotId = reservation.BatterySlotId,
                    type = reservation.Type.ToString(),
                    scheduledDateTime = reservation.ScheduledDateTime,
                    status = reservation.Status.ToString(),
                    verifiedBy = reservation.QrCode.VerifiedBy,
                    verifiedAt = reservation.QrCode.VerifiedAt,
                    message = reservation.Status == ReservationStatus.Completed
                        ? "Energy transfer has already been completed."
                        : "QR code was already verified by you."
                });
            }

            if (reservation.Status != ReservationStatus.Approved)
            {
                return BadRequest(new
                {
                    verified = false,
                    message = "This reservation is not approved for energy transfer."
                });
            }

            DateTime verifiedAt = DateTime.UtcNow;

            bool updated = await _gridOperationsService.VerifyQrCodeAsync(
                request.QrCode,
                OperatorId,
                verifiedAt);

            if (!updated)
            {
                return StatusCode(500, new
                {
                    verified = false,
                    message = "Failed to verify QR code."
                });
            }

            return Ok(new
            {
                verified = true,
                alreadyVerified = false,
                reservationId = reservation.Id,
                prosumerNic = reservation.ProsumerNic,
                nodeId = reservation.NodeId,
                batterySlotId = reservation.BatterySlotId,
                type = reservation.Type.ToString(),
                scheduledDateTime = reservation.ScheduledDateTime,
                status = reservation.Status.ToString(),
                verifiedBy = OperatorId,
                verifiedAt,
                message = "QR code verified successfully."
            });
        }

        [HttpPost("finalize-transfer")]
        public async Task<IActionResult> FinalizeTransfer(
            [FromBody] FinalizeTransferRequestDto request)
        {
            if (string.IsNullOrWhiteSpace(request.ReservationId))
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Reservation ID is required."
                });
            }

            if (string.IsNullOrWhiteSpace(OperatorId))
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Operator ID is required."
                });
            }

            var reservation = await _gridOperationsService
                .GetByReservationIdAsync(request.ReservationId);

            if (reservation == null)
            {
                return NotFound(new
                {
                    success = false,
                    message = "Reservation not found."
                });
            }

            if (reservation.QrCode?.VerifiedAt == null)
            {
                return BadRequest(new
                {
                    success = false,
                    message = "QR code must be verified before finalizing the transfer."
                });
            }

            if (reservation.Status == ReservationStatus.Completed)
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Energy transfer has already been completed."
                });
            }

            if (reservation.Status != ReservationStatus.Approved)
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Reservation is not approved."
                });
            }

            if (reservation.QrCode.VerifiedBy != OperatorId)
            {
                return StatusCode(403, new
                {
                    success = false,
                    message = "Another operator verified this QR code."
                });
            }

            if (!ObjectId.TryParse(reservation.BatterySlotId, out _))
            {
                return Conflict(new
                {
                    success = false,
                    message = "This reservation has an invalid battery slot ID."
                });
            }

            bool updated = await _gridOperationsService
                .CompleteTransferAndReleaseSlotAsync(
                    request.ReservationId,
                    OperatorId!);

            if (!updated)
            {
                return StatusCode(500, new
                {
                    success = false,
                    message = "Failed to finalize energy transfer."
                });
            }

            return Ok(new
            {
                success = true,
                reservationId = request.ReservationId,
                reservationStatus = "Completed",
                finalizedBy = OperatorId,
                finalizedAt = DateTime.UtcNow,
                message = "Energy transfer finalized successfully."
            });
        }
    }
}