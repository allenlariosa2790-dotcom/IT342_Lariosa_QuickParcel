import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ScrollToTop from './components/ScrollToTop';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import SenderDashboard from './pages/SenderDashboard';
import RiderDashboard from './pages/RiderDashboard';
import CreateDelivery from './pages/CreateDelivery';
import MyDeliveries from './pages/MyDeliveries';
import TrackingPage from './pages/TrackingPage';
import Profile from './pages/Profile';
import AvailableDeliveries from './pages/AvailableDeliveries';
import Earnings from './pages/Earnings';
import PaymentSuccess from './pages/PaymentSuccess';
import PaymentCancel from './pages/PaymentCancel';

// Protected Route Component
const ProtectedRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem('token');
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  if (!token) {
    return <Navigate to="/login" />;
  }

  if (allowedRoles && !allowedRoles.includes(user.userType)) {
    return <Navigate to="/" />;
  }

  return children;
};

function App() {
  return (
    <Router>
      <ScrollToTop />
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Sender Routes */}
        <Route
          path="/sender-dashboard"
          element={
            <ProtectedRoute allowedRoles={['SENDER']}>
              <SenderDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/create-delivery"
          element={
            <ProtectedRoute allowedRoles={['SENDER']}>
              <CreateDelivery />
            </ProtectedRoute>
          }
        />

        {/* Rider Routes */}
        <Route
          path="/rider-dashboard"
          element={
            <ProtectedRoute allowedRoles={['RIDER']}>
              <RiderDashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/available-deliveries"
          element={
            <ProtectedRoute allowedRoles={['RIDER']}>
              <AvailableDeliveries />
            </ProtectedRoute>
          }
        />
        <Route
          path="/earnings"
          element={
            <ProtectedRoute allowedRoles={['RIDER']}>
              <Earnings />
            </ProtectedRoute>
          }
        />

        {/* Shared Routes (both roles) */}
        <Route
          path="/my-deliveries"
          element={
            <ProtectedRoute allowedRoles={['SENDER', 'RIDER']}>
              <MyDeliveries />
            </ProtectedRoute>
          }
        />
        <Route
          path="/tracking/:id"
          element={
            <ProtectedRoute allowedRoles={['SENDER', 'RIDER']}>
              <TrackingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile"
          element={
            <ProtectedRoute allowedRoles={['SENDER', 'RIDER']}>
              <Profile />
            </ProtectedRoute>
          }
        />

        {/* Payment Routes (Public - accessed after redirect from PayMongo) */}
        <Route path="/payment/success" element={<PaymentSuccess />} />
        <Route path="/payment/cancel" element={<PaymentCancel />} />

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;