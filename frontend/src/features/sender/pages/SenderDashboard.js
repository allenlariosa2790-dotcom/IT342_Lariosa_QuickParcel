import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getMyDeliveries } from '../../tracking/services/trackingApi';

const SenderDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [deliveries, setDeliveries] = useState([]);
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    completed: 0,
    totalSpent: 0
  });

  useEffect(() => {
    // Get user data from localStorage
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    console.log('Token from storage:', token);
    console.log('User from storage:', userData);

    if (!token || !userData) {
      console.log('No token or user data, redirecting to login');
      navigate('/login');
      return;
    }

    try {
      const parsedUser = JSON.parse(userData);
      setUser(parsedUser);
      console.log('User set in state:', parsedUser);

      // Fetch deliveries after user is set
      fetchDeliveries();
    } catch (error) {
      console.error('Error parsing user data:', error);
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }, [navigate]);

  const fetchDeliveries = async () => {
    try {
      const response = await getMyDeliveries();
      console.log('Deliveries response:', response);
      const deliveriesData = response.data || [];
      setDeliveries(deliveriesData);

      // Calculate stats
      const activeStatuses = ['PENDING', 'ACCEPTED', 'PICKED_UP', 'IN_TRANSIT'];
      const total = deliveriesData.length;
      const active = deliveriesData.filter(d => activeStatuses.includes(d.status)).length;
      const completed = deliveriesData.filter(d => d.status === 'DELIVERED').length;
      const totalSpent = deliveriesData
        .filter(d => d.status === 'DELIVERED')
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      setStats({ total, active, completed, totalSpent });
    } catch (err) {
      console.error('Failed to fetch deliveries:', err);
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

  if (!user) {
    return null; // Will redirect via useEffect
  }

  // Get recent deliveries (last 3)
  const recentDeliveries = [...deliveries]
    .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    .slice(0, 3);

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="SENDER" />
        <div className="flex-1 p-8">
          {/* Welcome Banner */}
          <div className="bg-gradient-to-r from-[#2563EB] to-blue-700 text-white rounded-xl p-6 mb-8">
            <div className="flex justify-between items-center">
              <div>
                <h1 className="text-2xl font-bold">Welcome back, {user.firstName}!</h1>
                <p className="opacity-90 mt-1">Here's what's happening with your deliveries today</p>
              </div>
              <button onClick={() => navigate('/create-delivery')}
                className="bg-white text-[#2563EB] px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-all duration-300">
                + Create Delivery
              </button>
            </div>
          </div>

          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Total Deliveries</p>
              <p className="text-3xl font-bold text-[#2563EB] mt-2">{stats.total}</p>
              <p className="text-green-600 text-sm mt-2">{stats.total === 0 ? 'No deliveries yet' : `${stats.completed} completed`}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Active Deliveries</p>
              <p className="text-3xl font-bold text-[#2563EB] mt-2">{stats.active}</p>
              <p className="text-gray-500 text-sm mt-2">{stats.active === 0 ? 'No active deliveries' : 'In progress'}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Total Spent</p>
              <p className="text-2xl font-bold text-[#2563EB] mt-2">₱{stats.totalSpent.toFixed(2)}</p>
              <p className="text-gray-500 text-sm mt-2">Across {stats.completed} deliveries</p>
            </div>
          </div>

          {/* Recent Deliveries */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Recent Deliveries</h3>
            {recentDeliveries.length === 0 ? (
              <p className="text-gray-500 text-center py-4">No deliveries yet. Create your first delivery!</p>
            ) : (
              <div className="space-y-3">
                {recentDeliveries.map((delivery) => (
                  <div key={delivery.id} className="border-b pb-3 last:border-b-0">
                    <div className="flex justify-between items-start">
                      <div>
                        <p className="font-semibold text-[#2563EB]">{delivery.trackingNumber}</p>
                        <p className="text-sm text-gray-600">From: {delivery.pickupAddress?.substring(0, 40)}...</p>
                        <p className="text-sm text-gray-600">To: {delivery.dropoffAddress?.substring(0, 40)}...</p>
                      </div>
                      <div className="text-right">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                          delivery.status === 'DELIVERED' ? 'bg-green-100 text-green-800' :
                          delivery.status === 'CANCELLED' ? 'bg-red-100 text-red-800' :
                          'bg-yellow-100 text-yellow-800'
                        }`}>
                          {delivery.status}
                        </span>
                        <p className="text-sm font-semibold text-[#2563EB] mt-1">₱{delivery.estimatedCost?.toFixed(2)}</p>
                      </div>
                    </div>
                    <button
                      onClick={() => navigate(`/tracking/${delivery.id}`)}
                      className="text-[#2563EB] text-sm mt-2 hover:underline"
                    >
                      Track Delivery →
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-2 gap-4">
            <button
              onClick={() => navigate('/my-deliveries')}
              className="bg-[#2563EB] text-white px-4 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-all"
            >
              View My Deliveries
            </button>
            <button
              onClick={() => navigate('/profile')}
              className="bg-gray-600 text-white px-4 py-3 rounded-lg font-semibold hover:bg-gray-700 transition-all"
            >
              Profile
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SenderDashboard;