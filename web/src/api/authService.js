import axiosClient from './axiosClient';

export const authService = {
  
  // Call the POST /api/auth/login endpoint
  login: async (emailOrNic, password) => {
    try {
      const response = await axiosClient.post('/auth/login', {
        emailOrNic: emailOrNic,
        password: password
      });
      return response.data;
    } catch (error) {
      // Pass the error message from the C# backend to the frontend
      throw error.response?.data?.message || 'Failed to connect to server';
    }
  }

};
