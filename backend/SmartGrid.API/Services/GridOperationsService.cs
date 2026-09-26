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
        private readonly MongoDbContext _context;
        private readonly IMongoCollection<BatterySlot> _slots;

        public GridOperationsService(MongoDbContext context)
        {
            _reservations = context.Reservations;
            _context = context;
            _slots = context.BatterySlots;
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
            string qrCode, string OperatorId, DateTime verifiedAt)
        {
            var update = Builders<EnergyReservation>.Update
                .Set("qrCode.verifiedAt", verifiedAt)
                .Set("qrCode.verifiedBy", OperatorId)
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
            string id, ReservationStatus status, string OperatorId)
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
                    && r.QrCode.VerifiedAt != null
                    && r.QrCode!.VerifiedBy == OperatorId,
                update);

            return result.ModifiedCount > 0;
        }

        // Completes the reservation and releases its booked slot together.
        public async Task<bool> CompleteTransferAndReleaseSlotAsync(
            string id, string operatorId)
        {
            if (!ObjectId.TryParse(id, out _))
                return false;

            using var session = await _context.Database.Client.StartSessionAsync();
            session.StartTransaction();

            try
            {
                var reservation = await _reservations.Find(session,
                    r => r.Id == id
                        && r.Status == ReservationStatus.Approved
                        && r.QrCode != null
                        && r.QrCode.VerifiedAt != null
                        && r.QrCode.VerifiedBy == operatorId)
                    .FirstOrDefaultAsync();

                if (reservation == null)
                {
                    await session.AbortTransactionAsync();
                    return false;
                }

                var slotResult = await _slots.UpdateOneAsync(
                    session,
                    s => s.Id == reservation.BatterySlotId
                        && s.MicroGridId == reservation.NodeId
                        && s.Status == "Booked",
                    Builders<BatterySlot>.Update.Set(s => s.Status, "Available"));

                if (slotResult.ModifiedCount != 1)
                {
                    await session.AbortTransactionAsync();
                    return false;
                }

                var reservationResult = await _reservations.UpdateOneAsync(
                    session,
                    r => r.Id == id
                        && r.Status == ReservationStatus.Approved
                        && r.QrCode != null
                        && r.QrCode.VerifiedAt != null
                        && r.QrCode.VerifiedBy == operatorId,
                    Builders<EnergyReservation>.Update
                        .Set(r => r.Status, ReservationStatus.Completed)
                        .Set(r => r.UpdatedAt, DateTime.UtcNow));

                if (reservationResult.ModifiedCount != 1)
                {
                    await session.AbortTransactionAsync();
                    return false;
                }

                await session.CommitTransactionAsync();
                return true;
            }
            catch
            {
                if (session.IsInTransaction)
                    await session.AbortTransactionAsync();
                throw;
            }
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

        // Approves only a reservation that is currently Pending.
        public async Task<bool> ApprovePendingReservationAsync(string id)
        {
            if (!ObjectId.TryParse(id, out _))
                return false;

            var now = DateTime.UtcNow;
            var code = $"QR-{id}-{Convert.ToHexString(
                System.Security.Cryptography.RandomNumberGenerator.GetBytes(32))}";

            var update = Builders<EnergyReservation>.Update
                .Set(r => r.Status, ReservationStatus.Approved)
                .Set(r => r.QrCode, new QrInfo
                {
                    Code = code,
                    GeneratedAt = now
                })
                .Set(r => r.UpdatedAt, now);

            var result = await _reservations.UpdateOneAsync(
                r => r.Id == id
                    && r.Status == ReservationStatus.Pending
                    && r.ScheduledDateTime > now
                    && r.QrCode == null,
                update);

            return result.ModifiedCount == 1;
        }
    }
}