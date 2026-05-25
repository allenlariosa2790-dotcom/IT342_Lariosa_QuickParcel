import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getAllUsers, getAllDeliveries, getDashboardStats, updateUserStatus, cancelDelivery } from '../services/adminApi';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('overview'); // overview, users, deliveries
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalSenders: 0,
    totalRiders: 0,
    totalDeliveries: 0,
    pendingDeliveries: 0,
    completedDeliveries: 0,
    totalEarnings: 0,
    activeRiders: 0,
  });
  const [users, setUsers] = useState([]);
  const [deliveries, setDeliveries] = useState([]);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    // Check if user is admin
    const userData = localStorage.getItem('user');
    if (userData) {
      const user = JSON.parse(userData);
      if (user.userType !== 'ADMIN') {
        navigate('/login');
        return;
      }
    } else {
      navigate('/login');
      return;
    }
    fetchAllData();
  }, [navigate]); // Added navigate to dependency array

  const fetchAllData = async () => {
    setLoading(true);
    try {
      const [statsRes, usersRes, deliveriesRes] = await Promise.all([
        getDashboardStats(),
        getAllUsers(),
        getAllDeliveries(),
      ]);

      console.log('Stats response:', statsRes);
      console.log('Users response:', usersRes);
      console.log('Deliveries response:', deliveriesRes);

      // Handle different response structures
      setStats(statsRes.data || statsRes);
      setUsers(usersRes.data || usersRes);
      setDeliveries(deliveriesRes.data || deliveriesRes);
    } catch (error) {
      console.error('Failed to fetch admin data:', error);
      setMessage({ type: 'error', text: 'Failed to load dashboard data: ' + (error.response?.data?.error || error.message) });
    } finally {
      setLoading(false);
    }
  };

  const handleUserStatusToggle = async (userId, currentStatus) => {
    setActionLoading(true);
    try {
      await updateUserStatus(userId, !currentStatus);
      setMessage({ type: 'success', text: `User ${!currentStatus ? 'activated' : 'suspended'} successfully` });
      fetchAllData(); // Refresh data
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to update user status' });
    } finally {
      setActionLoading(false);
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    }
  };

  const handleCancelDelivery = async (deliveryId) => {
    if (!window.confirm('Are you sure you want to cancel this delivery?')) return;
    setActionLoading(true);
    try {
      await cancelDelivery(deliveryId);
      setMessage({ type: 'success', text: 'Delivery cancelled successfully' });
      fetchAllData();
    } catch (error) {
      setMessage({ type: 'error', text: 'Failed to cancel delivery' });
    } finally {
      setActionLoading(false);
      setTimeout(() => setMessage({ type: '', text: '' }), 3000);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      case 'ACCEPTED': return 'bg-blue-100 text-blue-800';
      case 'PICKED_UP': return 'bg-purple-100 text-purple-800';
      case 'IN_TRANSIT': return 'bg-indigo-100 text-indigo-800';
      case 'DELIVERED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="ADMIN" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading admin dashboard...</p>
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
        <Sidebar userType="ADMIN" />
        <div className="flex-1 p-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-2xl font-bold">Admin Dashboard</h1>
              <p className="text-gray-500 mt-1">Manage users, deliveries, and system overview</p>
            </div>
            <button
              onClick={() => fetchAllData()}
              className="bg-[#2563EB] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              🔄 Refresh
            </button>
          </div>

          {/* Message Alert */}
          {message.text && (
            <div className={`mb-4 p-3 rounded-lg ${
              message.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}>
              {message.text}
            </div>
          )}

          {/* Tab Navigation */}
          <div className="flex gap-2 mb-6 border-b">
            <button
              onClick={() => setActiveTab('overview')}
              className={`px-4 py-2 font-medium transition-colors ${
                activeTab === 'overview' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              📊 Overview
            </button>
            <button
              onClick={() => setActiveTab('users')}
              className={`px-4 py-2 font-medium transition-colors ${
                activeTab === 'users' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              👥 Users
            </button>
            <button
              onClick={() => setActiveTab('deliveries')}
              className={`px-4 py-2 font-medium transition-colors ${
                activeTab === 'deliveries' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              📦 Deliveries
            </button>
          </div>

          {/* Overview Tab */}
          {activeTab === 'overview' && (
            <>
              {/* Stats Cards */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                <div className="bg-white rounded-xl shadow-md p-4">
                  <p className="text-gray-500 text-sm">Total Users</p>
                  <p className="text-3xl font-bold text-[#2563EB]">{stats.totalUsers}</p>
                  <div className="flex gap-2 mt-2 text-xs">
                    <span className="text-green-600">Senders: {stats.totalSenders}</span>
                    <span className="text-blue-600">Riders: {stats.totalRiders}</span>
                  </div>
                </div>
                <div className="bg-white rounded-xl shadow-md p-4">
                  <p className="text-gray-500 text-sm">Total Deliveries</p>
                  <p className="text-3xl font-bold text-[#2563EB]">{stats.totalDeliveries}</p>
                  <div className="flex gap-2 mt-2 text-xs">
                    <span className="text-yellow-600">Pending: {stats.pendingDeliveries}</span>
                    <span className="text-green-600">Completed: {stats.completedDeliveries}</span>
                  </div>
                </div>
                <div className="bg-white rounded-xl shadow-md p-4">
                  <p className="text-gray-500 text-sm">Total Earnings</p>
                  <p className="text-3xl font-bold text-green-600">{formatCurrency(stats.totalEarnings)}</p>
                  <p className="text-xs text-gray-500 mt-2">From completed deliveries</p>
                </div>
                <div className="bg-white rounded-xl shadow-md p-4">
                  <p className="text-gray-500 text-sm">Active Riders</p>
                  <p className="text-3xl font-bold text-blue-600">{stats.activeRiders}</p>
                  <p className="text-xs text-gray-500 mt-2">Out of {stats.totalRiders} total</p>
                </div>
              </div>

              {/* Recent Deliveries Preview */}
              <div className="bg-white rounded-xl shadow-md p-6">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-bold">Recent Deliveries</h3>
                  <button
                    onClick={() => setActiveTab('deliveries')}
                    className="text-[#2563EB] hover:underline text-sm"
                  >
                    View All →
                  </button>
                </div>
                <div className="space-y-3">
                  {deliveries.slice(0, 5).map((delivery) => (
                    <div key={delivery.id} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <div>
                        <p className="font-mono text-sm font-semibold">{delivery.trackingNumber}</p>
                        <p className="text-xs text-gray-500 mt-1">
                          {delivery.pickupAddress?.split(',')[0]} → {delivery.dropoffAddress?.split(',')[0]}
                        </p>
                      </div>
                      <div className="text-right">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(delivery.status)}`}>
                          {delivery.status}
                        </span>
                        <p className="text-sm font-semibold text-[#2563EB] mt-1">{formatCurrency(delivery.estimatedCost)}</p>
                      </div>
                    </div>
                  ))}
                  {deliveries.length === 0 && (
                    <p className="text-center text-gray-500 py-4">No deliveries yet</p>
                  )}
                </div>
              </div>
            </>
          )}

          {/* Users Tab */}
          {activeTab === 'users' && (
            <div className="bg-white rounded-xl shadow-md overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b">
                    <tr>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">ID</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Name</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Email</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Role</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Status</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Joined</th>
                      <th className="text-center p-4 text-sm font-semibold text-gray-600">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map((user, idx) => (
                      <tr key={user.id} className={`border-b hover:bg-gray-50 ${idx % 2 === 0 ? 'bg-white' : 'bg-gray-50'}`}>
                        <td className="p-4 text-sm font-mono">{user.id}</td>
                        <td className="p-4 text-sm">{user.firstName} {user.lastName}</td>
                        <td className="p-4 text-sm">{user.email}</td>
                        <td className="p-4 text-sm">
                          <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                            user.userType === 'SENDER' ? 'bg-green-100 text-green-800' :
                            user.userType === 'RIDER' ? 'bg-blue-100 text-blue-800' :
                            'bg-purple-100 text-purple-800'
                          }`}>
                            {user.userType}
                          </span>
                        </td>
                        <td className="p-4 text-sm">
                          <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                            user.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                          }`}>
                            {user.active ? 'Active' : 'Suspended'}
                          </span>
                        </td>
                        <td className="p-4 text-sm">{new Date(user.createdAt).toLocaleDateString()}</td>
                        <td className="p-4 text-center">
                          <button
                            onClick={() => handleUserStatusToggle(user.id, user.active)}
                            disabled={actionLoading}
                            className={`px-3 py-1 rounded-lg text-sm font-medium transition-colors ${
                              user.active
                                ? 'bg-red-500 text-white hover:bg-red-600'
                                : 'bg-green-500 text-white hover:bg-green-600'
                            } disabled:opacity-50`}
                          >
                            {user.active ? 'Suspend' : 'Activate'}
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {users.length === 0 && (
                <div className="text-center text-gray-500 py-8">No users found</div>
              )}
            </div>
          )}

          {/* Deliveries Tab */}
          {activeTab === 'deliveries' && (
            <div className="bg-white rounded-xl shadow-md overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b">
                    <tr>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Tracking #</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Sender</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Rider</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Status</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Amount</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Date</th>
                      <th className="text-center p-4 text-sm font-semibold text-gray-600">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {deliveries.map((delivery, idx) => (
                      <tr key={delivery.id} className={`border-b hover:bg-gray-50 ${idx % 2 === 0 ? 'bg-white' : 'bg-gray-50'}`}>
                        <td className="p-4 text-sm font-mono">{delivery.trackingNumber}</td>
                        <td className="p-4 text-sm">{delivery.sender?.user?.firstName} {delivery.sender?.user?.lastName}</td>
                        <td className="p-4 text-sm">{delivery.rider?.user?.firstName || 'Unassigned'} {delivery.rider?.user?.lastName || ''}</td>
                        <td className="p-4 text-sm">
                          <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(delivery.status)}`}>
                            {delivery.status}
                          </span>
                        </td>
                        <td className="p-4 text-sm font-semibold text-[#2563EB]">{formatCurrency(delivery.estimatedCost)}</td>
                        <td className="p-4 text-sm">{new Date(delivery.createdAt).toLocaleDateString()}</td>
                        <td className="p-4 text-center">
                          <button
                            onClick={() => handleCancelDelivery(delivery.id)}
                            disabled={delivery.status === 'CANCELLED' || delivery.status === 'DELIVERED' || actionLoading}
                            className="bg-red-500 text-white px-3 py-1 rounded-lg text-sm font-medium hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            Cancel
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              {deliveries.length === 0 && (
                <div className="text-center text-gray-500 py-8">No deliveries found</div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;