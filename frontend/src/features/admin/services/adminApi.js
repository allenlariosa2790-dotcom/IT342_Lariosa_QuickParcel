import apiClient from '../../shared/utils/apiClient';

// User Management
export const getAllUsers = async () => {
  const response = await apiClient.get('/admin/users');
  return response.data;
};

export const updateUserStatus = async (userId, isActive) => {
  const response = await apiClient.put(`/admin/users/${userId}/status`, { isActive });
  return response.data;
};

// Delivery Management
export const getAllDeliveries = async () => {
  const response = await apiClient.get('/admin/deliveries');
  return response.data;
};

export const cancelDelivery = async (deliveryId) => {
  const response = await apiClient.put(`/admin/deliveries/${deliveryId}/cancel`);
  return response.data;
};

// Dashboard Stats
export const getDashboardStats = async () => {
  const response = await apiClient.get('/admin/stats');
  return response.data;
};