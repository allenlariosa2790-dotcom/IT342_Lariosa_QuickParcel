import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');
    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        setUser(parsedUser);
        setIsLoggedIn(true);
      } catch (e) {
        console.error('Failed to parse user', e);
      }
    } else {
      setIsLoggedIn(false);
      setUser(null);
    }
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setIsLoggedIn(false);
    setUser(null);
    navigate('/');
  };

  const dashboardPath = user?.userType === 'RIDER' ? '/rider-dashboard' : '/sender-dashboard';

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleHomeClick = () => {
    if (location.pathname !== '/') {
      navigate('/');
      // After navigation, scroll to top (ScrollToTop will also trigger, but this ensures)
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
            {/* Logo: goes to dashboard if logged in, else home */}
            <Link to={isLoggedIn ? dashboardPath : '/'} className="text-2xl font-bold text-[#2563EB]">
              QuickParcel
            </Link>
            <div className="hidden md:flex ml-10 space-x-8">
              {/* Home button – always scrolls to top */}
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
                <span className="text-gray-700">Hello, {user?.firstName}</span>
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