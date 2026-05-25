import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getDeliveryById, getTrackingHistory, getParcelImage } from '../services/trackingApi';

const TrackingPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [delivery, setDelivery] = useState(null);
  const [trackingHistory, setTrackingHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);
  const [parcelImage, setParcelImage] = useState(null);
  const [hasImage, setHasImage] = useState(false);
  const [showImageModal, setShowImageModal] = useState(false);
  const [selectedImage, setSelectedImage] = useState(null);

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) setUser(JSON.parse(userData));
    if (id) {
      fetchDeliveryAndHistory();
    } else {
      setError('No delivery ID provided');
      setLoading(false);
    }
  }, [id]);

  const fetchParcelImage = async () => {
    try {
      const response = await getParcelImage(id);
      if (response.hasImage) {
        setParcelImage(`http://localhost:8080${response.imageUrl}?t=${Date.now()}`);
        setHasImage(true);
      }
    } catch (err) {
      console.error('Failed to fetch parcel image:', err);
    }
  };

  const fetchDeliveryAndHistory = async () => {
    try {
      const [deliveryRes, historyRes] = await Promise.all([
        getDeliveryById(id),
        getTrackingHistory(id)
      ]);
      setDelivery(deliveryRes.data);
      setTrackingHistory(historyRes.data || []);

      await fetchParcelImage();
    } catch (err) {
      setError('Failed to load delivery details');
    } finally {
      setLoading(false);
    }
  };

  const openImageModal = (imageUrl) => {
    setSelectedImage(imageUrl);
    setShowImageModal(true);
  };

  const closeImageModal = () => {
    setShowImageModal(false);
    setSelectedImage(null);
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
          <div className="max-w-5xl mx-auto">
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

                {/* Parcel Image - Clickable */}
                {hasImage && parcelImage && (
                  <div className="col-span-2">
                    <p className="text-gray-500 text-sm">Parcel Image</p>
                    <div className="mt-2">
                      <img
                        src={parcelImage}
                        alt="Parcel"
                        className="max-w-full max-h-48 rounded-lg border shadow-sm cursor-pointer hover:opacity-90 transition-opacity object-contain bg-gray-50"
                        onClick={() => openImageModal(parcelImage)}
                        onError={(e) => {
                          e.target.style.display = 'none';
                        }}
                      />
                      <p className="text-xs text-gray-400 mt-1">Click image to enlarge</p>
                    </div>
                  </div>
                )}

                {/* Payment Information */}
                {delivery.paymentMethod && (
                  <div>
                    <p className="text-gray-500 text-sm">Payment Method</p>
                    <p className="font-medium">{delivery.paymentMethod}</p>
                  </div>
                )}
                {delivery.paymentStatus && (
                  <div>
                    <p className="text-gray-500 text-sm">Payment Status</p>
                    <p className={`font-medium ${delivery.paymentStatus === 'PAID' ? 'text-green-600' : 'text-yellow-600'}`}>
                      {delivery.paymentStatus}
                    </p>
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

            {/* Route Information */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <h2 className="text-lg font-bold mb-4">Route Information</h2>

              {delivery.pickupLatitude && delivery.pickupLongitude && delivery.dropoffLatitude && delivery.dropoffLongitude ? (
                <div className="bg-blue-50 rounded-lg p-4">
                  <p className="text-gray-700 mb-3 text-center">📍 Get directions for this delivery:</p>
                  <div className="flex flex-wrap gap-3 justify-center">
                    <a
                      href={`https://www.google.com/maps/dir/${delivery.pickupLatitude},${delivery.pickupLongitude}/${delivery.dropoffLatitude},${delivery.dropoffLongitude}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="bg-green-600 text-white px-5 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
                    >
                      <span>📍</span> Google Maps
                    </a>
                    <a
                      href={`https://www.openstreetmap.org/directions?engine=osrm_car&route=${delivery.pickupLatitude},${delivery.pickupLongitude}&to=${delivery.dropoffLatitude},${delivery.dropoffLongitude}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="bg-[#2563EB] text-white px-5 py-2 rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
                    >
                      <span>🗺️</span> OpenStreetMap
                    </a>
                    <a
                      href={`https://waze.com/ul?ll=${delivery.pickupLatitude},${delivery.pickupLongitude}&navigate=yes`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="bg-blue-500 text-white px-5 py-2 rounded-lg hover:bg-blue-600 transition-colors flex items-center gap-2"
                    >
                      <span>🚗</span> Waze
                    </a>
                  </div>
                  <p className="text-xs text-gray-500 text-center mt-3">
                    Click any button to open directions in your preferred app
                  </p>
                </div>
              ) : (
                <div className="bg-gray-100 rounded-lg p-4 text-center">
                  <p className="text-gray-500">📍 Location coordinates not available for directions</p>
                  <p className="text-sm text-gray-400 mt-1">Coordinates will be available when using the location picker</p>
                </div>
              )}

              {/* Distance Info */}
              {delivery.distance && (
                <div className="mt-4 bg-gray-100 rounded-lg p-3 text-center">
                  <p className="text-sm text-gray-600">
                    📏 Total Distance: <span className="font-bold">{delivery.distance.toFixed(2)} km</span>
                  </p>
                  {delivery.estimatedCost && (
                    <p className="text-sm text-gray-600 mt-1">
                      💰 Estimated Cost: <span className="font-bold text-[#2563EB]">₱{delivery.estimatedCost.toFixed(2)}</span>
                    </p>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Image Modal */}
      {showImageModal && selectedImage && (
        <div
          className="fixed inset-0 bg-black bg-opacity-90 flex items-center justify-center z-50 p-4"
          onClick={closeImageModal}
        >
          <div className="relative max-w-4xl max-h-[90vh]">
            <button
              onClick={closeImageModal}
              className="absolute -top-12 right-0 text-white text-3xl hover:text-gray-300 transition-colors"
            >
              ✕
            </button>
            <img
              src={selectedImage}
              alt="Parcel enlarged"
              className="max-w-full max-h-[90vh] object-contain rounded-lg"
              onClick={(e) => e.stopPropagation()}
            />
            <p className="text-center text-white text-sm mt-4">
              Click outside to close • {delivery?.trackingNumber}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default TrackingPage;