import apiClient from '../../shared/utils/apiClient';

export const createPayMongoPayment = async (deliveryId, amount, description) => {
  try {
    const response = await apiClient.post('/payments/paymongo/create', {
      deliveryId,
      amount,
      description
    });
    return response.data;
  } catch (error) {
    console.error('PayMongo payment creation failed:', error);
    throw error;
  }
};