import apiClient from '../../shared/utils/apiClient';

export const getAvailableDeliveries = async () => {
  const response = await apiClient.get('/rider/deliveries/available');
  return response.data;
};

export const acceptDelivery = async (deliveryId) => {
  const response = await apiClient.put(`/rider/deliveries/${deliveryId}/accept`);
  return response.data;
};

export const updateDeliveryStatus = async (deliveryId, status, location) => {
  const response = await apiClient.put(`/rider/deliveries/${deliveryId}/status`, { status, location });
  return response.data;
};

export const markPaymentAsPaid = async (deliveryId) => {
  const response = await apiClient.put(`/deliveries/${deliveryId}/mark-paid`);
  return response.data;
};