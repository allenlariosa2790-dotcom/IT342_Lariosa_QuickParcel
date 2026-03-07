import React from 'react';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';

const MyDeliveries = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="SENDER" />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold mb-4">My Deliveries</h1>
          <p>Delivery list coming soon...</p>
        </div>
      </div>
    </div>
  );
};

export default MyDeliveries;