import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import LocationPicker from '../components/LocationPicker';
import { createDelivery, calculateDistance } from '../services/delivery';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import StripePaymentForm from '../../payment/components/StripePaymentForm';
import apiClient from '../../shared/utils/apiClient';

// Stripe publishable key
const stripePromise = loadStripe('pk_test_51TYhsNPAjsa07MQsijFSCldoyzhtRJNzbyErBdnSZhLzP6Or72VRTtWAVECXLTxHvGmpRlSU4Jy1rbYoWCucduwf0006mHsDAZ');

const CreateDelivery = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Step 1: Parcel details
  const [parcel, setParcel] = useState({
    name: '',
    description: '',
    weight: '',
    size: 'MEDIUM',
    category: '',
    isFragile: false,
    imageFile: null,
    imagePreview: null,
  });

  // Step 2: Pickup & Dropoff
  const [pickup, setPickup] = useState({ address: '', lat: null, lng: null });
  const [dropoff, setDropoff] = useState({ address: '', lat: null, lng: null });
  const [scheduledTime, setScheduledTime] = useState('');
  const [notes, setNotes] = useState('');

  const [distance, setDistance] = useState(null);
  const [estimatedCost, setEstimatedCost] = useState(null);

  // Step 3: Payment
  const [paymentMethod, setPaymentMethod] = useState('COD');
  const [deliveryResponse, setDeliveryResponse] = useState(null);

  // Get minimum datetime (current time + 1 day)
  const getMinDateTime = () => {
    const minDate = new Date();
    minDate.setDate(minDate.getDate() + 1);
    minDate.setHours(0, 0, 0, 0);
    return minDate.toISOString().slice(0, 16);
  };

  // Validate step 2 before proceeding
  const validateStep2 = () => {
    if (!pickup.address || !pickup.address.trim()) {
      setError('Please select a valid pickup location');
      return false;
    }
    if (!dropoff.address || !dropoff.address.trim()) {
      setError('Please select a valid dropoff location');
      return false;
    }
    if (!scheduledTime) {
      setError('Please select a scheduled pickup time');
      return false;
    }
    const selectedDate = new Date(scheduledTime);
    const minDate = new Date();
    minDate.setDate(minDate.getDate() + 1);
    if (selectedDate < minDate) {
      setError('Scheduled time must be at least 1 day from now');
      return false;
    }
    return true;
  };

  // Calculate distance & cost using backend
  const calculateDistanceAndCost = async () => {
    if (!pickup.address || !dropoff.address) {
      setError('Please select both pickup and dropoff locations');
      return;
    }
    setLoading(true);
    try {
      const result = await calculateDistance(pickup.address, dropoff.address, parcel.weight);
      setDistance(result.distance);
      setEstimatedCost(result.estimatedCost);
      setError('');
    } catch (err) {
      console.error('Distance calculation failed', err);
      const fallbackDistance = 5.3;
      setDistance(fallbackDistance);
      setEstimatedCost(50 + fallbackDistance * 20);
    } finally {
      setLoading(false);
    }
  };

  const handleParcelChange = (e) => {
    const { name, value, type, checked, files } = e.target;
    if (type === 'file') {
      const file = files[0];
      if (file) {
        // Create preview URL
        const previewUrl = URL.createObjectURL(file);
        setParcel({
          ...parcel,
          imageFile: file,
          imagePreview: previewUrl
        });
      }
    } else if (type === 'checkbox') {
      setParcel({ ...parcel, [name]: checked });
    } else {
      setParcel({ ...parcel, [name]: value });
    }
  };

  const nextStep = async () => {
    if (step === 1) {
      if (!parcel.name || !parcel.weight || !parcel.category) {
        setError('Please fill in all required parcel details');
        return;
      }
      setError('');
      setStep(step + 1);
    } else if (step === 2) {
      if (!validateStep2()) {
        return;
      }
      setError('');
      if (!distance) {
        await calculateDistanceAndCost();
      }
      setStep(step + 1);
    }
  };

  const prevStep = () => {
    setError('');
    setStep(step - 1);
  };

  // Upload parcel image after delivery creation
  const uploadParcelImage = async (parcelId) => {
    if (!parcel.imageFile) return;

    try {
      const formData = new FormData();
      formData.append('file', parcel.imageFile);
      await apiClient.post(`/upload/parcel/${parcelId}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      console.log('Parcel image uploaded successfully');
    } catch (uploadError) {
      console.error('Failed to upload parcel image:', uploadError);
      // Don't show error to user - delivery was already created
    }
  };

  // Handle COD submission
  const handleCODSubmit = async () => {
    if (loading) return;
    setLoading(true);
    setError('');

    if (!validateStep2()) {
      setLoading(false);
      return;
    }

    const payload = {
      parcel: {
        name: parcel.name,
        description: parcel.description,
        weight: parseFloat(parcel.weight),
        size: parcel.size,
        category: parcel.category,
        isFragile: parcel.isFragile,
      },
      pickupAddress: pickup.address,
      dropoffAddress: dropoff.address,
      pickupLatitude: pickup.lat,
      pickupLongitude: pickup.lng,
      dropoffLatitude: dropoff.lat,
      dropoffLongitude: dropoff.lng,
      notes: notes,
      scheduledTime: scheduledTime ? new Date(scheduledTime).toISOString() : null,
      paymentMethod: 'COD',
      paymentStatus: 'PENDING',
    };

    try {
      const response = await createDelivery(payload);

      // Upload parcel image if exists
      if (parcel.imageFile && response.data?.parcel?.id) {
        await uploadParcelImage(response.data.parcel.id);
      }

      navigate('/my-deliveries');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create delivery');
      setLoading(false);
    }
  };

  // Handle Stripe delivery creation (creates delivery, then shows payment form)
  const handleStripeDeliveryCreation = async () => {
    if (loading) return;
    setLoading(true);
    setError('');

    if (!validateStep2()) {
      setLoading(false);
      return;
    }

    const payload = {
      parcel: {
        name: parcel.name,
        description: parcel.description,
        weight: parseFloat(parcel.weight),
        size: parcel.size,
        category: parcel.category,
        isFragile: parcel.isFragile,
      },
      pickupAddress: pickup.address,
      dropoffAddress: dropoff.address,
      pickupLatitude: pickup.lat,
      pickupLongitude: pickup.lng,
      dropoffLatitude: dropoff.lat,
      dropoffLongitude: dropoff.lng,
      notes: notes,
      scheduledTime: scheduledTime ? new Date(scheduledTime).toISOString() : null,
      paymentMethod: 'STRIPE',
      paymentStatus: 'UNPAID',
    };

    try {
      const response = await createDelivery(payload);

      // Upload parcel image if exists
      if (parcel.imageFile && response.data?.parcel?.id) {
        await uploadParcelImage(response.data.parcel.id);
      }

      setDeliveryResponse(response.data);
      setError('');
      setLoading(false);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create delivery');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="SENDER" />
        <div className="flex-1 p-8">
          <div className="max-w-3xl mx-auto">
            <h1 className="text-2xl font-bold mb-6">Create New Delivery</h1>

            {/* Step indicator */}
            <div className="flex mb-8">
              {[1, 2, 3].map((s) => (
                <div
                  key={s}
                  className={`flex-1 text-center py-2 border-b-2 ${
                    step >= s ? 'border-[#2563EB] text-[#2563EB]' : 'border-gray-300 text-gray-400'
                  }`}
                >
                  Step {s}: {s === 1 ? 'Parcel' : s === 2 ? 'Pickup & Dropoff' : 'Review & Payment'}
                </div>
              ))}
            </div>

            {error && <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">{error}</div>}

            {/* Step 1: Parcel Details */}
            {step === 1 && (
              <div className="bg-white rounded-xl shadow-md p-6 space-y-4">
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Parcel Name *</label>
                  <input
                    type="text"
                    name="name"
                    value={parcel.name}
                    onChange={handleParcelChange}
                    className="w-full border rounded-lg px-4 py-2"
                    required
                  />
                </div>
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Description</label>
                  <textarea
                    name="description"
                    value={parcel.description}
                    onChange={handleParcelChange}
                    rows="3"
                    className="w-full border rounded-lg px-4 py-2"
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-gray-700 font-medium mb-1">Weight (kg) *</label>
                    <input
                      type="number"
                      name="weight"
                      value={parcel.weight}
                      onChange={handleParcelChange}
                      step="0.1"
                      className="w-full border rounded-lg px-4 py-2"
                      required
                    />
                  </div>
                  <div>
                    <label className="block text-gray-700 font-medium mb-1">Size *</label>
                    <select
                      name="size"
                      value={parcel.size}
                      onChange={handleParcelChange}
                      className="w-full border rounded-lg px-4 py-2"
                    >
                      <option value="SMALL">Small</option>
                      <option value="MEDIUM">Medium</option>
                      <option value="LARGE">Large</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Category *</label>
                  <input
                    type="text"
                    name="category"
                    value={parcel.category}
                    onChange={handleParcelChange}
                    className="w-full border rounded-lg px-4 py-2"
                    required
                  />
                </div>
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="isFragile"
                    checked={parcel.isFragile}
                    onChange={handleParcelChange}
                    className="mr-2"
                  />
                  <label className="text-gray-700">Fragile – Handle with care</label>
                </div>

                {/* Image Upload with Preview */}
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Package Image (optional)</label>
                  <div className="flex items-start gap-4">
                    <div className="flex-1">
                      <input
                        type="file"
                        accept="image/*"
                        onChange={handleParcelChange}
                        className="w-full border rounded-lg px-4 py-2 file:mr-4 file:py-2 file:px-4 file:rounded-lg file:border-0 file:text-sm file:font-semibold file:bg-[#2563EB] file:text-white hover:file:bg-blue-700"
                      />
                      <p className="text-xs text-gray-500 mt-1">Max file size: 10MB. Supports JPG, PNG, GIF</p>
                    </div>
                    {parcel.imagePreview && (
                      <div className="relative">
                        <img
                          src={parcel.imagePreview}
                          alt="Preview"
                          className="w-20 h-20 object-cover rounded-lg border"
                        />
                        <button
                          type="button"
                          onClick={() => setParcel({ ...parcel, imageFile: null, imagePreview: null })}
                          className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs hover:bg-red-600"
                        >
                          ×
                        </button>
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex justify-end">
                  <button type="button" onClick={nextStep} className="bg-[#2563EB] text-white px-6 py-2 rounded-lg">
                    Next →
                  </button>
                </div>
              </div>
            )}

            {/* Step 2: Pickup & Dropoff with LocationPicker */}
            {step === 2 && (
              <div className="bg-white rounded-xl shadow-md p-6 space-y-4">
                <LocationPicker
                  label="Pickup Location *"
                  onLocationSelect={(loc) => setPickup(loc)}
                  initialAddress={pickup.address}
                  initialLat={pickup.lat}
                  initialLng={pickup.lng}
                />
                <LocationPicker
                  label="Dropoff Location *"
                  onLocationSelect={(loc) => setDropoff(loc)}
                  initialAddress={dropoff.address}
                  initialLat={dropoff.lat}
                  initialLng={dropoff.lng}
                />
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Scheduled Pickup Time *</label>
                  <input
                    type="datetime-local"
                    value={scheduledTime}
                    onChange={(e) => setScheduledTime(e.target.value)}
                    min={getMinDateTime()}
                    className="w-full border rounded-lg px-4 py-2"
                    required
                  />
                  <p className="text-xs text-gray-500 mt-1">Must be at least 1 day from now</p>
                </div>
                <div>
                  <label className="block text-gray-700 font-medium mb-1">Notes for Rider</label>
                  <textarea
                    value={notes}
                    onChange={(e) => setNotes(e.target.value)}
                    rows="2"
                    className="w-full border rounded-lg px-4 py-2"
                  />
                </div>

                {distance !== null && (
                  <div className="bg-gray-100 p-4 rounded-lg">
                    <p>Distance: {distance.toFixed(2)} km</p>
                    <p className="font-bold">Estimated Cost: ₱{estimatedCost?.toFixed(2)}</p>
                  </div>
                )}

                <div className="flex justify-between">
                  <button type="button" onClick={prevStep} className="bg-gray-300 text-gray-700 px-6 py-2 rounded-lg">
                    ← Back
                  </button>
                  <button
                    type="button"
                    onClick={nextStep}
                    disabled={loading}
                    className="bg-[#2563EB] text-white px-6 py-2 rounded-lg disabled:opacity-50"
                  >
                    {loading ? 'Calculating...' : 'Next →'}
                  </button>
                </div>
              </div>
            )}

            {/* Step 3: Review & Payment */}
            {step === 3 && (
              <div className="bg-white rounded-xl shadow-md p-6 space-y-4">
                <h3 className="text-lg font-bold">Review Your Delivery</h3>
                <div className="border rounded-lg p-4 space-y-2 bg-gray-50">
                  <p><strong>Parcel:</strong> {parcel.name} ({parcel.size}, {parcel.weight} kg)</p>
                  {parcel.imagePreview && (
                    <div className="mt-2">
                      <img src={parcel.imagePreview} alt="Parcel preview" className="w-24 h-24 object-cover rounded-lg" />
                    </div>
                  )}
                  <p><strong>From:</strong> {pickup.address}</p>
                  <p><strong>To:</strong> {dropoff.address}</p>
                  <p><strong>Scheduled:</strong> {scheduledTime ? new Date(scheduledTime).toLocaleString() : 'Not set'}</p>
                  <p><strong>Distance:</strong> {distance?.toFixed(2)} km</p>
                  <p className="text-lg font-bold text-[#2563EB]">Estimated Cost: ₱{estimatedCost?.toFixed(2)}</p>
                </div>

                <div>
                  <label className="block text-gray-700 font-medium mb-2">Payment Method *</label>
                  <div className="flex gap-4 flex-wrap">
                    <label className="flex items-center">
                      <input
                        type="radio"
                        value="COD"
                        checked={paymentMethod === 'COD'}
                        onChange={(e) => {
                          setPaymentMethod(e.target.value);
                          setDeliveryResponse(null);
                        }}
                        className="mr-2"
                      />
                      Cash on Delivery
                    </label>
                    <label className="flex items-center">
                      <input
                        type="radio"
                        value="STRIPE"
                        checked={paymentMethod === 'STRIPE'}
                        onChange={(e) => {
                          setPaymentMethod(e.target.value);
                          setDeliveryResponse(null);
                        }}
                        className="mr-2"
                      />
                      Credit/Debit Card (Stripe)
                    </label>
                    <label className="flex items-center opacity-50 cursor-not-allowed">
                      <input
                        type="radio"
                        value="PAYMONGO"
                        disabled
                        className="mr-2"
                      />
                      GCash (Coming Soon)
                    </label>
                  </div>
                </div>

                {/* COD Option */}
                {paymentMethod === 'COD' && (
                  <button
                    onClick={handleCODSubmit}
                    disabled={loading}
                    className="w-full bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 disabled:opacity-50 transition-colors"
                  >
                    {loading ? 'Creating...' : `Confirm Delivery (₱${estimatedCost?.toFixed(2)} COD)`}
                  </button>
                )}

                {/* Stripe Option - Create Delivery button */}
                {paymentMethod === 'STRIPE' && !deliveryResponse && (
                  <button
                    onClick={handleStripeDeliveryCreation}
                    disabled={loading}
                    className="w-full bg-[#2563EB] text-white py-3 rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50 transition-colors"
                  >
                    {loading ? 'Creating Delivery...' : `Proceed to Payment (₱${estimatedCost?.toFixed(2)})`}
                  </button>
                )}

                {/* Stripe Payment Form - Shows after delivery is created */}
                {paymentMethod === 'STRIPE' && deliveryResponse && (
                  <div className="mt-4 pt-4 border-t">
                    <Elements stripe={stripePromise}>
                      <StripePaymentForm
                        deliveryId={deliveryResponse.id}
                        amount={estimatedCost}
                        trackingNumber={deliveryResponse.trackingNumber}
                        onSuccess={() => {
                          alert('✅ Payment successful! Your delivery has been published.');
                          navigate('/my-deliveries');
                        }}
                        onError={(msg) => setError(msg)}
                      />
                    </Elements>
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateDelivery;