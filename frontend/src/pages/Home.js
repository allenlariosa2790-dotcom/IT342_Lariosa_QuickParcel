import React from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import Footer from '../components/layout/Footer';

const Home = () => {
  return (
    <div className="min-h-screen">
      <Navbar />

      {/* Hero Section - Matches SDD */}
      <section className="bg-gradient-to-r from-[#2563EB] to-blue-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-bold mb-6">
              Send Packages Instantly with Nearby Riders
            </h1>
            <p className="text-xl md:text-2xl mb-10 opacity-90 max-w-3xl mx-auto">
              Same-day delivery within your city. Fast, affordable, and trackable.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link to="/register" className="bg-white text-[#2563EB] px-6 py-3 rounded-lg font-semibold hover:bg-gray-100 transition-all duration-300 hover:shadow-lg">
                Send a Package
              </Link>
              <Link to="/register?role=rider" className="border-2 border-white text-white px-6 py-3 rounded-lg font-semibold hover:bg-white hover:text-[#2563EB] transition-all duration-300">
                Become a Rider
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* How It Works - 3 Steps - Matches SDD */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-12">How It Works</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-[#2563EB]">1</span>
              </div>
              <h3 className="text-xl font-bold mb-2">Create Delivery</h3>
              <p className="text-gray-600">Enter your package details, pickup and drop-off locations</p>
            </div>
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-[#2563EB]">2</span>
              </div>
              <h3 className="text-xl font-bold mb-2">Choose Rider</h3>
              <p className="text-gray-600">Get matched with verified nearby riders instantly</p>
            </div>
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 text-center">
              <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <span className="text-2xl font-bold text-[#2563EB]">3</span>
              </div>
              <h3 className="text-xl font-bold mb-2">Track & Receive</h3>
              <p className="text-gray-600">Real-time tracking until successful delivery</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Grid - 2x2 - Matches SDD */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-12">Why Choose QuickParcel</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <span className="text-[#2563EB] text-xl">📍</span>
                </div>
              </div>
              <div>
                <h3 className="text-xl font-bold mb-2">Real-time Tracking</h3>
                <p className="text-gray-600">Track your package every step of the way with live GPS updates</p>
              </div>
            </div>
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <span className="text-[#2563EB] text-xl">🔒</span>
                </div>
              </div>
              <div>
                <h3 className="text-xl font-bold mb-2">Secure Payments</h3>
                <p className="text-gray-600">Multiple payment options with encrypted transactions</p>
              </div>
            </div>
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <span className="text-[#2563EB] text-xl">📦</span>
                </div>
              </div>
              <div>
                <h3 className="text-xl font-bold mb-2">Package Insurance</h3>
                <p className="text-gray-600">Your parcels are protected with comprehensive insurance</p>
              </div>
            </div>
            <div className="bg-white rounded-xl shadow-md hover:shadow-lg transition-shadow duration-300 p-6 flex items-start space-x-4">
              <div className="flex-shrink-0">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <span className="text-[#2563EB] text-xl">✓</span>
                </div>
              </div>
              <div>
                <h3 className="text-xl font-bold mb-2">Rider Verification</h3>
                <p className="text-gray-600">All riders are verified and background checked</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials - Matches SDD */}
      <section className="py-20 bg-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h2 className="text-3xl md:text-4xl font-bold text-center mb-12">What Our Users Say</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            <div className="bg-white rounded-xl shadow-md p-6">
              <div className="flex text-yellow-400 mb-4">★★★★★</div>
              <p className="text-gray-700 mb-4">"QuickParcel saved my day! I needed urgent delivery and they delivered within 2 hours. Highly recommended!"</p>
              <div className="font-bold">Sarah Johnson</div>
              <div className="text-sm text-gray-500">Business Owner</div>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <div className="flex text-yellow-400 mb-4">★★★★☆</div>
              <p className="text-gray-700 mb-4">"As a rider, I love the flexibility and the earnings are great. The app is easy to use and support is excellent."</p>
              <div className="font-bold">Michael Chen</div>
              <div className="text-sm text-gray-500">QuickParcel Rider</div>
            </div>
            <div className="bg-white rounded-xl shadow-md p-6">
              <div className="flex text-yellow-400 mb-4">★★★★★</div>
              <p className="text-gray-700 mb-4">"Reliable, affordable, and trackable. This is now my go-to delivery service for all my packages."</p>
              <div className="font-bold">Emily Rodriguez</div>
              <div className="text-sm text-gray-500">Freelancer</div>
            </div>
          </div>
        </div>
      </section>

      <Footer />
    </div>
  );
};

export default Home;