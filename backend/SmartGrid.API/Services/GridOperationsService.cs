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

using MongoDB.Bson;
using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.GridOperations;
using SmartGrid.API.Models;

namespace SmartGrid.API.Services
{
    public class GridOperationsService
    {
        private readonly IMongoCollection<EnergyReservation> _reservations;

        public GridOperationsService(MongoDbContext context)
        {
            _reservations = context.Reservations;
        }

        public async Task<EnergyReservation?> GetByReservationIdAsync(string id)
        {
            if (!ObjectId.TryParse(id, out _))
                return null;

            return await _reservations
                .Find(r => r.Id == id)
                .FirstOrDefaultAsync();
        }

        public async Task<bool> UpdateQrCodeAsync(
            string id, string qrCode, DateTime generatedAt)
        {
            if (!ObjectId.TryParse(id, out _))
                return false;

            var update = Builders<EnergyReservation>.Update
                .Set(r => r.QrCode, new QrInfo
                {
                    Code = qrCode,
                    GeneratedAt = generatedAt
                })
                .Set(r => r.UpdatedAt, DateTime.UtcNow);

            var result = await _reservations.UpdateOneAsync(
                r => r.Id == id
                    && r.Status == ReservationStatus.Approved
                    && r.QrCode == null,
                update);

            return result.ModifiedCount > 0;
        }

        public async Task<EnergyReservation?> GetByQrCodeAsync(string qrCode)
        {
            return await _reservations
                .Find(r => r.QrCode != null && r.QrCode.Code == qrCode)
                .FirstOrDefaultAsync();
        }

        public async Task<bool> VerifyQrCodeAsync(
            string qrCode, string operatorId, DateTime verifiedAt)
        {
            var update = Builders<EnergyReservation>.Update
                .Set("qrCode.verifiedAt", verifiedAt)
                .Set("qrCode.verifiedBy", operatorId)
                .Set(r => r.UpdatedAt, DateTime.UtcNow);

            var result = await _reservations.UpdateOneAsync(
                r => r.QrCode != null
                    && r.QrCode.Code == qrCode
                    && r.QrCode.VerifiedAt == null
                    && r.Status == ReservationStatus.Approved,
                update);

            return result.ModifiedCount > 0;
        }

        public async Task<bool> UpdateReservationStatusAsync(
            string id, ReservationStatus status)
        {
            if (!ObjectId.TryParse(id, out _))
                return false;

            var update = Builders<EnergyReservation>.Update
                .Set(r => r.Status, status)
                .Set(r => r.UpdatedAt, DateTime.UtcNow);

            var result = await _reservations.UpdateOneAsync(
                r => r.Id == id
                    && r.Status == ReservationStatus.Approved
                    && r.QrCode != null
                    && r.QrCode.VerifiedAt != null,
                update);

            return result.ModifiedCount > 0;
        }

        public async Task<DashboardStatsResponseDto> GetDashboardStatsAsync()
        {
            var pendingCount = await _reservations.CountDocumentsAsync(
                r => r.Status == ReservationStatus.Pending);

            var approvedFutureCount = await _reservations.CountDocumentsAsync(
                r => r.Status == ReservationStatus.Approved
                    && r.ScheduledDateTime > DateTime.UtcNow);

            return new DashboardStatsResponseDto
            {
                PendingReservations = pendingCount,
                ApprovedFutureReservations = approvedFutureCount
            };
        }
    }
}