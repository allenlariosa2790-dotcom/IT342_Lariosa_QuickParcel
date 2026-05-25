import React, { useState, useEffect } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import apiClient from '../../shared/utils/apiClient';

const StripePaymentForm = ({ deliveryId, amount, trackingNumber, onSuccess, onError }) => {
    const stripe = useStripe();
    const elements = useElements();
    const [processing, setProcessing] = useState(false);
    const [paymentError, setPaymentError] = useState(null);
    const [debugInfo, setDebugInfo] = useState('Initializing...');

    useEffect(() => {
        console.log('=== StripePaymentForm DEBUG ===');
        console.log('deliveryId:', deliveryId);
        console.log('amount:', amount);
        console.log('trackingNumber:', trackingNumber);
        console.log('stripe loaded:', !!stripe);
        console.log('elements loaded:', !!elements);
        setDebugInfo(`Form ready. Stripe: ${!!stripe}, Elements: ${!!elements}`);
    }, [deliveryId, amount, trackingNumber, stripe, elements]);

    const handleSubmit = async (event) => {
        event.preventDefault();
        console.log('=== PAYMENT FORM SUBMITTED ===');
        console.log('Stripe available:', !!stripe);
        console.log('Elements available:', !!elements);

        setDebugInfo('Submit clicked...');

        if (!stripe || !elements) {
            const msg = 'Stripe not loaded. Please refresh the page.';
            console.error(msg);
            setPaymentError(msg);
            setDebugInfo(msg);
            return;
        }

        setProcessing(true);
        setPaymentError(null);
        setDebugInfo('Creating payment intent...');

        try {
            console.log('Making API call to /payments/stripe/create-payment-intent');
            console.log('Payload:', { deliveryId, amount, trackingNumber });

            const response = await apiClient.post('/payments/stripe/create-payment-intent', {
                deliveryId: Number(deliveryId),
                amount: Number(amount),
                description: `Delivery ${trackingNumber}`
            });

            console.log('API Response:', response.data);
            setDebugInfo('Payment intent created, confirming card...');

            const { clientSecret, paymentIntentId } = response.data;

            const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
                payment_method: {
                    card: elements.getElement(CardElement),
                },
            });

            if (error) {
                console.error('Stripe error:', error);
                setPaymentError(error.message);
                setDebugInfo(`Error: ${error.message}`);
                onError?.(error.message);
            } else if (paymentIntent.status === 'succeeded') {
                console.log('Payment succeeded!');
                setDebugInfo('Payment succeeded, marking delivery as paid...');

                await apiClient.put(`/deliveries/${deliveryId}/mark-paid`);

                alert(`✅ Payment successful! Delivery ${trackingNumber} has been published.`);
                onSuccess?.(paymentIntent);
            }
        } catch (err) {
            console.error('Payment error:', err);
            console.error('Error response:', err.response?.data);
            setPaymentError(err.message);
            setDebugInfo(`Error: ${err.message}`);
            onError?.(err.message);
        } finally {
            setProcessing(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-4">
            <div className="text-xs text-gray-500 bg-gray-100 p-2 rounded">
                Debug: {debugInfo}
            </div>
            <div className="border rounded-lg p-4 bg-white">
                <CardElement options={{
                    style: {
                        base: { fontSize: '16px', color: '#424770' },
                        invalid: { color: '#9e2146' },
                    },
                }} />
            </div>
            {paymentError && (
                <div className="text-red-600 text-sm bg-red-50 p-2 rounded">
                    ❌ {paymentError}
                </div>
            )}
            <button
                type="submit"
                disabled={!stripe || processing}
                className="w-full bg-[#2563EB] text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50"
            >
                {processing ? 'Processing...' : `Pay ₱${amount?.toFixed(2)}`}
            </button>
            <p className="text-xs text-gray-500 text-center">
                Test Card: 4242 4242 4242 4242 | Any future expiry | Any CVC
            </p>
        </form>
    );
};

export default StripePaymentForm;