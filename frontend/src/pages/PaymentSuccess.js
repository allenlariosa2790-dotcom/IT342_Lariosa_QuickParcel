import React, { useEffect, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import api from '../services/api';

const PaymentSuccess = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('Verifying payment...');
  const [countdown, setCountdown] = useState(5);

  useEffect(() => {
    const paymentIntentId = searchParams.get('payment_intent_id');
    const deliveryId = searchParams.get('deliveryId');

    console.log('Payment success page loaded:', { paymentIntentId, deliveryId });

    if (paymentIntentId && deliveryId) {
      // Call backend to verify payment and update status
      api.get(`/payments/paymongo/status/${paymentIntentId}?deliveryId=${deliveryId}`)
        .then(response => {
          console.log('Payment status response:', response.data);
          if (response.data.status === 'COMPLETED') {
            setStatus('Payment successful! Your delivery has been published.');
            // Start countdown to redirect
            const timer = setInterval(() => {
              setCountdown(prev => {
                if (prev <= 1) {
                  clearInterval(timer);
                  navigate('/my-deliveries');
                }
                return prev - 1;
              });
            }, 1000);
          } else {
            setStatus('Payment pending. Please check back later.');
          }
        })
        .catch(error => {
          console.error('Payment verification failed:', error);
          setStatus('Payment verification failed. Please contact support.');
        });
    } else {
      setStatus('No payment information found.');
      console.warn('Missing payment_intent_id or deliveryId');
    }
  }, [searchParams, navigate]);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="container mx-auto px-4 py-16">
        <div className="max-w-md mx-auto bg-white rounded-xl shadow-md p-8 text-center">
          <div className="text-green-500 text-5xl mb-4">✓</div>
          <h1 className="text-2xl font-bold mb-2">Payment Successful!</h1>
          <p className="text-gray-600 mb-4">{status}</p>
          {status.includes('successful') && (
            <p className="text-sm text-gray-500">
              Redirecting to your deliveries in {countdown} seconds...
            </p>
          )}
          <button
            onClick={() => navigate('/my-deliveries')}
            className="mt-4 bg-[#2563EB] text-white px-6 py-2 rounded-lg hover:bg-blue-700"
          >
            Go to My Deliveries
          </button>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;