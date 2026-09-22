import axiosClient from './axiosClient';

export const nodeService = {
    // Get all nodes
    getAll: async () => {
        const response = await axiosClient.get('/nodes');
        return response.data.data;
    },

    // Create a new node
    create: async (nodeData) => {
        const response = await axiosClient.post('/nodes', nodeData);
        return response.data.data;
    },

    // Update a node
    update: async (id, nodeData) => {
        const response = await axiosClient.put(`/nodes/${id}`, nodeData);
        return response.data;
    },

    // Update node schedule
    updateSchedule: async (id, schedule) => {
        const response = await axiosClient.patch(`/nodes/${id}/schedule`, schedule);
        return response.data;
    },

    // Get all battery slots for a specific node
    getBatterySlots: async (id) => {
        const response = await axiosClient.get(`/nodes/${id}/battery-slots`);
        return response.data.data;
    },
    
    // Update battery slots
    updateBatterySlots: async (id, slots) => {
        const response = await axiosClient.put(`/nodes/${id}/battery-slots`, slots);
        return response.data;
    },

    // Check battery slot availability
    checkSlotAvailability: async (nodeId, slotId) => {
        const response = await axiosClient.get(`/nodes/${nodeId}/battery-slots/${slotId}/availability`);
        return response.data.data;
    },

    // Deactivate a node
    deactivate: async (id) => {
        const response = await axiosClient.delete(`/nodes/${id}`);
        return response.data;
    },

    // Get nearby nodes
    getNearby: async (lat, lng, radius = 5.0) => {
        const response = await axiosClient.get('/nodes/nearby', {
            params: { lat, lng, radius }
        });
        return response.data.data;
    }
};

export default nodeService;