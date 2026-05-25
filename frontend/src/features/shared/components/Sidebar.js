import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

const Sidebar = ({ userType }) => {
  const location = useLocation();
  const navigate = useNavigate();

  const menuItems = userType === 'SENDER' ? [
    { path: '/sender-dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/create-delivery', label: 'Create Delivery', icon: '➕' },
    { path: '/my-deliveries', label: 'My Deliveries', icon: '📦' },
    { path: '/payments', label: 'Payments', icon: '💰' },
    { path: '/profile', label: 'Profile', icon: '👤' },
  ] : [
    { path: '/rider-dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/available-deliveries', label: 'Available', icon: '🔍' },
    { path: '/my-deliveries', label: 'My Deliveries', icon: '📦' },
    { path: '/earnings', label: 'Earnings', icon: '💰' },
    { path: '/profile', label: 'Profile', icon: '👤' },
  ];

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  return (
    <div className="w-64 bg-white shadow-md min-h-screen p-4">
      {/* Removed search bar */}
      <div className="mb-8">
        <h2 className="text-xl font-bold text-[#2563EB] px-4">QuickParcel</h2>
        <p className="text-xs text-gray-400 px-4 mt-1">{userType} Portal</p>
      </div>

      <nav>
        <ul className="space-y-2">
          {menuItems.map((item) => (
            <li key={item.path}>
              <Link
                to={item.path}
                className={`flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-300 ${
                  location.pathname === item.path
                    ? 'bg-[#2563EB] text-white'
                    : 'text-gray-600 hover:bg-gray-100'
                }`}
              >
                <span>{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            </li>
          ))}
          <li className="pt-4 mt-4 border-t">
            <button
              onClick={handleLogout}
              className="w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-gray-600 hover:bg-gray-100 transition-all duration-300"
            >
              <span>🚪</span>
              <span>Logout</span>
            </button>
          </li>
        </ul>
      </nav>
    </div>
  );
};

export default Sidebar;