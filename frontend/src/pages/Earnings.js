import React from 'react';
import Navbar from '../components/layout/Navbar';
import Sidebar from '../components/layout/Sidebar';

const Earnings = () => {
  const transactions = [
    { id: 'QP-2298', date: 'Feb 28, 2026', amount: '$18', status: 'Completed' },
    { id: 'QP-2297', date: 'Feb 28, 2026', amount: '$15', status: 'Completed' },
    { id: 'QP-2295', date: 'Feb 27, 2026', amount: '$22', status: 'Completed' },
    { id: 'QP-2293', date: 'Feb 27, 2026', amount: '$12', status: 'Completed' },
    { id: 'QP-2290', date: 'Feb 26, 2026', amount: '$20', status: 'Completed' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="flex">
        <Sidebar userType="RIDER" />
        <div className="flex-1 p-8">
          <h1 className="text-2xl font-bold mb-6">Earnings</h1>

          {/* Summary Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">This Week</p>
              <p className="text-3xl font-bold text-[#2563EB]">$342</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Today</p>
              <p className="text-3xl font-bold text-[#2563EB]">$42</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <p className="text-gray-500 text-sm">Average</p>
              <p className="text-3xl font-bold text-[#2563EB]">$48/day</p>
            </div>
          </div>

          {/* Weekly Overview Chart Placeholder */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Weekly Overview</h3>
            <div className="flex justify-between items-end h-32 mb-2">
              {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day, i) => (
                <div key={day} className="flex flex-col items-center w-12">
                  <div className="w-8 bg-[#2563EB] rounded-t" style={{ height: `${20 + i * 8}px` }}></div>
                  <span className="text-xs mt-2">{day}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Linked Account */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Linked Account</h3>
            <div className="flex justify-between items-center">
              <div>
                <p className="font-medium">Bank of America</p>
                <p className="text-sm text-gray-500">Account Number •••• 4829</p>
              </div>
              <button className="text-[#2563EB] hover:underline">Withdraw</button>
            </div>
          </div>

          {/* Recent Transactions */}
          <div className="bg-white rounded-xl shadow-md p-6">
            <h3 className="text-lg font-bold mb-4">Recent Transactions</h3>
            <div className="space-y-3">
              {transactions.map((t) => (
                <div key={t.id} className="flex justify-between items-center border-b pb-2">
                  <div>
                    <p className="font-medium">{t.id}</p>
                    <p className="text-sm text-gray-500">{t.date}</p>
                  </div>
                  <p className="font-bold text-[#2563EB]">{t.amount}</p>
                </div>
              ))}
            </div>
            <button className="w-full mt-4 text-[#2563EB] hover:underline text-center">
              View All Transactions
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Earnings;