import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import { getAvailableDeliveries, acceptDelivery, getMyDeliveries, updateDeliveryStatus } from '../services/delivery';
import api from '../services/api'; // Import api for mark-paid endpoint

const RiderDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [availableDeliveries, setAvailableDeliveries] = useState([]);
  const [activeDeliveries, setActiveDeliveries] = useState([]);
  const [updating, setUpdating] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (!token || !userData) {
      navigate('/login');
      return;
    }
    setUser(JSON.parse(userData));
    fetchData();
  }, [navigate]);

  const fetchData = async () => {
    setLoading(true);
    try {
      await Promise.all([fetchAvailableDeliveries(), fetchActiveDeliveries()]);
    } catch (error) {
      console.error('Error fetching data', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchAvailableDeliveries = async () => {
    try {
      const response = await getAvailableDeliveries();
      let deliveries = [];
      if (Array.isArray(response.data)) {
        deliveries = response.data;
      } else if (response.data && Array.isArray(response.data.deliveries)) {
        deliveries = response.data.deliveries;
      } else if (response.data && Array.isArray(response.data.content)) {
        deliveries = response.data.content;
      } else {
        console.warn('Unexpected response structure:', response.data);
      }
      setAvailableDeliveries(deliveries);
    } catch (err) {
      console.error('Failed to fetch available deliveries:', err);
      setAvailableDeliveries([]);
    }
  };

  const fetchActiveDeliveries = async () => {
    try {
      const response = await getMyDeliveries();
      const deliveries = Array.isArray(response.data) ? response.data : [];
      // Active = in progress OR delivered but not yet paid
      const active = deliveries.filter(d =>
        ['ACCEPTED', 'PICKED_UP', 'IN_TRANSIT'].includes(d.status) ||
        (d.status === 'DELIVERED' && d.paymentStatus !== 'PAID')
      );
      setActiveDeliveries(active);
    } catch (err) {
      console.error('Failed to fetch active deliveries:', err);
      setActiveDeliveries([]);
    }
  };

  const handleAccept = async (deliveryId) => {
    try {
      await acceptDelivery(deliveryId);
      await Promise.all([fetchAvailableDeliveries(), fetchActiveDeliveries()]);
      alert('Delivery accepted!');
    } catch (error) {
      alert('Accept failed');
    }
  };

  const handleStatusUpdate = async (deliveryId, newStatus) => {
    setUpdating(true);
    try {
      await updateDeliveryStatus(deliveryId, newStatus, 'Current location');
      await fetchActiveDeliveries();
      await fetchAvailableDeliveries();
      alert(`Status updated to ${newStatus}`);
    } catch (error) {
      alert('Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  const markPaymentAsPaid = async (deliveryId) => {
    try {
      await api.put(`/deliveries/${deliveryId}/mark-paid`);
      alert('Payment marked as collected!');
      await fetchActiveDeliveries(); // refresh to update payment status
    } catch (err) {
      console.error('Failed to mark payment:', err);
      alert('Failed to update payment status');
    }
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
                <h1 className="text-2xl font-bold">Welcome back, {user?.firstName || 'Rider'}!</h1>
                <p className="opacity-90 mt-1">Ready to earn today?</p>
              </div>
              <div className="bg-white text-[#2563EB] px-4 py-2 rounded-lg font-semibold">
                Today's Earnings: $42
              </div>
            </div>
          </div>

          {/* Map placeholder */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Delivery Hotspots</h3>
            <div className="bg-gray-100 h-48 rounded-lg flex items-center justify-center">
              <p className="text-gray-500">Map showing available delivery hotspots</p>
            </div>
          </div>

          {/* Available Deliveries */}
          <div className="mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">Available Deliveries ({availableDeliveries.length})</h3>
              <button
                onClick={() => navigate('/available-deliveries')}
                className="text-[#2563EB] hover:underline"
              >
                View All
              </button>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {availableDeliveries.length === 0 ? (
                <p className="text-gray-500 col-span-3">No deliveries available at the moment.</p>
              ) : (
                availableDeliveries.slice(0, 3).map((delivery) => (
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
                ))
              )}
            </div>
          </div>

          {/* Active Deliveries (Multiple) */}
          {activeDeliveries.length > 0 && (
            <div className="mb-8">
              <h3 className="text-lg font-bold mb-4">Your Active Deliveries ({activeDeliveries.length})</h3>
              <div className="space-y-4">
                {activeDeliveries.map((delivery) => (
                  <div key={delivery.id} className="bg-white rounded-xl shadow-md p-6">
                    <div className="border-l-4 border-[#2563EB] pl-4">
                      <div className="flex justify-between items-start">
                        <div>
                          <div className="font-semibold text-lg">{delivery.trackingNumber}</div>
                          <div className="text-sm text-gray-600 mb-2">
                            {delivery.pickupAddress} → {delivery.dropoffAddress}
                          </div>
                          <div className="text-sm text-green-600 mb-2">Status: {delivery.status}</div>
                          {delivery.paymentMethod && (
                            <div className="text-sm text-gray-600">
                              Payment: {delivery.paymentMethod} -{' '}
                              <span className={delivery.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}>
                                {delivery.paymentStatus || 'PENDING'}
                              </span>
                            </div>
                          )}
                        </div>
                        <div className="text-right">
                          <div className="font-bold text-[#2563EB]">₱{delivery.estimatedCost?.toFixed(2)}</div>
                          <div className="text-xs text-gray-500">{delivery.distance?.toFixed(2)} km</div>
                        </div>
                      </div>
                      <div className="flex gap-2 mt-3 flex-wrap">
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'PICKED_UP')}
                          disabled={updating || delivery.status !== 'ACCEPTED'}
                          className="bg-yellow-500 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                        >
                          Picked Up
                        </button>
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'IN_TRANSIT')}
                          disabled={updating || delivery.status !== 'PICKED_UP'}
                          className="bg-blue-500 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                        >
                          In Transit
                        </button>
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'DELIVERED')}
                          disabled={updating || delivery.status !== 'IN_TRANSIT'}
                          className="bg-green-500 text-white px-3 py-1 rounded text-sm disabled:opacity-50"
                        >
                          Delivered
                        </button>
                        {/* COD Payment Button – only show if payment method is COD and not yet paid */}
                        {delivery.paymentMethod === 'COD' && delivery.paymentStatus !== 'PAID' && (
                          <button
                            onClick={() => markPaymentAsPaid(delivery.id)}
                            className="bg-purple-600 text-white px-3 py-1 rounded text-sm hover:bg-purple-700"
                          >
                            Confirm Payment Collected
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
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