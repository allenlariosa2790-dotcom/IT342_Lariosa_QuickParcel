import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import ScrollToTop from './features/shared/components/ScrollToTop';
import Home from './pages/Home';
import Login from './features/auth/pages/Login';
import Register from './features/auth/pages/Register';
import SenderDashboard from './features/sender/pages/SenderDashboard';
import RiderDashboard from './features/rider/pages/RiderDashboard';
import CreateDelivery from './features/delivery/pages/CreateDelivery';
import MyDeliveries from './features/tracking/pages/MyDeliveries';
import TrackingPage from './features/tracking/pages/TrackingPage';
import Profile from './features/auth/pages/Profile';
import AvailableDeliveries from './features/rider/pages/AvailableDeliveries';
import Earnings from './features/rider/pages/Earnings';
import PaymentSuccess from './features/payment/pages/PaymentSuccess';
import PaymentCancel from './features/payment/pages/PaymentCancel';
import PaymentsPage from './features/payment/pages/PaymentsPage';
import ReceiptPage from './features/payment/pages/ReceiptPage';

// Protected Route Component - must be used with element prop
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

        {/* Payment Routes */}
        <Route path="/payment/success" element={<PaymentSuccess />} />
        <Route path="/payment/cancel" element={<PaymentCancel />} />
        <Route
          path="/payments"
          element={
            <ProtectedRoute allowedRoles={['SENDER', 'RIDER']}>
              <PaymentsPage />
            </ProtectedRoute>
          }
        />
        <Route path="/receipt/:id" element={
            <ProtectedRoute allowedRoles={['SENDER', 'RIDER']}>
                <ReceiptPage />
            </ProtectedRoute>
        } />

        {/* Catch-all redirect */}
        <Route path="*" element={<Navigate to="/" />} />
      </Routes>
    </Router>
  );
}

export default App;