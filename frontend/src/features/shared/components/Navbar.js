import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import apiClient from '../utils/apiClient';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [profilePicture, setProfilePicture] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        setUser(parsedUser);
        setIsLoggedIn(true);
        fetchProfilePicture();
      } catch (e) {
        console.error('Failed to parse user', e);
      }
    } else {
      setIsLoggedIn(false);
      setUser(null);
      setProfilePicture(null);
    }
  }, []);

  const fetchProfilePicture = async () => {
    try {
      const response = await apiClient.get('/upload/profile-picture');
      if (response.data.hasPicture) {
        setProfilePicture(`http://localhost:8080${response.data.url}?t=${Date.now()}`);
      }
    } catch (error) {
      console.error('Failed to fetch profile picture:', error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsLoggedIn(false);
    setUser(null);
    setProfilePicture(null);
    navigate('/');
  };

  const dashboardPath = user?.userType === 'RIDER' ? '/rider-dashboard' :
                        user?.userType === 'ADMIN' ? '/admin-dashboard' : '/sender-dashboard';

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleHomeClick = () => {
    if (location.pathname !== '/') {
      navigate('/');
      setTimeout(scrollToTop, 100);
    } else {
      scrollToTop();
    }
  };

  const scrollToSection = (sectionId) => {
    if (location.pathname !== '/') {
      navigate('/');
      setTimeout(() => {
        const element = document.getElementById(sectionId);
        if (element) element.scrollIntoView({ behavior: 'smooth' });
      }, 100);
    } else {
      const element = document.getElementById(sectionId);
      if (element) element.scrollIntoView({ behavior: 'smooth' });
    }
  };

  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to={isLoggedIn ? dashboardPath : '/'} className="text-2xl font-bold text-[#2563EB]">
              QuickParcel
            </Link>
            <div className="hidden md:flex ml-10 space-x-8">
              <button
                onClick={handleHomeClick}
                className="text-gray-600 hover:text-[#2563EB] transition-colors cursor-pointer"
              >
                Home
              </button>
              <button
                onClick={() => scrollToSection('how-it-works')}
                className="text-gray-600 hover:text-[#2563EB] transition-colors cursor-pointer"
              >
                How It Works
              </button>
              <button
                onClick={() => scrollToSection('about')}
                className="text-gray-600 hover:text-[#2563EB] transition-colors cursor-pointer"
              >
                About
              </button>
              {isLoggedIn && (
                <Link to={dashboardPath} className="text-gray-600 hover:text-[#2563EB] transition-colors">
                  Dashboard
                </Link>
              )}
            </div>
          </div>

          <div className="flex items-center space-x-4">
            {isLoggedIn ? (
              <>
                <div className="flex items-center space-x-3">
                  {/* Profile Picture */}
                  <Link to="/profile" className="relative">
                    {profilePicture ? (
                      <img
                        src={profilePicture}
                        alt="Profile"
                        className="w-8 h-8 rounded-full object-cover border-2 border-[#2563EB] hover:opacity-80 transition-opacity"
                        onError={(e) => {
                          e.target.src = 'https://via.placeholder.com/32?text=U';
                        }}
                      />
                    ) : (
                      <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center border-2 border-[#2563EB] hover:bg-gray-300 transition-colors">
                        <span className="text-sm text-gray-600">
                          {user?.firstName?.charAt(0) || 'U'}
                        </span>
                      </div>
                    )}
                  </Link>
                  <span className="text-gray-700 hidden md:inline">Hello, {user?.firstName}</span>
                </div>
                <button
                  onClick={handleLogout}
                  className="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-600 hover:text-[#2563EB] font-medium transition-colors">
                  Login
                </Link>
                <Link to="/register" className="bg-[#2563EB] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition">
                  Sign Up
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;