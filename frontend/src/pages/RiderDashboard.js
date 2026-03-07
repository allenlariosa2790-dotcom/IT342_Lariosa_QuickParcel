import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';

const RiderDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Mock data for available deliveries
  const availableDeliveries = [
    {
      id: 'QP-2026-007',
      size: 'Medium',
      pickup: '123 Main St, Downtown',
      dropoff: '456 Oak Ave, Uptown',
      distance: '3.2 km',
      weight: '2.5 kg',
      earnings: '$8.50'
    },
    {
      id: 'QP-2026-008',
      size: 'Large',
      pickup: '789 Elm St, Midtown',
      dropoff: '321 Pine Rd, Suburbs',
      distance: '5.8 km',
      weight: '4.0 kg',
      earnings: '$12.00'
    },
    {
      id: 'QP-2026-009',
      size: 'Small',
      pickup: '555 Market St, Downtown',
      dropoff: '888 Broadway, Center',
      distance: '2.1 km',
      weight: '1.0 kg',
      earnings: '$6.50'
    }
  ];

  // Mock data for active delivery
  const activeDelivery = {
    id: 'QP-2026-001',
    pickup: '123 Main St, Downtown',
    dropoff: '456 Oak Ave, Uptown',
    status: 'Picked up at 11:15 AM'
  };

  useEffect(() => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    if (!token || !userData) {
      navigate('/login');
      return;
    }

    setUser(JSON.parse(userData));
    setLoading(false);
  }, [navigate]);

  const handleAcceptDelivery = (deliveryId) => {
    alert(`Delivery ${deliveryId} accepted! (Demo functionality)`);
    // In real app, this would call an API
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600">Loading dashboard...</p>
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
          {/* Welcome Banner */}
          <div className="bg-gradient-to-r from-[#2563EB] to-blue-700 text-white rounded-xl p-6 mb-8">
            <div className="flex justify-between items-center">
              <div>
                <h1 className="text-2xl font-bold">Welcome back, {user?.firstName || 'Michael'}!</h1>
                <p className="opacity-90 mt-1">Ready to earn today?</p>
              </div>
              <div className="bg-white text-[#2563EB] px-4 py-2 rounded-lg font-semibold">
                Today's Earnings: $42
              </div>
            </div>
          </div>

          {/* Delivery Hotspots Map Placeholder */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Delivery Hotspots</h3>
            <div className="bg-gray-100 h-48 rounded-lg flex items-center justify-center">
              <p className="text-gray-500">Map showing available delivery hotspots</p>
            </div>
          </div>

          {/* Available Deliveries */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">Available Deliveries (3)</h3>
              <button className="text-[#2563EB] hover:underline">View All</button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {availableDeliveries.map((delivery) => (
                <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition-shadow">
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-semibold">{delivery.id}</span>
                    <span className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">{delivery.size}</span>
                  </div>
                  <div className="text-sm text-gray-600 mb-2">
                    <div>From: {delivery.pickup}</div>
                    <div>To: {delivery.dropoff}</div>
                  </div>
                  <div className="flex justify-between text-sm mb-3">
                    <span>📍 {delivery.distance}</span>
                    <span>⚖️ {delivery.weight}</span>
                    <span className="font-semibold text-[#2563EB]">{delivery.earnings}</span>
                  </div>
                  <button
                    onClick={() => handleAcceptDelivery(delivery.id)}
                    className="w-full bg-[#2563EB] text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
                  >
                    Accept
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* Active Delivery */}
          {activeDelivery && (
            <div className="bg-white rounded-xl shadow-md p-6 mb-8">
              <h3 className="text-lg font-bold mb-4">Active Delivery</h3>
              <div className="border-l-4 border-[#2563EB] pl-4">
                <div className="font-semibold">{activeDelivery.id}</div>
                <div className="text-sm text-gray-600 mb-2">
                  {activeDelivery.pickup} → {activeDelivery.dropoff}
                </div>
                <div className="text-sm text-green-600">✅ {activeDelivery.status}</div>
              </div>
            </div>
          )}

          {/* Earnings Summary */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <h3 className="text-lg font-bold mb-4">Earnings This Week</h3>
            <div className="flex justify-between items-center">
              <div>
                <p className="text-3xl font-bold text-[#2563EB]">$342</p>
                <p className="text-sm text-gray-500">This week</p>
              </div>
              <div className="text-right">
                <p className="text-lg font-semibold">$298</p>
                <p className="text-sm text-gray-500">Last week</p>
              </div>
              <div className="text-right">
                <p className="text-lg font-semibold">$48</p>
                <p className="text-sm text-gray-500">Average/day</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RiderDashboard;