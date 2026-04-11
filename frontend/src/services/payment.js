import api from './api';

export const createPayMongoPayment = async (deliveryId, amount, description) => {
  try {
    const response = await api.post('/payments/paymongo/create', {
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

export const getPayMongoPaymentStatus = async (paymentReference) => {
  try {
    const response = await api.get(`/payments/paymongo/status/${paymentReference}`);
    return response.data;
  } catch (error) {
    console.error('Failed to get payment status:', error);
    throw error;
  }
};