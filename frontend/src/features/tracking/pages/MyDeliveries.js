import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getMyDeliveries } from '../services/trackingApi';

const MyDeliveries = () => {
  const [deliveries, setDeliveries] = useState([]);
  const [filteredDeliveries, setFilteredDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);

  // Sorting state
  const [sortBy, setSortBy] = useState('createdAt'); // createdAt, status, trackingNumber, estimatedCost
  const [sortOrder, setSortOrder] = useState('desc'); // asc, desc

  // Filter state
  const [statusFilter, setStatusFilter] = useState('ALL'); // ALL, PENDING, ACCEPTED, PICKED_UP, IN_TRANSIT, DELIVERED

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) setUser(JSON.parse(userData));
    fetchDeliveries();
  }, []);

  useEffect(() => {
    // Apply sorting and filtering whenever deliveries, sortBy, sortOrder, or statusFilter changes
    let result = [...deliveries];

    // Apply status filter
    if (statusFilter !== 'ALL') {
      result = result.filter(d => d.status === statusFilter);
    }

    // Apply sorting
    result.sort((a, b) => {
      let aVal, bVal;

      switch (sortBy) {
        case 'createdAt':
          aVal = new Date(a.createdAt);
          bVal = new Date(b.createdAt);
          break;
        case 'estimatedCost':
          aVal = a.estimatedCost || 0;
          bVal = b.estimatedCost || 0;
          break;
        case 'trackingNumber':
          aVal = a.trackingNumber || '';
          bVal = b.trackingNumber || '';
          break;
        case 'status':
          const statusOrder = { 'PENDING': 1, 'ACCEPTED': 2, 'PICKED_UP': 3, 'IN_TRANSIT': 4, 'DELIVERED': 5, 'CANCELLED': 6 };
          aVal = statusOrder[a.status] || 99;
          bVal = statusOrder[b.status] || 99;
          break;
        default:
          aVal = new Date(a.createdAt);
          bVal = new Date(b.createdAt);
      }

      if (sortOrder === 'asc') {
        return aVal > bVal ? 1 : -1;
      } else {
        return aVal < bVal ? 1 : -1;
      }
    });

    setFilteredDeliveries(result);
  }, [deliveries, sortBy, sortOrder, statusFilter]);

  const fetchDeliveries = async () => {
    try {
      const response = await getMyDeliveries();
      setDeliveries(response.data);
    } catch (err) {
      setError('Failed to load deliveries');
      console.error(err);
    } finally {
      setLoading(false);
    }
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

  const handleSort = (field) => {
    if (sortBy === field) {
      // Toggle order if same field
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      // New field, default to descending
      setSortBy(field);
      setSortOrder('desc');
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) return '↕️';
    return sortOrder === 'asc' ? '↑' : '↓';
  };

  // Statistics
  const stats = {
    total: deliveries.length,
    active: deliveries.filter(d => !['DELIVERED', 'CANCELLED'].includes(d.status)).length,
    completed: deliveries.filter(d => d.status === 'DELIVERED').length,
    cancelled: deliveries.filter(d => d.status === 'CANCELLED').length,
  };

  if (loading) return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8 flex justify-center items-center">
          <div className="text-center">
            <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
            <p>Loading deliveries...</p>
          </div>
        </div>
      </div>
    </div>
  );

  if (error) return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8 text-red-500">{error}</div>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-2xl font-bold">My Deliveries</h1>
              <p className="text-gray-500 mt-1">Track and manage your deliveries</p>
            </div>
            <Link
              to="/create-delivery"
              className="bg-[#2563EB] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
            >
              + New Delivery
            </Link>
          </div>

          {/* Stats Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-[#2563EB]">{stats.total}</p>
              <p className="text-sm text-gray-500">Total Deliveries</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-yellow-600">{stats.active}</p>
              <p className="text-sm text-gray-500">Active</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-green-600">{stats.completed}</p>
              <p className="text-sm text-gray-500">Completed</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-2xl font-bold text-red-600">{stats.cancelled}</p>
              <p className="text-sm text-gray-500">Cancelled</p>
            </div>
          </div>

          {/* Filters and Sorting */}
          <div className="bg-white rounded-xl shadow-md p-4 mb-6">
            <div className="flex flex-wrap justify-between items-center gap-4">
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => setStatusFilter('ALL')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'ALL' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  All
                </button>
                <button
                  onClick={() => setStatusFilter('PENDING')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'PENDING' ? 'bg-yellow-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  Pending
                </button>
                <button
                  onClick={() => setStatusFilter('ACCEPTED')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'ACCEPTED' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  Accepted
                </button>
                <button
                  onClick={() => setStatusFilter('PICKED_UP')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'PICKED_UP' ? 'bg-purple-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  Picked Up
                </button>
                <button
                  onClick={() => setStatusFilter('IN_TRANSIT')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'IN_TRANSIT' ? 'bg-indigo-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  In Transit
                </button>
                <button
                  onClick={() => setStatusFilter('DELIVERED')}
                  className={`px-3 py-1 rounded-full text-sm font-medium transition-colors ${
                    statusFilter === 'DELIVERED' ? 'bg-green-500 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                  }`}
                >
                  Delivered
                </button>
              </div>
            </div>

            {/* Sort options */}
            <div className="flex flex-wrap gap-4 mt-4 pt-3 border-t">
              <span className="text-sm text-gray-500">Sort by:</span>
              <button
                onClick={() => handleSort('createdAt')}
                className="text-sm text-gray-700 hover:text-[#2563EB]"
              >
                Date {getSortIcon('createdAt')}
              </button>
              <button
                onClick={() => handleSort('trackingNumber')}
                className="text-sm text-gray-700 hover:text-[#2563EB]"
              >
                Tracking # {getSortIcon('trackingNumber')}
              </button>
              <button
                onClick={() => handleSort('status')}
                className="text-sm text-gray-700 hover:text-[#2563EB]"
              >
                Status {getSortIcon('status')}
              </button>
              <button
                onClick={() => handleSort('estimatedCost')}
                className="text-sm text-gray-700 hover:text-[#2563EB]"
              >
                Amount {getSortIcon('estimatedCost')}
              </button>
            </div>
          </div>

          {filteredDeliveries.length === 0 ? (
            <div className="bg-white rounded-xl shadow-md p-12 text-center text-gray-500">
              <div className="text-5xl mb-4">📦</div>
              <p className="text-lg">No deliveries found.</p>
              <p className="text-sm mt-2">
                {deliveries.length === 0 ? 'Create your first delivery to get started!' : 'Try changing your filters.'}
              </p>
              {deliveries.length === 0 && (
                <Link
                  to="/create-delivery"
                  className="inline-block mt-4 bg-[#2563EB] text-white px-6 py-2 rounded-lg hover:bg-blue-700"
                >
                  Create Delivery
                </Link>
              )}
            </div>
          ) : (
            <div className="space-y-4">
              {filteredDeliveries.map((delivery) => (
                <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition-shadow">
                  <div className="flex justify-between items-start flex-wrap gap-2">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-bold text-lg">{delivery.trackingNumber}</p>
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(delivery.status)}`}>
                          {delivery.status}
                        </span>
                      </div>
                      <p className="text-sm text-gray-600 mt-2">
                        📍 From: {delivery.pickupAddress?.substring(0, 60)}...
                      </p>
                      <p className="text-sm text-gray-600">
                        🏁 To: {delivery.dropoffAddress?.substring(0, 60)}...
                      </p>
                      <div className="flex flex-wrap gap-3 mt-2 text-sm">
                        {delivery.rider && (
                          <span className="text-gray-500">
                            Rider: {delivery.rider.user?.firstName} {delivery.rider.user?.lastName}
                          </span>
                        )}
                        <span className="text-gray-500">
                          Created: {new Date(delivery.createdAt).toLocaleDateString()}
                        </span>
                        {delivery.estimatedCost && (
                          <span className="font-semibold text-[#2563EB]">
                            ₱{delivery.estimatedCost.toFixed(2)}
                          </span>
                        )}
                      </div>
                    </div>
                    <Link
                      to={`/tracking/${delivery.id}`}
                      className="bg-[#2563EB] text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700 transition-colors whitespace-nowrap"
                    >
                      Track Delivery →
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyDeliveries;