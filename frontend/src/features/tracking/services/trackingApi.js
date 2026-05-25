import apiClient from '../../shared/utils/apiClient';

export const getMyDeliveries = async () => {
  const response = await apiClient.get('/tracking/my');
  return response.data;
};

export const getDeliveryById = async (id) => {
  const response = await apiClient.get(`/tracking/delivery/${id}`);
  return response.data;
};

export const getTrackingHistory = async (id) => {
  const response = await apiClient.get(`/tracking/delivery/${id}/history`);
  return response.data;
};

export const getParcelImage = async (id) => {
  const response = await apiClient.get(`/tracking/delivery/${id}/image`);
  return response.data;
};