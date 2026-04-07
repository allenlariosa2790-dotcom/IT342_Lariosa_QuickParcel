import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import { getAvailableDeliveries, acceptDelivery } from '../services/delivery';

const AvailableDeliveries = () => {
  const navigate = useNavigate();
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await getAvailableDeliveries();
      let data = [];
      if (Array.isArray(response.data)) {
        data = response.data;
      } else if (response.data && Array.isArray(response.data.deliveries)) {
        data = response.data.deliveries;
      } else if (response.data && Array.isArray(response.data.content)) {
        data = response.data.content;
      } else {
        console.warn('Unexpected response structure:', response.data);
      }
      setDeliveries(data);
    } catch (err) {
      setError('Failed to load available deliveries');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleAccept = async (deliveryId) => {
    try {
      await acceptDelivery(deliveryId);
      alert('Delivery accepted!');
      fetchDeliveries(); // refresh list
    } catch (err) {
      alert('Accept failed');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="RIDER" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading available deliveries...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="RIDER" />
        <div className="flex-1 p-8">
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-2xl font-bold">Available Deliveries ({deliveries.length})</h1>
            <button
              onClick={() => navigate('/rider-dashboard')}
              className="text-[#2563EB] hover:underline"
            >
              ← Back to Dashboard
            </button>
          </div>

          {error && <div className="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</div>}

          {deliveries.length === 0 ? (
            <div className="bg-white rounded-xl shadow-md p-8 text-center text-gray-500">
              No available deliveries at the moment.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {deliveries.map((delivery) => (
                <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition-shadow">
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-semibold">{delivery.trackingNumber}</span>
                    <span className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">
                      {delivery.parcel?.size || 'Standard'}
                    </span>
                  </div>
                  <div className="text-sm text-gray-600 mb-2">
                    <div>From: {delivery.pickupAddress}</div>
                    <div>To: {delivery.dropoffAddress}</div>
                  </div>
                  <div className="flex justify-between text-sm mb-3">
                    <span>📍 {delivery.distance ? `${delivery.distance.toFixed(2)} km` : 'Distance pending'}</span>
                    <span className="font-semibold text-[#2563EB]">
                      ₱{delivery.estimatedCost?.toFixed(2) || '0.00'}
                    </span>
                  </div>
                  <button
                    onClick={() => handleAccept(delivery.id)}
                    className="w-full bg-[#2563EB] text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
                  >
                    Accept
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AvailableDeliveries;