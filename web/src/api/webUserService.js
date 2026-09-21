import axiosClient from './axiosClient';

export const webUserService = {
  // Get all web users (Grid Operators & BackOffice Users)
  getAll: async () => {
    const response = await axiosClient.get('/users');
    return response.data.data;
  },

  // Create a new web user
  create: async (userData) => {
    const response = await axiosClient.post('/users', userData);
    return response.data.data;
  },

  // Admin password reset
  resetPassword: async (userId, newPassword) => {
    const response = await axiosClient.put(`/users/${userId}/reset-password`, { newPassword });
    return response.data;
  }
};
