import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import apiClient from '../../shared/utils/apiClient';
import { getAvailableDeliveries, acceptDelivery, updateDeliveryStatus } from '../services/riderApi';
import { getMyDeliveries } from '../../tracking/services/trackingApi';

const RiderDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [availableDeliveries, setAvailableDeliveries] = useState([]);
  const [activeDeliveries, setActiveDeliveries] = useState([]);
  const [updating, setUpdating] = useState(false);

  const [earnings, setEarnings] = useState({
    today: 0,
    thisWeek: 0,
    lastWeek: 0,
    total: 0,
    completedCount: 0,
  });

  // Weekly earnings data for chart
  const [weeklyData, setWeeklyData] = useState([
    { day: 'Mon', earnings: 0, deliveries: 0 },
    { day: 'Tue', earnings: 0, deliveries: 0 },
    { day: 'Wed', earnings: 0, deliveries: 0 },
    { day: 'Thu', earnings: 0, deliveries: 0 },
    { day: 'Fri', earnings: 0, deliveries: 0 },
    { day: 'Sat', earnings: 0, deliveries: 0 },
    { day: 'Sun', earnings: 0, deliveries: 0 },
  ]);

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
      await Promise.all([fetchAvailableDeliveries(), fetchActiveDeliveries(), fetchEarnings(), fetchWeeklyStats()]);
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

  const fetchEarnings = async () => {
    try {
      const response = await getMyDeliveries();
      const deliveries = Array.isArray(response.data) ? response.data : [];

      const completed = deliveries.filter(d => d.status === 'DELIVERED');
      const totalEarnings = completed.reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const todayEarnings = completed
        .filter(d => new Date(d.deliveredTime || d.updatedAt || d.createdAt) >= today)
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      const weekEarnings = completed
        .filter(d => new Date(d.deliveredTime || d.updatedAt || d.createdAt) >= weekAgo)
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      const twoWeeksAgo = new Date();
      twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14);
      const lastWeekEarnings = completed
        .filter(d => {
          const date = new Date(d.deliveredTime || d.updatedAt || d.createdAt);
          return date >= twoWeeksAgo && date < weekAgo;
        })
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      setEarnings({
        today: todayEarnings,
        thisWeek: weekEarnings,
        lastWeek: lastWeekEarnings,
        total: totalEarnings,
        completedCount: completed.length,
      });
    } catch (err) {
      console.error('Failed to fetch earnings:', err);
    }
  };

  const fetchWeeklyStats = async () => {
    try {
      const response = await getMyDeliveries();
      const deliveries = Array.isArray(response.data) ? response.data : [];
      const completed = deliveries.filter(d => d.status === 'DELIVERED');

      const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      const weeklyTotals = {};
      dayNames.forEach(day => { weeklyTotals[day] = { earnings: 0, deliveries: 0 }; });

      const oneWeekAgo = new Date();
      oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);

      completed.forEach(delivery => {
        const date = new Date(delivery.deliveredTime || delivery.updatedAt || delivery.createdAt);
        if (date >= oneWeekAgo) {
          const dayName = dayNames[date.getDay()];
          weeklyTotals[dayName].earnings += delivery.estimatedCost || 0;
          weeklyTotals[dayName].deliveries += 1;
        }
      });

      const orderedDays = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      const chartData = orderedDays.map(day => ({
        day,
        earnings: weeklyTotals[day]?.earnings || 0,
        deliveries: weeklyTotals[day]?.deliveries || 0,
      }));

      setWeeklyData(chartData);
    } catch (err) {
      console.error('Failed to fetch weekly stats:', err);
    }
  };

  const handleAccept = async (deliveryId) => {
    try {
      await acceptDelivery(deliveryId);
      await Promise.all([fetchAvailableDeliveries(), fetchActiveDeliveries()]);
      alert('✅ Delivery accepted!');
    } catch (error) {
      alert('❌ Accept failed');
    }
  };

  const handleStatusUpdate = async (deliveryId, newStatus) => {
    setUpdating(true);
    try {
      await updateDeliveryStatus(deliveryId, newStatus, 'Current location');
      await Promise.all([fetchActiveDeliveries(), fetchAvailableDeliveries(), fetchEarnings(), fetchWeeklyStats()]);
      alert(`✅ Status updated to ${newStatus}`);
    } catch (error) {
      alert('❌ Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  const markPaymentAsPaid = async (deliveryId) => {
    try {
      await apiClient.put(`/deliveries/${deliveryId}/mark-paid`);
      alert('💰 Payment marked as collected!');
      await fetchActiveDeliveries();
    } catch (err) {
      console.error('Failed to mark payment:', err);
      alert('❌ Failed to update payment status');
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);
  };

  // Find max earnings for chart scaling
  const maxEarnings = Math.max(...weeklyData.map(d => d.earnings), 100);

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
            <div className="flex justify-between items-center flex-wrap gap-4">
              <div>
                <h1 className="text-2xl font-bold">Welcome back, {user?.firstName || 'Rider'}!</h1>
                <p className="opacity-90 mt-1">Ready to earn today?</p>
              </div>
              <div className="bg-white text-[#2563EB] px-4 py-2 rounded-lg font-semibold">
                {formatCurrency(earnings.today)} Today
              </div>
            </div>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-8">
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-[#2563EB]">{earnings.completedCount}</p>
              <p className="text-sm text-gray-500">Completed Deliveries</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-green-600">{formatCurrency(earnings.thisWeek)}</p>
              <p className="text-sm text-gray-500">This Week</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-blue-600">{formatCurrency(earnings.lastWeek)}</p>
              <p className="text-sm text-gray-500">Last Week</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-[#2563EB]">{formatCurrency(earnings.total)}</p>
              <p className="text-sm text-gray-500">Total Earnings</p>
            </div>
          </div>

          {/* Weekly Earnings Chart - Replaces the map placeholder */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold">Weekly Earnings Overview</h3>
              <div className="flex gap-4 text-xs">
                <div className="flex items-center gap-1">
                  <div className="w-3 h-3 bg-[#2563EB] rounded"></div>
                  <span>Earnings (₱)</span>
                </div>
                <div className="flex items-center gap-1">
                  <div className="w-3 h-3 bg-green-500 rounded"></div>
                  <span>Deliveries</span>
                </div>
              </div>
            </div>

            {/* Bar Chart */}
            <div className="h-64">
              <div className="flex h-full items-end gap-2">
                {weeklyData.map((day, idx) => (
                  <div key={idx} className="flex-1 flex flex-col items-center gap-2">
                    <div className="relative w-full flex flex-col items-center">
                      {/* Earnings Bar */}
                      <div
                        className="w-full bg-[#2563EB] rounded-t transition-all duration-500 hover:bg-blue-600"
                        style={{
                          height: `${(day.earnings / maxEarnings) * 150}px`,
                          minHeight: day.earnings > 0 ? '4px' : '0px'
                        }}
                      >
                        <div className="absolute -top-6 left-1/2 transform -translate-x-1/2 text-xs font-semibold text-[#2563EB] whitespace-nowrap">
                          {day.earnings > 0 ? formatCurrency(day.earnings) : ''}
                        </div>
                      </div>
                      {/* Deliveries indicator dot */}
                      {day.deliveries > 0 && (
                        <div className="absolute -bottom-4 w-2 h-2 bg-green-500 rounded-full"></div>
                      )}
                    </div>
                    <div className="text-xs text-gray-500 mt-4">{day.day}</div>
                    {day.deliveries > 0 && (
                      <div className="text-xs text-green-600">{day.deliveries} delivery{day.deliveries !== 1 ? 's' : ''}</div>
                    )}
                  </div>
                ))}
              </div>
            </div>

            {weeklyData.every(d => d.earnings === 0) && (
              <div className="text-center text-gray-500 py-8">
                <p>No earnings data for this week yet.</p>
                <p className="text-sm mt-1">Complete deliveries to see your earnings chart!</p>
              </div>
            )}
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
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {availableDeliveries.length === 0 ? (
                <div className="bg-white rounded-xl shadow-md p-8 text-center text-gray-500 col-span-full">
                  <div className="text-4xl mb-2">🚗</div>
                  <p>No deliveries available at the moment.</p>
                  <p className="text-sm mt-1">Check back later for new deliveries.</p>
                </div>
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
                      <div className="truncate">From: {delivery.pickupAddress}</div>
                      <div className="truncate">To: {delivery.dropoffAddress}</div>
                    </div>
                    <div className="flex justify-between text-sm mb-3">
                      <span>📍 {delivery.distance ? `${delivery.distance.toFixed(2)} km` : 'Distance pending'}</span>
                      <span className="font-semibold text-[#2563EB]">
                        {formatCurrency(delivery.estimatedCost)}
                      </span>
                    </div>
                    <button
                      onClick={() => handleAccept(delivery.id)}
                      className="w-full bg-[#2563EB] text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
                    >
                      Accept Delivery
                    </button>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Active Deliveries */}
          {activeDeliveries.length > 0 && (
            <div className="mb-8">
              <h3 className="text-lg font-bold mb-4">Your Active Deliveries ({activeDeliveries.length})</h3>
              <div className="space-y-4">
                {activeDeliveries.map((delivery) => (
                  <div key={delivery.id} className="bg-white rounded-xl shadow-md p-6 hover:shadow-lg transition-shadow">
                    <div className="border-l-4 border-[#2563EB] pl-4">
                      <div className="flex justify-between items-start flex-wrap gap-4">
                        <div className="flex-1">
                          <div className="font-semibold text-lg">{delivery.trackingNumber}</div>
                          <div className="text-sm text-gray-600 mb-2">
                            <div>From: {delivery.pickupAddress}</div>
                            <div>To: {delivery.dropoffAddress}</div>
                          </div>
                          <div className="flex flex-wrap gap-2 mt-2">
                            <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                              delivery.status === 'ACCEPTED' ? 'bg-blue-100 text-blue-800' :
                              delivery.status === 'PICKED_UP' ? 'bg-purple-100 text-purple-800' :
                              delivery.status === 'IN_TRANSIT' ? 'bg-indigo-100 text-indigo-800' :
                              delivery.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                              'bg-gray-100 text-gray-800'
                            }`}>
                              {delivery.status}
                            </span>
                            {delivery.paymentMethod && (
                              <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                                delivery.paymentStatus === 'PAID' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                              }`}>
                                {delivery.paymentMethod} - {delivery.paymentStatus || 'PENDING'}
                              </span>
                            )}
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="font-bold text-xl text-[#2563EB]">{formatCurrency(delivery.estimatedCost)}</div>
                          <div className="text-xs text-gray-500">{delivery.distance?.toFixed(2)} km</div>
                        </div>
                      </div>
                      <div className="flex gap-2 mt-4 flex-wrap">
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'PICKED_UP')}
                          disabled={updating || delivery.status !== 'ACCEPTED'}
                          className="bg-yellow-500 text-white px-3 py-1.5 rounded text-sm disabled:opacity-50 hover:bg-yellow-600 transition-colors"
                        >
                          📦 Picked Up
                        </button>
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'IN_TRANSIT')}
                          disabled={updating || delivery.status !== 'PICKED_UP'}
                          className="bg-blue-500 text-white px-3 py-1.5 rounded text-sm disabled:opacity-50 hover:bg-blue-600 transition-colors"
                        >
                          🚚 In Transit
                        </button>
                        <button
                          onClick={() => handleStatusUpdate(delivery.id, 'DELIVERED')}
                          disabled={updating || delivery.status !== 'IN_TRANSIT'}
                          className="bg-green-500 text-white px-3 py-1.5 rounded text-sm disabled:opacity-50 hover:bg-green-600 transition-colors"
                        >
                          ✅ Delivered
                        </button>
                        {delivery.paymentMethod === 'COD' && delivery.paymentStatus !== 'PAID' && (
                          <button
                            onClick={() => markPaymentAsPaid(delivery.id)}
                            className="bg-purple-600 text-white px-3 py-1.5 rounded text-sm hover:bg-purple-700 transition-colors"
                          >
                            💰 Confirm Payment
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default RiderDashboard;