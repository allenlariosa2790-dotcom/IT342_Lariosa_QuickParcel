import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';

const SenderDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

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
    } catch (error) {
      console.error('Error parsing user data:', error);
      navigate('/login');
    } finally {
      setLoading(false);
    }
  }, [navigate]);

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
              <button className="bg-white text-[#2563EB] px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-all duration-300">
                + Create Delivery
              </button>
            </div>
          </div>

          {/* Quick Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Total Deliveries</p>
              <p className="text-3xl font-bold text-[#2563EB] mt-2">0</p>
              <p className="text-green-600 text-sm mt-2">No deliveries yet</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Active Deliveries</p>
              <p className="text-3xl font-bold text-[#2563EB] mt-2">0</p>
              <p className="text-gray-500 text-sm mt-2">No active deliveries</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Account Info</p>
              <p className="text-lg font-semibold text-[#2563EB] mt-2">{user.email}</p>
              <p className="text-gray-500 text-sm mt-2">ID: {user.id}</p>
            </div>
          </div>

          {/* User Info Card */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <h3 className="text-lg font-bold mb-4">Your Information</h3>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-gray-500 text-sm">Name</p>
                <p className="font-medium">{user.firstName} {user.lastName}</p>
              </div>
              <div>
                <p className="text-gray-500 text-sm">Email</p>
                <p className="font-medium">{user.email}</p>
              </div>
              <div>
                <p className="text-gray-500 text-sm">User Type</p>
                <p className="font-medium">
                  <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                    {user.userType}
                  </span>
                </p>
              </div>
              <div>
                <p className="text-gray-500 text-sm">User ID</p>
                <p className="font-medium">{user.id}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SenderDashboard;