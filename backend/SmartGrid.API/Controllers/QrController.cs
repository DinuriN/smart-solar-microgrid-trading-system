/*
 * File Name    : QrController.cs
 * Description  : Handles QR verification and energy transfer finalization
 *                for Grid Operator operations.
 *                TODO: Align shared reservation/status fields and Grid Operator
 *                identification with final implementations from Members 1 and 3.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using Microsoft.AspNetCore.Mvc;
using SmartGrid.API.DTOs.GridOperations;
using SmartGrid.API.Services;

namespace SmartGrid.API.Controllers
{
    [ApiController]
    [Route("api/qr")]
    public class QrController : ControllerBase
    {
        private readonly GridOperationsService _gridOperationsService;

        // Initializes the controller with the Grid Operations service.
        public QrController(GridOperationsService gridOperationsService)
        {
            _gridOperationsService = gridOperationsService;
        }

        // Verifies a prosumer transaction QR code against reservation data.
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

            if (string.IsNullOrWhiteSpace(request.OperatorId))
            {
                return BadRequest(new
                {
                    verified = false,
                    message = "Operator ID is required."
                });
            }

            var reservation =
                await _gridOperationsService
                    .GetByQrCodeAsync(request.QrCode);

            if (reservation == null)
            {
                return NotFound(new
                {
                    verified = false,
                    message = "Invalid QR code."
                });
            }

            // TODO: Confirm the final reservation status values with Member 3.
            if (!string.Equals(
                reservation.Status,
                "Approved",
                StringComparison.OrdinalIgnoreCase))
            {
                return BadRequest(new
                {
                    verified = false,
                    message =
                        "This reservation is not approved for energy transfer."
                });
            }

            if (reservation.QrCode.VerifiedAt != null)
            {
                return BadRequest(new
                {
                    verified = false,
                    message =
                        "This QR code has already been verified."
                });
            }

            DateTime verifiedAt = DateTime.UtcNow;

            bool updated =
                await _gridOperationsService.VerifyQrCodeAsync(
                    request.QrCode,
                    request.OperatorId,
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
                reservationId = reservation.ReservationCode,
                status = reservation.Status,
                verifiedBy = request.OperatorId,
                verifiedAt = verifiedAt,
                message = "QR code verified successfully."
            });
        }

        // Finalizes an energy transfer after successful QR verification.
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

            if (string.IsNullOrWhiteSpace(request.OperatorId))
            {
                return BadRequest(new
                {
                    success = false,
                    message = "Operator ID is required."
                });
            }

            var reservation =
                await _gridOperationsService
                    .GetByReservationCodeAsync(request.ReservationId);

            if (reservation == null)
            {
                return NotFound(new
                {
                    success = false,
                    message = "Reservation not found."
                });
            }

            if (reservation.QrCode.VerifiedAt == null)
            {
                return BadRequest(new
                {
                    success = false,
                    message =
                        "QR code must be verified before finalizing the transfer."
                });
            }

            // TODO: Confirm the final reservation status values with Member 3.
            if (string.Equals(
                reservation.Status,
                "Completed",
                StringComparison.OrdinalIgnoreCase))
            {
                return BadRequest(new
                {
                    success = false,
                    message =
                        "Energy transfer has already been completed."
                });
            }

            bool updated =
                await _gridOperationsService
                    .UpdateReservationStatusAsync(
                        request.ReservationId,
                        "Completed");

            if (!updated)
            {
                return StatusCode(500, new
                {
                    success = false,
                    message =
                        "Failed to finalize energy transfer."
                });
            }

            /*
             * TODO: Update the associated BatterySlot /
             * EnergyBookingSlots record to Available after Members 2/3
             * finalize the shared slot model and collection structure.
             */

            return Ok(new
            {
                success = true,
                reservationId = request.ReservationId,
                reservationStatus = "Completed",
                finalizedBy = request.OperatorId,
                finalizedAt = DateTime.UtcNow,
                message = "Energy transfer finalized successfully."
            });
        }
    }
}
