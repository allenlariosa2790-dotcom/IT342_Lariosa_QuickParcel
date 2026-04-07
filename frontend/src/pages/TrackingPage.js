import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import api from '../services/api';

const TrackingPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [delivery, setDelivery] = useState(null);
  const [trackingHistory, setTrackingHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);

  useEffect(() => {
    // Get user from localStorage
    const userData = localStorage.getItem('user');
    if (userData) {
      try {
        setUser(JSON.parse(userData));
      } catch (e) {
        console.error('Failed to parse user', e);
      }
    }
    if (id) {
      fetchDeliveryAndHistory();
    } else {
      setError('No delivery ID provided');
      setLoading(false);
    }
  }, [id]);

  const fetchDeliveryAndHistory = async () => {
    try {
      console.log('Fetching delivery:', id);
      const [deliveryRes, historyRes] = await Promise.all([
        api.get(`/deliveries/${id}`),
        api.get(`/deliveries/${id}/track`)
      ]);
      console.log('Delivery response:', deliveryRes.data);
      console.log('History response:', historyRes.data);
      setDelivery(deliveryRes.data.data);
      setTrackingHistory(historyRes.data.data || []);
    } catch (err) {
      console.error('Error fetching tracking data:', err);
      setError(err.response?.data?.message || 'Failed to load delivery details');
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

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType={user?.userType || 'SENDER'} />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading tracking details...</p>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType={user?.userType || 'SENDER'} />
          <div className="flex-1 p-8">
            <div className="bg-red-100 text-red-700 p-4 rounded-lg">
              {error}
            </div>
            <button
              onClick={() => navigate(-1)}
              className="mt-4 text-[#2563EB] hover:underline"
            >
              ← Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (!delivery) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType={user?.userType || 'SENDER'} />
          <div className="flex-1 p-8">
            <p>Delivery not found.</p>
            <button
              onClick={() => navigate(-1)}
              className="mt-4 text-[#2563EB] hover:underline"
            >
              ← Go Back
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8">
          <div className="max-w-4xl mx-auto">
            {/* Back button */}
            <button
              onClick={() => navigate(-1)}
              className="text-[#2563EB] hover:underline mb-4 inline-block"
            >
              ← Back
            </button>

            {/* Header */}
            <div className="bg-white rounded-xl shadow-md p-6 mb-6">
              <h1 className="text-2xl font-bold">Tracking: {delivery.trackingNumber}</h1>
              <div className="mt-2 flex items-center gap-3 flex-wrap">
                <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(delivery.status)}`}>
                  {delivery.status}
                </span>
                <span className="text-gray-500 text-sm">
                  Last updated: {delivery.updatedAt ? new Date(delivery.updatedAt).toLocaleString() : (delivery.createdAt ? new Date(delivery.createdAt).toLocaleString() : 'N/A')}
                </span>
              </div>
            </div>

            {/* Delivery details */}
            <div className="bg-white rounded-xl shadow-md p-6 mb-6">
              <h2 className="text-lg font-bold mb-4">Delivery Details</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <p className="text-gray-500 text-sm">Pickup Address</p>
                  <p className="font-medium">{delivery.pickupAddress || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-gray-500 text-sm">Dropoff Address</p>
                  <p className="font-medium">{delivery.dropoffAddress || 'N/A'}</p>
                </div>
                {delivery.rider?.user && (
                  <div>
                    <p className="text-gray-500 text-sm">Rider</p>
                    <p className="font-medium">{delivery.rider.user.firstName} {delivery.rider.user.lastName}</p>
                  </div>
                )}
                {delivery.parcel && (
                  <>
                    <div>
                      <p className="text-gray-500 text-sm">Parcel</p>
                      <p className="font-medium">{delivery.parcel.name} ({delivery.parcel.size}, {delivery.parcel.weight} kg)</p>
                    </div>
                    <div>
                      <p className="text-gray-500 text-sm">Category</p>
                      <p className="font-medium">{delivery.parcel.category}</p>
                    </div>
                  </>
                )}
                {delivery.estimatedCost && (
                  <div>
                    <p className="text-gray-500 text-sm">Estimated Cost</p>
                    <p className="font-medium">₱{delivery.estimatedCost.toFixed(2)}</p>
                  </div>
                )}
                {delivery.notes && (
                  <div className="col-span-2">
                    <p className="text-gray-500 text-sm">Notes</p>
                    <p className="font-medium">{delivery.notes}</p>
                  </div>
                )}
              </div>
            </div>

            {/* Tracking history timeline */}
            <div className="bg-white rounded-xl shadow-md p-6 mb-6">
              <h2 className="text-lg font-bold mb-4">Tracking History</h2>
              {trackingHistory.length === 0 ? (
                <p className="text-gray-500">No tracking updates yet.</p>
              ) : (
                <div className="relative">
                  <div className="absolute left-4 top-0 bottom-0 w-0.5 bg-gray-200"></div>
                  <div className="space-y-6">
                    {trackingHistory.map((track, idx) => (
                      <div key={track.id || idx} className="relative pl-10">
                        <div className="absolute left-0 top-1 w-8 h-8 rounded-full bg-white border-2 border-[#2563EB] flex items-center justify-center">
                          <div className="w-2 h-2 bg-[#2563EB] rounded-full"></div>
                        </div>
                        <div>
                          <p className="font-semibold">{track.status}</p>
                          <p className="text-sm text-gray-500">
                            {track.location && `📍 ${track.location} • `}
                            {track.timestamp ? new Date(track.timestamp).toLocaleString() : 'N/A'}
                          </p>
                          {track.notes && <p className="text-sm text-gray-600 mt-1">{track.notes}</p>}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>

            {/* Map placeholder */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <h2 className="text-lg font-bold mb-4">Route Map</h2>
              <div className="bg-gray-100 h-64 rounded-lg flex items-center justify-center">
                <p className="text-gray-500">Map view will be available soon</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TrackingPage;