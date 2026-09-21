import axiosClient from './axiosClient';

export const prosumerService = {
  // Get all prosumers
  getAll: async () => {
    const response = await axiosClient.get('/prosumers');
    // Extract the array from the backend's { success: true, data: [...] } wrapper
    return response.data.data;
  },

  // Deactivate a prosumer (Grid Operator / Backoffice)
  deactivate: async (nic) => {
    const response = await axiosClient.patch(`/prosumers/${nic}/deactivate`);
    return response.data;
  },

  // Reactivate a prosumer (Backoffice)
  reactivate: async (nic) => {
    const response = await axiosClient.patch(`/prosumers/${nic}/reactivate`);
    return response.data;
  }
};
