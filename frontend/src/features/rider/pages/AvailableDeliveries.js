import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getAvailableDeliveries, acceptDelivery } from '../services/riderApi';

const AvailableDeliveries = () => {
  const navigate = useNavigate();
  const [deliveries, setDeliveries] = useState([]);
  const [filteredDeliveries, setFilteredDeliveries] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [sortBy, setSortBy] = useState('distance'); // distance, earnings, size, createdAt
  const [sortOrder, setSortOrder] = useState('asc');
  const [riderLocation, setRiderLocation] = useState(null);
  const [locationLoading, setLocationLoading] = useState(false);
  const [selectedDelivery, setSelectedDelivery] = useState(null);
  const [showMap, setShowMap] = useState(false);

  // Get rider's current location
  const getRiderLocation = () => {
    setLocationLoading(true);
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setRiderLocation({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          });
          setLocationLoading(false);
        },
        (error) => {
          console.error('Geolocation error:', error);
          setLocationLoading(false);
          alert('Unable to get your location. Proximity sorting may not work.');
        }
      );
    } else {
      alert('Geolocation is not supported by your browser');
      setLocationLoading(false);
    }
  };

  // Calculate distance between two coordinates (Haversine formula)
  const calculateDistance = (lat1, lng1, lat2, lng2) => {
    const R = 6371; // Earth's radius in km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLng = (lng2 - lng1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLng / 2) * Math.sin(dLng / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  };

  useEffect(() => {
    fetchDeliveries();
  }, []);

  useEffect(() => {
    applySortingAndFiltering();
  }, [deliveries, sortBy, sortOrder, riderLocation]);

  const fetchDeliveries = async () => {
    try {
      const response = await getAvailableDeliveries();
      let data = [];
      if (Array.isArray(response.data)) {
        data = response.data;
      } else if (response.data && Array.isArray(response.data.deliveries)) {
        data = response.data.deliveries;
      } else if (response.data && Array.isArray(response.data.content)) {
        data = response.data.content;
      }

      // Calculate proximity distance for each delivery if rider location is available
      if (riderLocation) {
        data = data.map(delivery => ({
          ...delivery,
          proximityDistance: calculateDistance(
            riderLocation.lat,
            riderLocation.lng,
            delivery.pickupLatitude || 0,
            delivery.pickupLongitude || 0
          )
        }));
      }

      setDeliveries(data);
    } catch (err) {
      setError('Failed to load available deliveries');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const applySortingAndFiltering = () => {
    let sorted = [...deliveries];

    switch (sortBy) {
      case 'distance':
        if (riderLocation) {
          sorted.sort((a, b) => {
            const aDist = a.proximityDistance || Infinity;
            const bDist = b.proximityDistance || Infinity;
            return sortOrder === 'asc' ? aDist - bDist : bDist - aDist;
          });
        } else {
          // Fallback to estimated delivery distance
          sorted.sort((a, b) => {
            const aDist = a.distance || 0;
            const bDist = b.distance || 0;
            return sortOrder === 'asc' ? aDist - bDist : bDist - aDist;
          });
        }
        break;
      case 'earnings':
        sorted.sort((a, b) => {
          const aEarn = a.estimatedCost || 0;
          const bEarn = b.estimatedCost || 0;
          return sortOrder === 'asc' ? aEarn - bEarn : bEarn - aEarn;
        });
        break;
      case 'size':
        const sizeOrder = { 'SMALL': 1, 'MEDIUM': 2, 'LARGE': 3 };
        sorted.sort((a, b) => {
          const aSize = sizeOrder[a.parcel?.size] || 2;
          const bSize = sizeOrder[b.parcel?.size] || 2;
          return sortOrder === 'asc' ? aSize - bSize : bSize - aSize;
        });
        break;
      case 'createdAt':
        sorted.sort((a, b) => {
          const aDate = new Date(a.createdAt);
          const bDate = new Date(b.createdAt);
          return sortOrder === 'asc' ? aDate - bDate : bDate - aDate;
        });
        break;
      default:
        break;
    }

    setFilteredDeliveries(sorted);
  };

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
  };

  const handleAccept = async (deliveryId) => {
    try {
      await acceptDelivery(deliveryId);
      alert('✅ Delivery accepted!');
      fetchDeliveries();
    } catch (err) {
      alert('❌ Accept failed');
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);
  };

  const getSizeColor = (size) => {
    switch (size) {
      case 'SMALL': return 'bg-green-100 text-green-800';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800';
      case 'LARGE': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) return '↕️';
    return sortOrder === 'asc' ? '↑' : '↓';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="RIDER" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading available deliveries...</p>
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
        <Sidebar userType="RIDER" />
        <div className="flex-1 p-8">
          <div className="flex justify-between items-center mb-6">
            <div>
              <h1 className="text-2xl font-bold">Available Deliveries ({filteredDeliveries.length})</h1>
              <p className="text-gray-500 mt-1">Find and accept deliveries near you</p>
            </div>
            <div className="flex gap-3">
              <button
                onClick={getRiderLocation}
                disabled={locationLoading}
                className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
              >
                <span>📍</span>
                {locationLoading ? 'Getting location...' : (riderLocation ? 'Update Location' : 'Use My Location')}
              </button>
              <button
                onClick={() => navigate('/rider-dashboard')}
                className="text-[#2563EB] hover:underline"
              >
                ← Back to Dashboard
              </button>
            </div>
          </div>

          {/* Sort Options */}
          <div className="bg-white rounded-xl shadow-md p-4 mb-6">
            <div className="flex flex-wrap items-center gap-4">
              <span className="text-sm text-gray-500 font-medium">Sort by:</span>
              <button
                onClick={() => handleSort('distance')}
                className={`text-sm px-3 py-1 rounded-full transition-colors flex items-center gap-1 ${
                  sortBy === 'distance' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                Distance {getSortIcon('distance')}
              </button>
              <button
                onClick={() => handleSort('earnings')}
                className={`text-sm px-3 py-1 rounded-full transition-colors flex items-center gap-1 ${
                  sortBy === 'earnings' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                Earnings {getSortIcon('earnings')}
              </button>
              <button
                onClick={() => handleSort('size')}
                className={`text-sm px-3 py-1 rounded-full transition-colors flex items-center gap-1 ${
                  sortBy === 'size' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                Parcel Size {getSortIcon('size')}
              </button>
              <button
                onClick={() => handleSort('createdAt')}
                className={`text-sm px-3 py-1 rounded-full transition-colors flex items-center gap-1 ${
                  sortBy === 'createdAt' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                Posted Date {getSortIcon('createdAt')}
              </button>
            </div>
            {riderLocation && sortBy === 'distance' && (
              <p className="text-xs text-green-600 mt-3">
                📍 Sorting by proximity to your current location
              </p>
            )}
          </div>

          {error && <div className="bg-red-100 text-red-700 p-3 rounded mb-4">{error}</div>}

          {filteredDeliveries.length === 0 ? (
            <div className="bg-white rounded-xl shadow-md p-12 text-center text-gray-500">
              <div className="text-5xl mb-4">🚗</div>
              <p className="text-lg">No available deliveries at the moment.</p>
              <p className="text-sm mt-2">Check back later for new deliveries.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              {filteredDeliveries.map((delivery) => (
                <div key={delivery.id} className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all duration-300 overflow-hidden">
                  {/* Map Preview */}
                  {delivery.pickupLatitude && delivery.pickupLongitude && delivery.dropoffLatitude && delivery.dropoffLongitude && (
                    <div className="h-32 bg-gray-100 relative">
                      <img
                        src={`https://api.maptiler.com/maps/streets/staticmap/${(delivery.pickupLongitude + delivery.dropoffLongitude) / 2},${(delivery.pickupLatitude + delivery.dropoffLatitude) / 2},13/600x150.png?key=${process.env.REACT_APP_MAPTILER_KEY}&markers=pg:${delivery.pickupLatitude},${delivery.pickupLongitude},color:blue|pg:${delivery.dropoffLatitude},${delivery.dropoffLongitude},color:red`}
                        alt="Route preview"
                        className="w-full h-32 object-cover"
                        onError={(e) => {
                          e.target.style.display = 'none';
                        }}
                      />
                      <div className="absolute bottom-1 right-1 bg-black bg-opacity-50 text-white text-xs px-1.5 py-0.5 rounded">
                        Pickup (Blue) → Dropoff (Red)
                      </div>
                    </div>
                  )}

                  <div className="p-4">
                    {/* Header */}
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <span className="font-bold text-lg">{delivery.trackingNumber}</span>
                        <div className="flex gap-2 mt-1">
                          <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${getSizeColor(delivery.parcel?.size)}`}>
                            {delivery.parcel?.size || 'Standard'}
                          </span>
                          {delivery.parcel?.isFragile && (
                            <span className="bg-red-100 text-red-800 px-2 py-0.5 rounded-full text-xs">⚠️ Fragile</span>
                          )}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-bold text-xl text-[#2563EB]">{formatCurrency(delivery.estimatedCost)}</div>
                        <div className="text-xs text-gray-500">
                          {delivery.distance ? `${delivery.distance.toFixed(1)} km` : 'Distance pending'}
                        </div>
                      </div>
                    </div>

                    {/* Location Details */}
                    <div className="space-y-2 mb-3 text-sm">
                      <div className="flex items-start gap-2">
                        <span className="text-blue-500 mt-0.5">🚩</span>
                        <div className="flex-1">
                          <p className="text-gray-500 text-xs">Pickup</p>
                          <p className="font-medium">{delivery.pickupAddress}</p>
                          {delivery.pickupLatitude && delivery.pickupLongitude && riderLocation && (
                            <p className="text-xs text-green-600 mt-0.5">
                              📍 {calculateDistance(riderLocation.lat, riderLocation.lng, delivery.pickupLatitude, delivery.pickupLongitude).toFixed(1)} km from you
                            </p>
                          )}
                        </div>
                      </div>
                      <div className="flex items-start gap-2">
                        <span className="text-red-500 mt-0.5">🏁</span>
                        <div className="flex-1">
                          <p className="text-gray-500 text-xs">Dropoff</p>
                          <p className="font-medium">{delivery.dropoffAddress}</p>
                        </div>
                      </div>
                    </div>

                    {/* Parcel Details */}
                    <div className="grid grid-cols-2 gap-2 mb-4 text-sm bg-gray-50 rounded-lg p-3">
                      <div>
                        <p className="text-gray-500 text-xs">Parcel Name</p>
                        <p className="font-medium">{delivery.parcel?.name || 'N/A'}</p>
                      </div>
                      <div>
                        <p className="text-gray-500 text-xs">Weight</p>
                        <p className="font-medium">{delivery.parcel?.weight || 0} kg</p>
                      </div>
                      <div className="col-span-2">
                        <p className="text-gray-500 text-xs">Category</p>
                        <p className="font-medium">{delivery.parcel?.category || 'N/A'}</p>
                      </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="flex gap-2">
                      <button
                        onClick={() => {
                          setSelectedDelivery(delivery);
                          setShowMap(true);
                        }}
                        className="flex-1 bg-gray-200 text-gray-700 py-2 rounded-lg font-semibold hover:bg-gray-300 transition-colors flex items-center justify-center gap-1"
                      >
                        <span>🗺️</span> View Route
                      </button>
                      <button
                        onClick={() => handleAccept(delivery.id)}
                        className="flex-1 bg-[#2563EB] text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors flex items-center justify-center gap-1"
                      >
                        <span>✅</span> Accept Delivery
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Route Modal */}
      {showMap && selectedDelivery && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4" onClick={() => setShowMap(false)}>
          <div className="bg-white rounded-xl max-w-2xl w-full max-h-[80vh] overflow-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-4 border-b sticky top-0 bg-white">
              <div className="flex justify-between items-center">
                <h3 className="text-lg font-bold">Route: {selectedDelivery.trackingNumber}</h3>
                <button onClick={() => setShowMap(false)} className="text-gray-500 hover:text-gray-700 text-2xl">&times;</button>
              </div>
            </div>
            <div className="p-4">
              {selectedDelivery.pickupLatitude && selectedDelivery.pickupLongitude &&
               selectedDelivery.dropoffLatitude && selectedDelivery.dropoffLongitude ? (
                <>
                  {/* Map with MapTiler (free, no credit card required) */}
                  <div className="w-full h-64 bg-gray-100 rounded-lg mb-4 overflow-hidden">
                    <img
                      src={`https://api.maptiler.com/maps/streets/staticmap/${(selectedDelivery.pickupLongitude + selectedDelivery.dropoffLongitude) / 2},${(selectedDelivery.pickupLatitude + selectedDelivery.dropoffLatitude) / 2},13/800x400.png?key=${process.env.REACT_APP_MAPTILER_KEY}&markers=pg:${selectedDelivery.pickupLatitude},${selectedDelivery.pickupLongitude},color:blue|pg:${selectedDelivery.dropoffLatitude},${selectedDelivery.dropoffLongitude},color:red`}
                      alt="Route map"
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        e.target.style.display = 'none';
                        e.target.parentElement.innerHTML = `
                          <div class="flex flex-col items-center justify-center h-full">
                            <p class="text-gray-500 mb-2">📍 Map preview not available</p>
                            <p class="text-sm text-gray-400">Use buttons below to open in Google Maps</p>
                          </div>
                        `;
                      }}
                    />
                  </div>

                  <div className="flex gap-2 mb-4">
                    <a
                      href={`https://www.google.com/maps/dir/${selectedDelivery.pickupLatitude},${selectedDelivery.pickupLongitude}/${selectedDelivery.dropoffLatitude},${selectedDelivery.dropoffLongitude}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 bg-green-600 text-white text-center py-2 rounded-lg hover:bg-green-700"
                    >
                      📍 Open in Google Maps
                    </a>
                    <a
                      href={`https://www.openstreetmap.org/directions?engine=osrm_car&route=${selectedDelivery.pickupLatitude},${selectedDelivery.pickupLongitude}&to=${selectedDelivery.dropoffLatitude},${selectedDelivery.dropoffLongitude}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="flex-1 bg-[#2563EB] text-white text-center py-2 rounded-lg hover:bg-blue-700"
                    >
                      🗺️ Open in OpenStreetMap
                    </a>
                  </div>
                </>
              ) : (
                <p className="text-gray-500 text-center py-8">Route coordinates not available for this delivery.</p>
              )}

              <div className="bg-gray-100 rounded-lg p-4">
                <p className="font-semibold mb-2">Delivery Summary</p>
                <div className="space-y-1 text-sm">
                  <p><span className="text-gray-500">Pickup:</span> {selectedDelivery.pickupAddress}</p>
                  <p><span className="text-gray-500">Dropoff:</span> {selectedDelivery.dropoffAddress}</p>
                  <p><span className="text-gray-500">Distance:</span> {selectedDelivery.distance?.toFixed(2)} km</p>
                  <p><span className="text-gray-500">Earnings:</span> {formatCurrency(selectedDelivery.estimatedCost)}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AvailableDeliveries;