import api from './api';

export const createDelivery = async (deliveryData) => {
  const response = await api.post('/deliveries', deliveryData);
  return response.data;
};

export const getMyDeliveries = async () => {
  const response = await api.get('/deliveries/my');
  return response.data;
};

export const getAvailableDeliveries = async () => {
  const response = await api.get('/deliveries/available');
  return response.data;
};

export const acceptDelivery = async (deliveryId) => {
  const response = await api.put(`/deliveries/${deliveryId}/accept`);
  return response.data;
};

export const updateDeliveryStatus = async (deliveryId, status, location) => {
  const response = await api.put(`/deliveries/${deliveryId}/status`, { status, location });
  return response.data;
};

export const getTrackingHistory = async (deliveryId) => {
  const response = await api.get(`/deliveries/${deliveryId}/track`);
  return response.data;
};

export const calculateDistance = async (pickupAddress, dropoffAddress, weight) => {
  const response = await api.post('/deliveries/calculate-distance', {
    pickupAddress,
    dropoffAddress,
    weight: parseFloat(weight) || 1.0
  });
  return response.data;
};