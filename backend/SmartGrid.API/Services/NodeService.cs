/*
* File Name    : NodeService.cs
* Description  : FAT service containing all business logic for microgrid node management.
* Author       : De Silva W.Y.S
* IT Number    : IT23235038
* Date         : 2026-09-20
*/
using MongoDB.Driver;
using SmartGrid.API.Database;
using SmartGrid.API.DTOs.Nodes;
using SmartGrid.API.Models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace SmartGrid.API.Services
{
    public class NodeService
    {
        private readonly IMongoCollection<SolarMicroGrid> _nodesCollection;
        private readonly IMongoCollection<BatterySlot> _batterySlotsCollection;
        // TODO: Inject EnergyReservation collection once Member 3 provides schema
        // private readonly IMongoCollection<EnergyReservation> _reservationsCollection;

        // Inline comment: Constructor injects MongoDB collections for node operations
        public NodeService(MongoDbContext context)
        {
            _nodesCollection = context.SolarMicroGrids;
            _batterySlotsCollection = context.BatterySlots;
        }

        // Inline comment: Creates a new microgrid node with validated data
        public async Task<SolarMicroGrid> CreateNodeAsync(CreateNodeDto dto)
        {
            var node = new SolarMicroGrid
            {
                NodeName = dto.NodeName,
                Location = new GeoLocation
                {
                    Latitude = dto.Latitude,
                    Longitude = dto.Longitude,
                    Address = dto.Address
                },
                CapacityKWh = dto.CapacityKWh,
                IsActive = true
            };

            await _nodesCollection.InsertOneAsync(node);
            return node;
        }

        // Inline comment: Retrieves all active microgrid nodes for dashboard display
        public async Task<List<SolarMicroGrid>> GetAllActiveNodesAsync()
        {
            return await _nodesCollection.Find(n => n.IsActive).ToListAsync();
        }

        // Inline comment: Retrieves a single node by its MongoDB ObjectId
        public async Task<SolarMicroGrid?> GetNodeByIdAsync(string id)
        {
            return await _nodesCollection.Find(n => n.Id == id).FirstOrDefaultAsync();
        }

        // Inline comment: Updates node details including schedule and capacity
        public async Task<bool> UpdateNodeAsync(string id, UpdateNodeDto dto)
        {
            // Create an empty update definition builder
            var update = Builders<SolarMicroGrid>.Update;
            var updates = new List<UpdateDefinition<SolarMicroGrid>>();

            // Manually check each field for null before adding to the update list
            if (!string.IsNullOrEmpty(dto.NodeName))
                updates.Add(update.Set(n => n.NodeName, dto.NodeName));

            if (dto.Latitude.HasValue)
                updates.Add(update.Set(n => n.Location.Latitude, dto.Latitude.Value));

            if (dto.Longitude.HasValue)
                updates.Add(update.Set(n => n.Location.Longitude, dto.Longitude.Value));

            if (!string.IsNullOrEmpty(dto.Address))
                updates.Add(update.Set(n => n.Location.Address, dto.Address));

            if (dto.CapacityKWh.HasValue)
                updates.Add(update.Set(n => n.CapacityKWh, dto.CapacityKWh.Value));

            if (dto.Schedule != null && dto.Schedule.Count > 0)
                updates.Add(update.Set(n => n.Schedule, dto.Schedule));

            // Combine all valid updates into a single operation
            var combinedUpdate = update.Combine(updates);

            // Only execute if there are actual changes to make
            if (updates.Count == 0) return false;

            var result = await _nodesCollection.UpdateOneAsync(n => n.Id == id, combinedUpdate);
            return result.ModifiedCount > 0;
        }

        // Inline comment: CRITICAL - Checks EnergyReservation collection for active/pending bookings before allowing deactivation
        // TODO: Uncomment and implement reservation check once Member 3 provides exact field names
        public async Task<bool> DeactivateNodeAsync(string nodeId)
        {
            /*
            var activeReservations = await _reservationsCollection.CountDocumentsAsync(
                r => r.[MEMBER3_FIELD_NAME] == nodeId && 
                     (r.Status == "[MEMBER3_PENDING_STATUS]" || r.Status == "[MEMBER3_APPROVED_STATUS]")
            );

            if (activeReservations > 0)
            {
                throw new Exception($"Cannot deactivate node: {activeReservations} active reservation(s) exist.");
            }
            */

            // Placeholder: Actual reservation check will be added after Member 3 provides schema
            var update = Builders<SolarMicroGrid>.Update.Set(n => n.IsActive, false);
            var result = await _nodesCollection.UpdateOneAsync(n => n.Id == nodeId, update);
            return result.ModifiedCount > 0;
        }

        // Inline comment: Checks if a specific battery slot exists and has "Available" status
        public async Task<bool> IsBatterySlotAvailableAsync(string nodeId, string slotId)
        {
            var slot = await _batterySlotsCollection
                .Find(s => s.Id == slotId && s.MicroGridId == nodeId)
                .FirstOrDefaultAsync();

            // Returns true only if slot exists AND status is exactly "Available"
            return slot != null && slot.Status == "Available";
        }

        // Inline comment: Retrieves all battery slots for a specific node (for viewing/editing in UI)
        public async Task<List<BatterySlot>> GetBatterySlotsByNodeIdAsync(string nodeId)
        {
            return await _batterySlotsCollection
                .Find(s => s.MicroGridId == nodeId)
                .SortBy(s => s.StartTime)
                .ToListAsync();
        }

        // Inline comment: Updates the status of battery slots for a specific node
        public async Task<bool> UpdateBatterySlotsAsync(string nodeId, List<BatterySlot> slots)
        {
            // Inline comment: Bulk update or replace battery slots for the node
            await _batterySlotsCollection.DeleteManyAsync(s => s.MicroGridId == nodeId);
            if (slots != null && slots.Count > 0)
            {
                await _batterySlotsCollection.InsertManyAsync(slots);
            }
            return true;
        }

        // Inline comment: Calculates great-circle distance between two GPS points in kilometers using Haversine formula
        private double CalculateDistance(double lat1, double lon1, double lat2, double lon2)
        {
            const double R = 6371; // Earth's radius in km
            var dLat = (lat2 - lat1) * Math.PI / 180;
            var dLon = (lon2 - lon1) * Math.PI / 180;
            var a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                    Math.Cos(lat1 * Math.PI / 180) * Math.Cos(lat2 * Math.PI / 180) *
                    Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            var c = 2 * Math.Atan2(Math.Sqrt(a), Math.Sqrt(1 - a));
            return R * c;
        }

        // Inline comment: Filters active nodes within specified radius using Haversine distance calculation
        public async Task<List<SolarMicroGrid>> GetNearbyNodesAsync(double lat, double lng, double radiusKm = 5.0)
        {
            var allActiveNodes = await GetAllActiveNodesAsync();
            return allActiveNodes.Where(n =>
                CalculateDistance(lat, lng, n.Location.Latitude, n.Location.Longitude) <= radiusKm
            ).ToList();
        }

        // ==========================================
        // NEW METHODS FOR MEMBER 3 (Reservation Workflow)
        // ==========================================

        // 1. Get a single battery slot by ID (For Member 3's reservation validation)
        public async Task<BatterySlot?> GetSlotAsync(string nodeId, string slotId)
        {
            return await _batterySlotsCollection
                .Find(s => s.Id == slotId && s.MicroGridId == nodeId)
                .FirstOrDefaultAsync();
        }

        // 2. Update status of a single slot (For Member 3's booking workflow)
        public async Task<bool> UpdateSlotStatusAsync(string nodeId, string slotId, string status)
        {
            var update = Builders<BatterySlot>.Update.Set(s => s.Status, status);
            var result = await _batterySlotsCollection.UpdateOneAsync(
                s => s.Id == slotId && s.MicroGridId == nodeId, 
                update
            );
            return result.ModifiedCount > 0;
        }

        // 3. Get slots available at specific arrival time (For Member 3's mobile booking)
        public async Task<List<BatterySlot>> GetAvailableSlotsAtTimeAsync(string nodeId, DateTime arrivalTime)
        {
            return await _batterySlotsCollection
                .Find(s => s.MicroGridId == nodeId && 
                           s.StartTime <= arrivalTime && 
                           s.EndTime >= arrivalTime && 
                           s.Status == "Available")
                .SortBy(s => s.StartTime)
                .ToListAsync();
        }
 
    }
}