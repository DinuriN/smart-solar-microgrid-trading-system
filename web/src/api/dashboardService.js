import axiosClient from './axiosClient';

export const getDashboardStats = () => axiosClient.get('/dashboard/stats');