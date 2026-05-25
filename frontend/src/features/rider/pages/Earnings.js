import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import { getMyDeliveries } from '../../tracking/services/trackingApi';

const Earnings = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [earnings, setEarnings] = useState({
    today: 0,
    thisWeek: 0,
    lastWeek: 0,
    thisMonth: 0,
    allTime: 0,
    completedCount: 0,
    averagePerDelivery: 0,
  });
  const [weeklyData, setWeeklyData] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [filter, setFilter] = useState('all'); // all, thisWeek, thisMonth

  useEffect(() => {
    const userData = localStorage.getItem('user');
    if (userData) setUser(JSON.parse(userData));
    fetchEarningsData();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);
  };

  const fetchEarningsData = async () => {
    setLoading(true);
    try {
      const response = await getMyDeliveries();
      const deliveries = Array.isArray(response.data) ? response.data : [];

      // Filter completed deliveries only
      const completed = deliveries.filter(d => d.status === 'DELIVERED');

      // Calculate all-time earnings
      const allTimeEarnings = completed.reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      // Today's earnings
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const todayEarnings = completed
        .filter(d => new Date(d.deliveredTime || d.updatedAt || d.createdAt) >= today)
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      // This week (last 7 days)
      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      const weekEarnings = completed
        .filter(d => new Date(d.deliveredTime || d.updatedAt || d.createdAt) >= weekAgo)
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      // Last week (days 7-14)
      const twoWeeksAgo = new Date();
      twoWeeksAgo.setDate(twoWeeksAgo.getDate() - 14);
      const lastWeekEarnings = completed
        .filter(d => {
          const date = new Date(d.deliveredTime || d.updatedAt || d.createdAt);
          return date >= twoWeeksAgo && date < weekAgo;
        })
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      // This month (last 30 days)
      const monthAgo = new Date();
      monthAgo.setDate(monthAgo.getDate() - 30);
      const monthEarnings = completed
        .filter(d => new Date(d.deliveredTime || d.updatedAt || d.createdAt) >= monthAgo)
        .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

      // Calculate average per delivery
      const avgPerDelivery = completed.length > 0 ? allTimeEarnings / completed.length : 0;

      setEarnings({
        today: todayEarnings,
        thisWeek: weekEarnings,
        lastWeek: lastWeekEarnings,
        thisMonth: monthEarnings,
        allTime: allTimeEarnings,
        completedCount: completed.length,
        averagePerDelivery: avgPerDelivery,
      });

      // Prepare weekly data for chart (last 7 days)
      const last7Days = [];
      for (let i = 6; i >= 0; i--) {
        const date = new Date();
        date.setDate(date.getDate() - i);
        date.setHours(0, 0, 0, 0);
        const nextDay = new Date(date);
        nextDay.setDate(date.getDate() + 1);

        const dayEarnings = completed
          .filter(d => {
            const deliveryDate = new Date(d.deliveredTime || d.updatedAt || d.createdAt);
            return deliveryDate >= date && deliveryDate < nextDay;
          })
          .reduce((sum, d) => sum + (d.estimatedCost || 0), 0);

        last7Days.push({
          day: date.toLocaleDateString('en-US', { weekday: 'short' }),
          date: date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
          earnings: dayEarnings,
          deliveries: completed.filter(d => {
            const deliveryDate = new Date(d.deliveredTime || d.updatedAt || d.createdAt);
            return deliveryDate >= date && deliveryDate < nextDay;
          }).length,
        });
      }
      setWeeklyData(last7Days);

      // Prepare transactions (all completed deliveries, sorted by date descending)
      const sortedTransactions = [...completed].sort((a, b) => {
        const dateA = new Date(a.deliveredTime || a.updatedAt || a.createdAt);
        const dateB = new Date(b.deliveredTime || b.updatedAt || b.createdAt);
        return dateB - dateA;
      });

      setTransactions(sortedTransactions);

    } catch (err) {
      console.error('Failed to fetch earnings data:', err);
    } finally {
      setLoading(false);
    }
  };

  const getFilteredTransactions = () => {
    if (filter === 'all') return transactions;

    const now = new Date();
    if (filter === 'thisWeek') {
      const weekAgo = new Date();
      weekAgo.setDate(weekAgo.getDate() - 7);
      return transactions.filter(t => {
        const date = new Date(t.deliveredTime || t.updatedAt || t.createdAt);
        return date >= weekAgo;
      });
    }
    if (filter === 'thisMonth') {
      const monthAgo = new Date();
      monthAgo.setDate(monthAgo.getDate() - 30);
      return transactions.filter(t => {
        const date = new Date(t.deliveredTime || t.updatedAt || t.createdAt);
        return date >= monthAgo;
      });
    }
    return transactions;
  };

  const maxEarnings = Math.max(...weeklyData.map(d => d.earnings), 100);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="RIDER" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading earnings data...</p>
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
              <h1 className="text-2xl font-bold">Earnings</h1>
              <p className="text-gray-500 mt-1">Track your delivery earnings and history</p>
            </div>
            <button
              onClick={() => navigate('/rider-dashboard')}
              className="text-[#2563EB] hover:underline"
            >
              ← Back to Dashboard
            </button>
          </div>

          {/* Summary Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-gray-500 text-sm">Today</p>
              <p className="text-2xl font-bold text-[#2563EB]">{formatCurrency(earnings.today)}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-gray-500 text-sm">This Week</p>
              <p className="text-2xl font-bold text-green-600">{formatCurrency(earnings.thisWeek)}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-gray-500 text-sm">This Month</p>
              <p className="text-2xl font-bold text-blue-600">{formatCurrency(earnings.thisMonth)}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4 text-center">
              <p className="text-gray-500 text-sm">All Time</p>
              <p className="text-2xl font-bold text-purple-600">{formatCurrency(earnings.allTime)}</p>
            </div>
          </div>

          {/* Additional Stats */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
            <div className="bg-white rounded-xl shadow-md p-4">
              <p className="text-gray-500 text-sm">Completed Deliveries</p>
              <p className="text-2xl font-bold text-[#2563EB]">{earnings.completedCount}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4">
              <p className="text-gray-500 text-sm">Average per Delivery</p>
              <p className="text-2xl font-bold text-[#2563EB]">{formatCurrency(earnings.averagePerDelivery)}</p>
            </div>
            <div className="bg-white rounded-xl shadow-md p-4">
              <p className="text-gray-500 text-sm">Last Week</p>
              <p className="text-2xl font-bold text-gray-700">{formatCurrency(earnings.lastWeek)}</p>
            </div>
          </div>

          {/* Weekly Earnings Chart */}
          <div className="bg-white rounded-xl shadow-md p-6 mb-8">
            <h3 className="text-lg font-bold mb-4">Daily Earnings (Last 7 Days)</h3>
            {weeklyData.every(d => d.earnings === 0) ? (
              <div className="text-center text-gray-500 py-8">
                <p>No earnings data available for the past week.</p>
                <p className="text-sm mt-1">Complete deliveries to see your earnings chart!</p>
              </div>
            ) : (
              <div className="h-64">
                <div className="flex h-full items-end gap-2">
                  {weeklyData.map((day, idx) => (
                    <div key={idx} className="flex-1 flex flex-col items-center gap-2">
                      <div className="relative w-full flex flex-col items-center">
                        <div
                          className="w-full bg-[#2563EB] rounded-t transition-all duration-500 hover:bg-blue-600"
                          style={{
                            height: `${(day.earnings / maxEarnings) * 150}px`,
                            minHeight: day.earnings > 0 ? '4px' : '0px'
                          }}
                        >
                          <div className="absolute -top-6 left-1/2 transform -translate-x-1/2 text-xs font-semibold text-[#2563EB] whitespace-nowrap">
                            {day.earnings > 0 ? formatCurrency(day.earnings) : ''}
                          </div>
                        </div>
                      </div>
                      <div className="text-center">
                        <div className="text-xs font-medium">{day.day}</div>
                        <div className="text-xs text-gray-400">{day.date}</div>
                        {day.deliveries > 0 && (
                          <div className="text-xs text-green-600">{day.deliveries} delivery{day.deliveries !== 1 ? 's' : ''}</div>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Filter Controls */}
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-bold">Transaction History</h3>
            <div className="flex gap-2">
              <button
                onClick={() => setFilter('all')}
                className={`px-3 py-1 rounded-lg text-sm transition-colors ${
                  filter === 'all' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                All Time
              </button>
              <button
                onClick={() => setFilter('thisWeek')}
                className={`px-3 py-1 rounded-lg text-sm transition-colors ${
                  filter === 'thisWeek' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                This Week
              </button>
              <button
                onClick={() => setFilter('thisMonth')}
                className={`px-3 py-1 rounded-lg text-sm transition-colors ${
                  filter === 'thisMonth' ? 'bg-[#2563EB] text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                This Month
              </button>
            </div>
          </div>

          {/* Transactions List */}
          {getFilteredTransactions().length === 0 ? (
            <div className="bg-white rounded-xl shadow-md p-12 text-center text-gray-500">
              <div className="text-5xl mb-4">💰</div>
              <p className="text-lg">No completed deliveries yet.</p>
              <p className="text-sm mt-2">Complete deliveries to see your earnings here.</p>
              <button
                onClick={() => navigate('/available-deliveries')}
                className="mt-4 bg-[#2563EB] text-white px-6 py-2 rounded-lg hover:bg-blue-700"
              >
                Browse Deliveries
              </button>
            </div>
          ) : (
            <div className="bg-white rounded-xl shadow-md overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-50 border-b">
                    <tr>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Tracking #</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Date Completed</th>
                      <th className="text-left p-4 text-sm font-semibold text-gray-600">Pickup → Dropoff</th>
                      <th className="text-right p-4 text-sm font-semibold text-gray-600">Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {getFilteredTransactions().map((delivery, idx) => (
                      <tr key={delivery.id} className={`border-b hover:bg-gray-50 transition-colors ${idx % 2 === 0 ? 'bg-white' : 'bg-gray-50'}`}>
                        <td className="p-4 font-mono text-sm">{delivery.trackingNumber}</td>
                        <td className="p-4 text-sm text-gray-600">
                          {new Date(delivery.deliveredTime || delivery.updatedAt || delivery.createdAt).toLocaleDateString('en-PH', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric',
                          })}
                        </td>
                        <td className="p-4 text-sm text-gray-600 max-w-xs">
                          <div className="truncate">
                            {delivery.pickupAddress?.split(',')[0]} → {delivery.dropoffAddress?.split(',')[0]}
                          </div>
                        </td>
                        <td className="p-4 text-right font-semibold text-[#2563EB]">
                          {formatCurrency(delivery.estimatedCost)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Earnings;