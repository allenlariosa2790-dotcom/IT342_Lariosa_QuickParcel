import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import Navbar from '../components/layout/Navbar';
import { login } from '../services/auth';

const Login = () => {
  const [role, setRole] = useState('SENDER');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [remember, setRemember] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    try {
      // Call login API
      const response = await login({ email, password });
      console.log('Login successful:', response);

      // Store token and user data
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response));

      // Show success message
      setSuccess('Login successful! Redirecting...');

      // Redirect based on user role after 1.5 seconds
      setTimeout(() => {
        if (response.userType === 'SENDER') {
          navigate('/sender-dashboard');
        } else if (response.userType === 'RIDER') {
          navigate('/rider-dashboard');
        } else {
          navigate('/admin-dashboard');
        }
      }, 1500);
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-md mx-auto px-4 py-12">
        <div className="bg-white rounded-xl shadow-lg p-8">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-bold text-gray-900">Welcome Back</h2>
            <p className="text-gray-600 mt-2">Log in to your account</p>
          </div>

          {/* Success Message */}
          {success && (
            <div className="mb-4 p-3 bg-green-500 text-white rounded-lg animate-pulse">
              {success}
            </div>
          )}

          {/* Role Tabs */}
          <div className="flex gap-4 mb-8 border-b border-gray-200 pb-4">
            <button
              onClick={() => setRole('SENDER')}
              className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
                role === 'SENDER'
                  ? 'bg-[#2563EB] text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
              type="button"
              disabled={loading || success}
            >
              Sender
            </button>
            <button
              onClick={() => setRole('RIDER')}
              className={`flex-1 py-3 rounded-lg font-semibold transition-all ${
                role === 'RIDER'
                  ? 'bg-[#2563EB] text-white'
                  : 'text-gray-600 hover:bg-gray-100'
              }`}
              type="button"
              disabled={loading || success}
            >
              Rider
            </button>
          </div>

          {error && (
            <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-gray-700 font-medium mb-2">Email</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#2563EB] focus:border-transparent outline-none transition-all"
                placeholder="john@example.com"
                required
                disabled={loading || success}
              />
            </div>

            <div className="mb-6">
              <label className="block text-gray-700 font-medium mb-2">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#2563EB] focus:border-transparent outline-none transition-all"
                placeholder="••••••••"
                required
                disabled={loading || success}
              />
            </div>

            <div className="flex items-center justify-between mb-6">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                  className="rounded border-gray-300 text-[#2563EB] focus:ring-[#2563EB]"
                  disabled={loading || success}
                />
                <span className="ml-2 text-gray-600">Remember me</span>
              </label>
              <Link to="/forgot-password" className="text-[#2563EB] hover:underline">
                Forgot Password?
              </Link>
            </div>

            <button
              type="submit"
              className="w-full bg-[#2563EB] text-white px-6 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
              disabled={loading || success}
            >
              {loading ? 'Logging in...' : success ? 'Redirecting...' : 'Log In'}
            </button>

            <p className="text-center mt-6 text-gray-600">
              Don't have an account?{' '}
              <Link to="/register" className="text-[#2563EB] font-semibold hover:underline">
                Sign Up
              </Link>
            </p>
          </form>

          <p className="text-center mt-8 text-gray-400 text-sm">
            © 2026 QuickParcel
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;