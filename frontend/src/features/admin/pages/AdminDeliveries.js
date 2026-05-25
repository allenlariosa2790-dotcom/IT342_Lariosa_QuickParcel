import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getAllDeliveries, cancelDelivery } from '../services/adminApi';

const AdminDeliveries = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [deliveries, setDeliveries] = useState([]);
  const [actionLoading, setActionLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [filter, setFilter] = useState('ALL');
  const [selectedDelivery, setSelectedDelivery] = useState(null);
  const [showDeliveryModal, setShowDeliveryModal] = useState(false);

  useEffect(() => {
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
    fetchDeliveries();
  }, [navigate]);

  const fetchDeliveries = async () => {
    setLoading(true);
    try {
      const response = await getAllDeliveries();
      setDeliveries(response.data || response);
    } catch (error) {
      console.error('Failed to fetch deliveries:', error);
      setMessage({ type: 'error', text: 'Failed to load deliveries' });
    } finally {
      setLoading(false);
    }
  };

  const handleCancelDelivery = async (deliveryId) => {
    if (!window.confirm('Are you sure you want to cancel this delivery?')) return;
    setActionLoading(true);
    try {
      await cancelDelivery(deliveryId);
      setMessage({ type: 'success', text: 'Delivery cancelled successfully' });
      fetchDeliveries();
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

  const getFilteredDeliveries = () => {
    if (filter === 'ALL') return deliveries;
    return deliveries.filter(d => d.status === filter);
  };

  const filteredDeliveries = getFilteredDeliveries();

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="ADMIN" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading deliveries...</p>
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
              <h1 className="text-2xl font-bold">Delivery Management</h1>
              <p className="text-gray-500 mt-1">View and manage all deliveries</p>
            </div>
            <button
              onClick={() => fetchDeliveries()}
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

          {/* Filter Tabs */}
          <div className="flex gap-2 mb-6 border-b">
            <button
              onClick={() => setFilter('ALL')}
              className={`px-4 py-2 font-medium transition-colors ${
                filter === 'ALL' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              All
            </button>
            <button
              onClick={() => setFilter('PENDING')}
              className={`px-4 py-2 font-medium transition-colors ${
                filter === 'PENDING' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Pending
            </button>
            <button
              onClick={() => setFilter('IN_TRANSIT')}
              className={`px-4 py-2 font-medium transition-colors ${
                filter === 'IN_TRANSIT' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              In Transit
            </button>
            <button
              onClick={() => setFilter('DELIVERED')}
              className={`px-4 py-2 font-medium transition-colors ${
                filter === 'DELIVERED' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Delivered
            </button>
            <button
              onClick={() => setFilter('CANCELLED')}
              className={`px-4 py-2 font-medium transition-colors ${
                filter === 'CANCELLED' ? 'text-[#2563EB] border-b-2 border-[#2563EB]' : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              Cancelled
            </button>
          </div>

          {/* Deliveries Table */}
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
                  {filteredDeliveries.map((delivery, idx) => (
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
                        <div className="flex gap-2 justify-center">
                          <button
                            onClick={() => {
                              setSelectedDelivery(delivery);
                              setShowDeliveryModal(true);
                            }}
                            className="bg-blue-500 text-white px-3 py-1 rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors"
                          >
                            View
                          </button>
                          <button
                            onClick={() => handleCancelDelivery(delivery.id)}
                            disabled={delivery.status === 'CANCELLED' || delivery.status === 'DELIVERED' || actionLoading}
                            className="bg-red-500 text-white px-3 py-1 rounded-lg text-sm font-medium hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
                          >
                            Cancel
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {filteredDeliveries.length === 0 && (
              <div className="text-center text-gray-500 py-8">No deliveries found</div>
            )}
          </div>
        </div>
      </div>

      {/* Delivery Detail Modal */}
      {showDeliveryModal && selectedDelivery && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" onClick={() => setShowDeliveryModal(false)}>
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[80vh] overflow-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-4 border-b sticky top-0 bg-white">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-bold">Delivery Details</h3>
                <button onClick={() => setShowDeliveryModal(false)} className="text-gray-500 hover:text-gray-700 text-2xl">&times;</button>
              </div>
            </div>
            <div className="p-4">
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-gray-500 text-sm">Tracking Number</p>
                    <p className="font-mono font-bold">{selectedDelivery.trackingNumber}</p>
                  </div>
                  <div>
                    <p className="text-gray-500 text-sm">Status</p>
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(selectedDelivery.status)}`}>
                      {selectedDelivery.status}
                    </span>
                  </div>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Sender</p>
                  <p className="font-medium">{selectedDelivery.sender?.user?.firstName} {selectedDelivery.sender?.user?.lastName}</p>
                  <p className="text-sm text-gray-500">{selectedDelivery.sender?.user?.email}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Rider</p>
                  <p className="font-medium">{selectedDelivery.rider?.user?.firstName || 'Unassigned'} {selectedDelivery.rider?.user?.lastName || ''}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Parcel</p>
                  <p>{selectedDelivery.parcel?.name} ({selectedDelivery.parcel?.size}, {selectedDelivery.parcel?.weight} kg)</p>
                  <p className="text-sm text-gray-500">Category: {selectedDelivery.parcel?.category}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Pickup Address</p>
                  <p>{selectedDelivery.pickupAddress}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Dropoff Address</p>
                  <p>{selectedDelivery.dropoffAddress}</p>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-gray-500 text-sm">Distance</p>
                    <p>{selectedDelivery.distance?.toFixed(2)} km</p>
                  </div>
                  <div>
                    <p className="text-gray-500 text-sm">Estimated Cost</p>
                    <p className="font-bold text-[#2563EB]">{formatCurrency(selectedDelivery.estimatedCost)}</p>
                  </div>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Payment</p>
                  <p>Method: {selectedDelivery.paymentMethod}</p>
                  <p>Status: <span className={selectedDelivery.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}>{selectedDelivery.paymentStatus}</span></p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Created</p>
                  <p>{new Date(selectedDelivery.createdAt).toLocaleString()}</p>
                </div>
                {selectedDelivery.notes && (
                  <div>
                    <p className="text-gray-500 text-sm">Notes</p>
                    <p className="italic">{selectedDelivery.notes}</p>
                  </div>
                )}
              </div>
            </div>
            <div className="p-4 border-t bg-gray-50">
              <button
                onClick={() => {
                  setShowDeliveryModal(false);
                  handleCancelDelivery(selectedDelivery.id);
                }}
                disabled={selectedDelivery.status === 'CANCELLED' || selectedDelivery.status === 'DELIVERED'}
                className="w-full bg-red-500 text-white py-2 rounded-lg font-medium hover:bg-red-600 disabled:opacity-50"
              >
                Cancel Delivery
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDeliveries;