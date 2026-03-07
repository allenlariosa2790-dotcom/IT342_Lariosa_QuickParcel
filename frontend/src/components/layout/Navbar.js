import React from 'react';
import { Link } from 'react-router-dom';

const Navbar = () => {
  return (
    <nav className="bg-white shadow-md sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <div className="flex items-center">
            <Link to="/" className="text-2xl font-bold text-[#2563EB]">QuickParcel</Link>
            <div className="hidden md:flex ml-10 space-x-8">
              <Link to="/" className="text-gray-600 hover:text-[#2563EB] transition-colors">Home</Link>
              <Link to="/how-it-works" className="text-gray-600 hover:text-[#2563EB] transition-colors">How It Works</Link>
              <Link to="/about" className="text-gray-600 hover:text-[#2563EB] transition-colors">About</Link>
            </div>
          </div>
          <div className="flex items-center space-x-4">
            <Link to="/login" className="text-gray-600 hover:text-[#2563EB] font-medium transition-colors">Login</Link>
            <Link to="/register" className="bg-[#2563EB] text-white px-6 py-2 rounded-lg font-semibold hover:bg-blue-700 transition-all duration-300">Sign Up</Link>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;