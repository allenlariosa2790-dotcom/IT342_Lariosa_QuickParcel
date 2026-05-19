import apiClient from '../../shared/utils/apiClient';

export const createDelivery = async (deliveryData) => {
  const response = await apiClient.post('/deliveries', deliveryData);
  return response.data;
};

export const calculateDistance = async (pickupAddress, dropoffAddress, weight) => {
  const response = await apiClient.post('/deliveries/calculate-distance', {
    pickupAddress,
    dropoffAddress,
    weight: parseFloat(weight) || 1.0
  });
  return response.data;
};