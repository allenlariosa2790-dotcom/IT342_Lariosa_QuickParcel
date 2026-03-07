import React, { useState } from 'react';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';

const AvailableDeliveries = () => {
  const [deliveries] = useState([
    {
      id: 'QP-2026-007',
      size: 'Medium',
      pickup: '123 Main St, Downtown',
      dropoff: '456 Oak Ave, Uptown',
      distance: '3.2 km',
      weight: '2.5 kg',
      earnings: '$8.50'
    },
    {
      id: 'QP-2026-008',
      size: 'Large',
      pickup: '789 Elm St, Midtown',
      dropoff: '321 Pine Rd, Suburbs',
      distance: '5.8 km',
      weight: '4.0 kg',
      earnings: '$12.00'
    },
    {
      id: 'QP-2026-009',
      size: 'Small',
      pickup: '555 Market St, Downtown',
      dropoff: '888 Broadway, Center',
      distance: '2.1 km',
      weight: '1.0 kg',
      earnings: '$6.50'
    },
    {
      id: 'QP-2026-010',
      size: 'Medium',
      pickup: '222 First St, Northside',
      dropoff: '777 Second Ave, Southside',
      distance: '4.5 km',
      weight: '3.2 kg',
      earnings: '$10.00'
    }
  ]);

  const handleAccept = (id) => {
    alert(`Delivery ${id} accepted! (Demo)`);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="RIDER" />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold mb-6">Available Deliveries</h1>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {deliveries.map((delivery) => (
              <div key={delivery.id} className="bg-white rounded-xl shadow-md p-4 hover:shadow-lg transition-shadow">
                <div className="flex justify-between items-start mb-2">
                  <span className="font-semibold text-lg">{delivery.id}</span>
                  <span className="bg-blue-100 text-blue-800 text-xs px-2 py-1 rounded-full">{delivery.size}</span>
                </div>
                <div className="text-sm text-gray-600 mb-3">
                  <div className="flex items-start mb-1">
                    <span className="mr-2">📍</span>
                    <div>
                      <div className="font-medium">From:</div>
                      <div>{delivery.pickup}</div>
                    </div>
                  </div>
                  <div className="flex items-start">
                    <span className="mr-2">🏁</span>
                    <div>
                      <div className="font-medium">To:</div>
                      <div>{delivery.dropoff}</div>
                    </div>
                  </div>
                </div>
                <div className="flex justify-between items-center text-sm border-t pt-3">
                  <div className="flex space-x-3">
                    <span className="text-gray-500">📏 {delivery.distance}</span>
                    <span className="text-gray-500">⚖️ {delivery.weight}</span>
                  </div>
                  <span className="font-bold text-[#2563EB]">{delivery.earnings}</span>
                </div>
                <button
                  onClick={() => handleAccept(delivery.id)}
                  className="w-full mt-3 bg-[#2563EB] text-white py-2 rounded-lg font-semibold hover:bg-blue-700 transition-colors"
                >
                  Accept Delivery
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AvailableDeliveries;