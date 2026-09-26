/*
 * File Name    : ReservationService.cs
 * Description  : Business logic for creating, modifying, cancelling and querying energy slot reservations.
 *                Time rules live in ReservationRules and node calls go through INodeGateway.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-21
 */

using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.Reservation;
using SmartGrid.API.Models;
using System.Text.RegularExpressions;

namespace SmartGrid.API.Services
{
    public class ReservationService : IReservationService
    {
        private readonly MongoDbContext _db;
        private readonly INodeGateway _nodeGateway;

        // Constructor injects the database context and the node gateway
        public ReservationService(MongoDbContext db, INodeGateway nodeGateway)
        {
            _db = db;
            _nodeGateway = nodeGateway;
        }

        // ---------- CREATE ----------
        public async Task<ReservationResponseDto> CreateAsync(CreateReservationDto dto, string requesterNic, string requesterRole)
        {
            // Enforce the 7-day scheduling rule on the requested time
            ReservationRules.ValidateSchedulingWindow(dto.ScheduledDateTime);

            if (!Enum.TryParse<ReservationType>(dto.Type, true, out var parsedType))
                throw new ReservationRuleException("Invalid reservation type.");

            // Ask the node component whether the slot is free at the requested time
            var available = await _nodeGateway.IsSlotAvailableAsync(dto.NodeId, dto.BatterySlotId, dto.ScheduledDateTime);
            if (!available)
                throw new ReservationRuleException("Selected battery slot is not available for this time.");

            // Claim the slot first (Two prosumers cannot take it at the same time)
            var claimed = await _nodeGateway.SetSlotBookedAsync(dto.NodeId, dto.BatterySlotId, true);
            if (!claimed)
                throw new ReservationRuleException("Could not reserve the battery slot. Please try again.");

            var reservation = new EnergyReservation
            {
                ProsumerNic = requesterRole == "GridOperator" && !string.IsNullOrEmpty(dto.ProsumerNic)
                    ? dto.ProsumerNic
                    : requesterNic,
                NodeId = dto.NodeId,
                BatterySlotId = dto.BatterySlotId,
                Type = parsedType,
                ScheduledDateTime = dto.ScheduledDateTime,
                Status = ReservationStatus.Pending
            };

            try
            {
                await _db.Reservations.InsertOneAsync(reservation);
            }
            catch
            {
                // Save failed - release the claimed slot.
                await _nodeGateway.SetSlotBookedAsync(dto.NodeId, dto.BatterySlotId, false);
                throw;
            }

            return MapToDto(reservation);
        }

        // ---------- MODIFY ----------
        public async Task<ReservationResponseDto> UpdateAsync(string id, UpdateReservationDto dto, string requesterNic, string requesterRole)
        {
            var reservation = await GetOrThrowAsync(id);
            EnsureOwnerOrOperator(reservation, requesterNic, requesterRole);

            // 12-hour notice rule is checked against the current booking time
            ReservationRules.ValidateModificationWindow(reservation.ScheduledDateTime);

            var newTime = dto.ScheduledDateTime ?? reservation.ScheduledDateTime;
            var newSlotId = string.IsNullOrEmpty(dto.BatterySlotId) ? reservation.BatterySlotId : dto.BatterySlotId;
            var timeChanged = newTime != reservation.ScheduledDateTime;
            var slotChanged = newSlotId != reservation.BatterySlotId;

            // 7-day rule applies whenever the time changes
            if (timeChanged)
                ReservationRules.ValidateSchedulingWindow(newTime);

            // Re-check availability whenever the slot or the time changes
            if (timeChanged || slotChanged)
            {
                var available = await _nodeGateway.IsSlotAvailableAsync(reservation.NodeId, newSlotId, newTime);
                if (!available)
                    throw new ReservationRuleException("Requested battery slot is not available for this time.");
            }

            // Moving to a different slot: claim the new one -> release the old one
            if (slotChanged)
            {
                var claimed = await _nodeGateway.SetSlotBookedAsync(reservation.NodeId, newSlotId, true);
                if (!claimed)
                    throw new ReservationRuleException("Could not reserve the new battery slot. Please try again.");

                await _nodeGateway.SetSlotBookedAsync(reservation.NodeId, reservation.BatterySlotId, false);
                reservation.BatterySlotId = newSlotId;
            }

            reservation.ScheduledDateTime = newTime;

            if (!string.IsNullOrEmpty(dto.Type) && Enum.TryParse<ReservationType>(dto.Type, true, out var parsedType))
                reservation.Type = parsedType;

            reservation.UpdatedAt = DateTime.UtcNow;
            await _db.Reservations.ReplaceOneAsync(r => r.Id == id, reservation);
            return MapToDto(reservation);
        }

        // ---------- CANCEL ----------
        public async Task CancelAsync(string id, string requesterNic, string requesterRole)
        {
            var reservation = await GetOrThrowAsync(id);
            EnsureOwnerOrOperator(reservation, requesterNic, requesterRole);
            ReservationRules.ValidateModificationWindow(reservation.ScheduledDateTime);

            // Release the slot first
            var released = await _nodeGateway.SetSlotBookedAsync(reservation.NodeId, reservation.BatterySlotId, false);
            if (!released)
                throw new ReservationRuleException("Could not release the battery slot. Please try again.");

            var update = Builders<EnergyReservation>.Update
                .Set(r => r.Status, ReservationStatus.Cancelled)
                .Set(r => r.UpdatedAt, DateTime.UtcNow);

            await _db.Reservations.UpdateOneAsync(r => r.Id == id, update);
        }

        // ---------- PROSUMER READS ----------
        public async Task<List<ReservationResponseDto>> GetHistoryAsync(string nic)
        {
            // Full booking history for one prosumer, newest first
            var results = await _db.Reservations
                .Find(r => r.ProsumerNic == nic)
                .SortByDescending(r => r.ScheduledDateTime)
                .ToListAsync();

            return results.Select(MapToDto).ToList();
        }

        public async Task<ReservationCountsDto> GetCountsAsync(string nic)
        {
            // Active = approved reservations, pending = awaiting approval
            var active = await _db.Reservations.CountDocumentsAsync(
                r => r.ProsumerNic == nic && r.Status == ReservationStatus.Approved);
            var pending = await _db.Reservations.CountDocumentsAsync(
                r => r.ProsumerNic == nic && r.Status == ReservationStatus.Pending);

            return new ReservationCountsDto
            {
                ActiveCount = (int)active,
                PendingCount = (int)pending
            };
        }

        // ---------- GRID OPERATOR READS ----------
        public async Task<List<ReservationResponseDto>> GetOperatorReservationsAsync(string operatorId, List<string>? statuses)
        {
            //Find the operator's nodes first (The operator is bound to nodes)
            var nodeIds = await _nodeGateway.GetOperatorNodeIdsAsync(operatorId);
            if (nodeIds.Count == 0)
                return new List<ReservationResponseDto>();

            var filterBuilder = Builders<EnergyReservation>.Filter;
            var filter = filterBuilder.In(r => r.NodeId, nodeIds);

            if (statuses is { Count: > 0 })
            {
                var parsedStatuses = statuses
                    .Select(s => Enum.TryParse<ReservationStatus>(s, true, out var val) ? val : (ReservationStatus?)null)
                    .Where(s => s.HasValue)
                    .Select(s => s!.Value)
                    .ToList();

                filter &= filterBuilder.In(r => r.Status, parsedStatuses);
            }

            var results = await _db.Reservations.Find(filter)
                .SortBy(r => r.ScheduledDateTime)
                .ToListAsync();

            return results.Select(MapToDto).ToList();
        }

        public async Task<ReservationResponseDto?> GetNextForOperatorAsync(string operatorId)
        {
            // Next upcoming approved booking across the operator's nodes
            var nodeIds = await _nodeGateway.GetOperatorNodeIdsAsync(operatorId);
            if (nodeIds.Count == 0)
                return null;

            var next = await _db.Reservations.Find(r => nodeIds.Contains(r.NodeId)
                    && r.Status == ReservationStatus.Approved
                    && r.ScheduledDateTime >= DateTime.UtcNow)
                .SortBy(r => r.ScheduledDateTime)
                .Limit(1)
                .FirstOrDefaultAsync();

            return next is null ? null : MapToDto(next);
        }

        // ---------- BACKOFFICE ----------
        public async Task<List<ReservationResponseDto>> GetBackOfficeQueueAsync(string? status = null)
        {
            var filterBuilder = Builders<EnergyReservation>.Filter;
            var filter = filterBuilder.Empty;

            if (!string.IsNullOrWhiteSpace(status) && Enum.TryParse<ReservationStatus>(status, true, out var parsedStatus))
            {
                filter &= filterBuilder.Eq(r => r.Status, parsedStatus);
            }

            var results = await _db.Reservations
                .Find(filter)
                .SortBy(r => r.ScheduledDateTime)
                .ToListAsync();

            return results.Select(MapToDto).ToList();
        }


        // ---------- SHARED SEARCH ----------
        public async Task<List<ReservationResponseDto>> SearchAsync(string criteria, string requesterRole, string requesterNic, string? requesterOperatorId, string? status = null)
        {
            // Limit results to what the caller is allowed to see -> match the text
            var filterBuilder = Builders<EnergyReservation>.Filter;
            FilterDefinition<EnergyReservation> scopeFilter;

            if (requesterRole == "Prosumer")
            {
                scopeFilter = filterBuilder.Eq(r => r.ProsumerNic, requesterNic);
            }
            else if (requesterRole == "GridOperator")
            {
                var nodeIds = await _nodeGateway.GetOperatorNodeIdsAsync(requesterOperatorId ?? string.Empty);
                scopeFilter = filterBuilder.In(r => r.NodeId, nodeIds);
            }
            else
            {
                scopeFilter = filterBuilder.Empty; // BackOffice: unscoped
            }

            var pattern = Regex.Escape(criteria ?? string.Empty);
            var textFilter = filterBuilder.Or(
                filterBuilder.Regex(r => r.ProsumerNic, new MongoDB.Bson.BsonRegularExpression(pattern, "i")),
                filterBuilder.Regex(r => r.NodeId, new MongoDB.Bson.BsonRegularExpression(pattern, "i")),
                filterBuilder.Regex(r => r.BatterySlotId, new MongoDB.Bson.BsonRegularExpression(pattern, "i"))
            );

            var finalFilter = scopeFilter & textFilter;

            if (!string.IsNullOrWhiteSpace(status) && Enum.TryParse<ReservationStatus>(status, true, out var parsedStatus))
            {
                finalFilter &= filterBuilder.Eq(r => r.Status, parsedStatus);
            }

            var results = await _db.Reservations
                .Find(finalFilter)
                .SortByDescending(r => r.ScheduledDateTime)
                .ToListAsync();

            return results.Select(MapToDto).ToList();
        }

        private async Task<EnergyReservation> GetOrThrowAsync(string id)
        {
            // Load one reservation or fail with 404 semantics
            var reservation = await _db.Reservations.Find(r => r.Id == id).FirstOrDefaultAsync();
            if (reservation is null)
                throw new KeyNotFoundException("Reservation not found.");

            return reservation;
        }

        private static void EnsureOwnerOrOperator(EnergyReservation reservation, string requesterNic, string requesterRole)
        {
            // A prosumer may only change their own reservations
            if (requesterRole == "Prosumer" && reservation.ProsumerNic != requesterNic)
                throw new UnauthorizedAccessException("You do not own this reservation.");
        }

        private static ReservationResponseDto MapToDto(EnergyReservation r) => new()
        {
            // Convert the stored reservation into the response shape sent to clients
            Id = r.Id,
            ProsumerNic = r.ProsumerNic,
            NodeId = r.NodeId,
            BatterySlotId = r.BatterySlotId,
            Type = r.Type.ToString(),
            ScheduledDateTime = r.ScheduledDateTime,
            Status = r.Status.ToString(),
            QrCode = r.QrCode?.Code,
            QrGeneratedAt = r.QrCode?.GeneratedAt,
            CreatedAt = r.CreatedAt
        };
    }
}