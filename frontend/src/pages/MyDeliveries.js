import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';
import api from '../services/api';

const MyDeliveries = () => {
  const [deliveries, setDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) setUser(JSON.parse(userData));
    fetchDeliveries();
  }, []);

  const fetchDeliveries = async () => {
    try {
      const response = await api.get('/deliveries/my');
      setDeliveries(response.data.data);
    } catch (err) {
      setError('Failed to load deliveries');
    } finally {
      setLoading(false);
    }
  };

  // Helper to get status badge color
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

  if (loading) return <div className="p-8">Loading...</div>;
  if (error) return <div className="p-8 text-red-500">{error}</div>;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold mb-6">My Deliveries</h1>
          {deliveries.length === 0 ? (
            <p className="text-gray-500">No deliveries yet.</p>
          ) : (
            <div className="space-y-4">
              {deliveries.map((delivery) => (
                <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition-shadow">
                  <div className="flex justify-between items-start flex-wrap gap-2">
                    <div className="flex-1">
                      <p className="font-bold text-lg">{delivery.trackingNumber}</p>
                      <p className="text-sm text-gray-600 mt-1">
                        {delivery.pickupAddress} → {delivery.dropoffAddress}
                      </p>
                      <div className="flex flex-wrap gap-3 mt-2 text-sm">
                        <span className={`px-2 py-1 rounded-full text-xs font-semibold ${getStatusBadge(delivery.status)}`}>
                          {delivery.status}
                        </span>
                        {delivery.rider && (
                          <span className="text-gray-500">
                            Rider: {delivery.rider.user?.firstName} {delivery.rider.user?.lastName}
                          </span>
                        )}
                        <span className="text-gray-500">
                          Created: {new Date(delivery.createdAt).toLocaleDateString()}
                        </span>
                      </div>
                    </div>
                    <Link
                      to={`/tracking/${delivery.id}`}
                      className="bg-[#2563EB] text-white px-4 py-2 rounded-lg text-sm hover:bg-blue-700"
                    >
                      Track
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