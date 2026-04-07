import React, { useState, useEffect, useRef } from 'react';
import { MapContainer, TileLayer, Marker, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix default marker icon in Leaflet
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const LocationPicker = ({ label, onLocationSelect, initialAddress = '', initialLat = null, initialLng = null }) => {
  const [address, setAddress] = useState(initialAddress);
  const [lat, setLat] = useState(initialLat || 10.3157); // Default Cebu City
  const [lng, setLng] = useState(initialLng || 123.8854);
  const [suggestions, setSuggestions] = useState([]);
  const [isTyping, setIsTyping] = useState(false);
  const debounceTimer = useRef(null);

  // Notify parent when lat/lng or address changes
  useEffect(() => {
    if (lat && lng && address) {
      onLocationSelect({ address, lat, lng });
    }
  }, [lat, lng, address]);

  // Geocode address to coordinates using Nominatim
  const geocodeAddress = async (query) => {
    if (!query.trim()) return;
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&addressdetails=0`
      );
      const data = await response.json();
      if (data.length > 0) {
        const { lat, lon, display_name } = data[0];
        setLat(parseFloat(lat));
        setLng(parseFloat(lon));
        setAddress(display_name);
        return { lat: parseFloat(lat), lng: parseFloat(lon), display_name };
      }
    } catch (error) {
      console.error('Geocoding error:', error);
    }
    return null;
  };

  // Autocomplete: fetch suggestions as user types
  const fetchSuggestions = async (query) => {
    if (query.length < 3) {
      setSuggestions([]);
      return;
    }
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=5&addressdetails=0`
      );
      const data = await response.json();
      setSuggestions(data);
    } catch (error) {
      console.error('Autocomplete error:', error);
    }
  };

  const handleAddressChange = (e) => {
    const value = e.target.value;
    setAddress(value);
    setIsTyping(true);
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      fetchSuggestions(value);
    }, 500);
  };

  const handleSuggestionClick = (suggestion) => {
    setAddress(suggestion.display_name);
    setLat(parseFloat(suggestion.lat));
    setLng(parseFloat(suggestion.lon));
    setSuggestions([]);
    setIsTyping(false);
  };

  // Map click handler to update marker and reverse geocode
  const MapClickHandler = () => {
    useMapEvents({
      click(e) {
        const { lat, lng } = e.latlng;
        setLat(lat);
        setLng(lng);
        // Reverse geocode to get address
        fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`)
          .then(res => res.json())
          .then(data => {
            if (data.display_name) setAddress(data.display_name);
          })
          .catch(err => console.error('Reverse geocode error:', err));
      },
    });
    return null;
  };

  return (
    <div className="mb-4">
      <label className="block text-gray-700 font-medium mb-1">{label} *</label>
      <input
        type="text"
        value={address}
        onChange={handleAddressChange}
        className="w-full border rounded-lg px-4 py-2"
        placeholder="Type an address or click on map"
      />
      {suggestions.length > 0 && isTyping && (
        <ul className="border rounded-lg mt-1 max-h-40 overflow-auto bg-white shadow-lg z-10 relative">
          {suggestions.map((s, idx) => (
            <li
              key={idx}
              onClick={() => handleSuggestionClick(s)}
              className="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm"
            >
              {s.display_name}
            </li>
          ))}
        </ul>
      )}
      <div className="mt-2 h-96 rounded-lg overflow-hidden border">
        <MapContainer center={[lat, lng]} zoom={13} style={{ height: '100%', width: '100%' }}>
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />
          <Marker position={[lat, lng]} draggable={true} eventHandlers={{
            dragend: (e) => {
              const { lat, lng } = e.target.getLatLng();
              setLat(lat);
              setLng(lng);
              // Reverse geocode
              fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&zoom=18&addressdetails=1`)
                .then(res => res.json())
                .then(data => {
                  if (data.display_name) setAddress(data.display_name);
                });
            }
          }} />
          <MapClickHandler />
        </MapContainer>
      </div>
      <p className="text-xs text-gray-500 mt-1">
        Drag the marker or click on map to adjust location.
      </p>
    </div>
  );
};

export default LocationPicker;