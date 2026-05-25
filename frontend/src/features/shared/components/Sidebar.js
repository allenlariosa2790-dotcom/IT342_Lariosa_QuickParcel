import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';

const Sidebar = ({ userType }) => {
  const location = useLocation();
  const navigate = useNavigate();

  let menuItems = [];

  if (userType === 'SENDER') {
    menuItems = [
      { path: '/sender-dashboard', label: 'Dashboard', icon: '📊' },
      { path: '/create-delivery', label: 'Create Delivery', icon: '➕' },
      { path: '/my-deliveries', label: 'My Deliveries', icon: '📦' },
      { path: '/payments', label: 'Payments', icon: '💰' },
      { path: '/profile', label: 'Profile', icon: '👤' },
    ];
  } else if (userType === 'RIDER') {
    menuItems = [
      { path: '/rider-dashboard', label: 'Dashboard', icon: '📊' },
      { path: '/available-deliveries', label: 'Available', icon: '🔍' },
      { path: '/my-deliveries', label: 'My Deliveries', icon: '📦' },
      { path: '/earnings', label: 'Earnings', icon: '💰' },
      { path: '/profile', label: 'Profile', icon: '👤' },
    ];
  } else if (userType === 'ADMIN') {
      menuItems = [
        { path: '/admin-dashboard', label: 'Dashboard', icon: '📊' },
        { path: '/admin/users', label: 'User Management', icon: '👥' },
        { path: '/admin/deliveries', label: 'Delivery Management', icon: '📦' },
        { path: '/admin/profile', label: 'Profile', icon: '👤' },
      ];
    }

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    navigate('/login');
  };

  // Function to handle admin navigation with tab parameter
  const handleAdminNav = (tab) => {
    navigate(`/admin-dashboard?tab=${tab}`);
  };

  return (
    <div className="w-64 bg-white shadow-md min-h-screen p-4">
      <div className="mb-8">

        <h2 className="text-m text-gray-400 px-4 mt-1">{userType} Portal</h2>
      </div>

      <nav>
        <ul className="space-y-2">
          {menuItems.map((item) => (
            <li key={item.path}>
              {userType === 'ADMIN' && item.path.includes('?') ? (
                // Handle admin links with query parameters
                <button
                  onClick={() => {
                    const tab = item.path.split('=')[1];
                    handleAdminNav(tab);
                  }}
                  className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg transition-all duration-300 ${
                    (item.path.includes('users') && location.search.includes('tab=users')) ||
                    (item.path.includes('deliveries') && location.search.includes('tab=deliveries')) ||
                    (item.path === '/admin-dashboard' && !location.search.includes('tab='))
                      ? 'bg-[#2563EB] text-white'
                      : 'text-gray-600 hover:bg-gray-100'
                  }`}
                >
                  <span>{item.icon}</span>
                  <span>{item.label}</span>
                </button>
              ) : (
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
              )}
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