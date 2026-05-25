import apiClient from '../../shared/utils/apiClient';

export const createStripePaymentIntent = async (deliveryId, amount, description) => {
    console.log('=== createStripePaymentIntent called ===');
    console.log('Delivery ID:', deliveryId);
    console.log('Amount:', amount);
    console.log('Description:', description);

    if (!deliveryId) {
        throw new Error('Delivery ID is required');
    }

    if (!amount || amount <= 0) {
        throw new Error('Valid amount is required');
    }

    try {
        const response = await apiClient.post('/payments/stripe/create-payment-intent', {
            deliveryId: Number(deliveryId),
            amount: Number(amount),
            description: String(description)
        });

        console.log('createStripePaymentIntent response:', response.data);

        if (!response.data.clientSecret) {
            throw new Error('No clientSecret received from server');
        }

        return response.data;
    } catch (error) {
        console.error('createStripePaymentIntent error:', error);
        console.error('Error response:', error.response?.data);
        throw error;
    }
};

export const getStripePaymentStatus = async (paymentIntentId) => {
    const response = await apiClient.get(`/payments/stripe/status/${paymentIntentId}`);
    return response.data;
};