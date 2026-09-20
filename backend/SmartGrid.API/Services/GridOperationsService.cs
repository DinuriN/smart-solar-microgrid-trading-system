/*
 * File Name    : GridOperationsService.cs
 * Description  : Handles MongoDB operations required by the Grid Operations,
 *                QR verification and operational dashboard component.
 *                TODO: Align shared reservation fields and collection name
 *                with Member 3's final implementation.
 * Author       : Dinuri Nureka A L D
 * IT Number    : IT23192478
 * Date         : 2026-09-20
 */

using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.GridOperations;
using SmartGrid.API.Models;

namespace SmartGrid.API.Services
{
    public class GridOperationsService
    {
        private readonly IMongoCollection<EnergyReservation> _reservations;

        // Initializes access to the temporary shared EnergyReservation collection
        public GridOperationsService(MongoDbContext context)
        {
            // TODO: Confirm the final collection name with Member 3.
            _reservations =
                context.Database.GetCollection<EnergyReservation>(
                    "energyReservations");
        }

        // Retrieves a reservation using its reservation code.
        public async Task<EnergyReservation?> GetByReservationCodeAsync(
            string reservationCode)
        {
            return await _reservations
                .Find(r => r.ReservationCode == reservationCode)
                .FirstOrDefaultAsync();
        }

        // Stores a newly generated QR code against a reservation.
        public async Task<bool> UpdateQrCodeAsync(
            string reservationCode,
            string qrCode,
            DateTime generatedAt)
        {
            var update = Builders<EnergyReservation>.Update
                .Set(r => r.QrCode.Code, qrCode)
                .Set(r => r.QrCode.GeneratedAt, generatedAt);

            var result = await _reservations.UpdateOneAsync(
                r => r.ReservationCode == reservationCode,
                update);

            return result.ModifiedCount > 0;
        }

        // Retrieves a reservation using its generated QR code.
        public async Task<EnergyReservation?> GetByQrCodeAsync(
            string qrCode)
        {
            return await _reservations
                .Find(r => r.QrCode.Code == qrCode)
                .FirstOrDefaultAsync();
        }

        // Records successful QR verification details.
        public async Task<bool> VerifyQrCodeAsync(
            string qrCode,
            string operatorId,
            DateTime verifiedAt)
        {
            var update = Builders<EnergyReservation>.Update
                .Set(r => r.QrCode.VerifiedAt, verifiedAt)
                .Set(r => r.QrCode.VerifiedBy, operatorId);

            var result = await _reservations.UpdateOneAsync(
                r => r.QrCode.Code == qrCode,
                update);

            return result.ModifiedCount > 0;
        }

        // Updates the status of an energy reservation.
        public async Task<bool> UpdateReservationStatusAsync(
            string reservationCode,
            string status)
        {
            var update = Builders<EnergyReservation>.Update
                .Set(r => r.Status, status);

            var result = await _reservations.UpdateOneAsync(
                r => r.ReservationCode == reservationCode,
                update);

            return result.ModifiedCount > 0;
        }

        // Retrieves operational dashboard statistics from MongoDB.
        public async Task<DashboardStatsResponseDto> GetDashboardStatsAsync()
        {
            var pendingFilter =
                Builders<EnergyReservation>.Filter.Eq(
                    r => r.Status,
                    "Pending");

            var approvedFutureFilter =
                Builders<EnergyReservation>.Filter.And(
                    Builders<EnergyReservation>.Filter.Eq(
                        r => r.Status,
                        "Approved"),
                    Builders<EnergyReservation>.Filter.Gt(
                        r => r.ScheduledDateTime,
                        DateTime.UtcNow)
                );

            var pendingCount =
                await _reservations.CountDocumentsAsync(pendingFilter);

            var approvedFutureCount =
                await _reservations.CountDocumentsAsync(
                    approvedFutureFilter);

            return new DashboardStatsResponseDto
            {
                PendingReservations = pendingCount,
                ApprovedFutureReservations = approvedFutureCount
            };
        }
    }
}
