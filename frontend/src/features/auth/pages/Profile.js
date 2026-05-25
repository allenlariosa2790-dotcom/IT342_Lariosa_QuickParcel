import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../shared/components/Navbar';
import Sidebar from '../../shared/components/Sidebar';
import ProfilePictureUpload from '../../shared/components/ProfilePictureUpload';
import apiClient from '../../shared/utils/apiClient';

const Profile = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    phone: '',
  });

  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    if (!token || !userData) {
      navigate('/login');
      return;
    }

    const parsedUser = JSON.parse(userData);
    setUser(parsedUser);
    setFormData({
      firstName: parsedUser.firstName || '',
      lastName: parsedUser.lastName || '',
      phone: parsedUser.phone || '',
    });
    setLoading(false);
  }, [navigate]);

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setMessage({ type: '', text: '' });
  };

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage({ type: '', text: '' });

    try {
      const response = await apiClient.put('/auth/profile', {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phone: formData.phone,
      });

      const updatedUser = { ...user, firstName: formData.firstName, lastName: formData.lastName, phone: formData.phone };
      localStorage.setItem('user', JSON.stringify(updatedUser));
      setUser(updatedUser);

      setMessage({ type: 'success', text: 'Profile updated successfully!' });
      setEditing(false);
    } catch (error) {
      setMessage({ type: 'error', text: error.response?.data?.message || 'Failed to update profile' });
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="flex">
          <Sidebar userType="SENDER" />
          <div className="flex-1 p-8 flex justify-center items-center">
            <div className="text-center">
              <div className="w-16 h-16 border-4 border-[#2563EB] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
              <p>Loading profile...</p>
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
        <Sidebar userType={user?.userType || 'SENDER'} />
        <div className="flex-1 p-8">
          <div className="max-w-2xl mx-auto">
            <div className="flex justify-between items-center mb-6">
              <div>
                <h1 className="text-2xl font-bold">Profile Settings</h1>
                <p className="text-gray-500 mt-1">Manage your account information</p>
              </div>
              {!editing && (
                <button
                  onClick={() => setEditing(true)}
                  className="bg-[#2563EB] text-white px-4 py-2 rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Edit Profile
                </button>
              )}
            </div>

            {/* Profile Picture Section */}
            <div className="bg-white rounded-xl shadow-md p-6 mb-6 text-center">
              <ProfilePictureUpload
                userId={user?.id}
                onUpdate={() => {
                  // Refresh profile picture after upload
                  setUser({ ...user });
                }}
              />
            </div>

            {/* Message Alert */}
            {message.text && (
              <div className={`mb-4 p-3 rounded-lg ${
                message.type === 'success' ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
              }`}>
                {message.text}
              </div>
            )}

            {/* Profile Information Card */}
            <div className="bg-white rounded-xl shadow-md p-6">
              <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
                <span>👤</span> Personal Information
              </h2>

              {editing ? (
                <form onSubmit={handleUpdateProfile} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-gray-700 font-medium mb-1">First Name *</label>
                      <input
                        type="text"
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-gray-700 font-medium mb-1">Last Name *</label>
                      <input
                        type="text"
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                        required
                      />
                    </div>
                  </div>
                  <div>
                    <label className="block text-gray-700 font-medium mb-1">Email</label>
                    <input
                      type="email"
                      value={user.email}
                      disabled
                      className="w-full border rounded-lg px-4 py-2 bg-gray-100 cursor-not-allowed"
                    />
                    <p className="text-xs text-gray-500 mt-1">Email cannot be changed</p>
                  </div>
                  <div>
                    <label className="block text-gray-700 font-medium mb-1">Phone Number</label>
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      className="w-full border rounded-lg px-4 py-2 focus:ring-2 focus:ring-[#2563EB] focus:border-transparent"
                    />
                  </div>
                  <div className="flex gap-3 pt-2">
                    <button
                      type="submit"
                      disabled={saving}
                      className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50"
                    >
                      {saving ? 'Saving...' : 'Save Changes'}
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        setEditing(false);
                        setFormData({
                          firstName: user.firstName,
                          lastName: user.lastName,
                          phone: user.phone || '',
                        });
                        setMessage({ type: '', text: '' });
                      }}
                      className="bg-gray-300 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-400"
                    >
                      Cancel
                    </button>
                  </div>
                </form>
              ) : (
                <div className="space-y-3">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <p className="text-gray-500 text-sm">Full Name</p>
                      <p className="font-medium">{user.firstName} {user.lastName}</p>
                    </div>
                    <div>
                      <p className="text-gray-500 text-sm">Email Address</p>
                      <p className="font-medium">{user.email}</p>
                    </div>
                    <div>
                      <p className="text-gray-500 text-sm">Phone Number</p>
                      <p className="font-medium">{user.phone || 'Not provided'}</p>
                    </div>
                    <div>
                      <p className="text-gray-500 text-sm">Account Type</p>
                      <p className="font-medium">
                        <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs">
                          {user.userType}
                        </span>
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-500 text-sm">User ID</p>
                      <p className="font-mono text-sm">{user.id}</p>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* Change Password Button */}
            <div className="mt-6 text-center">
              <button
                onClick={() => navigate('/change-password')}
                className="text-[#2563EB] hover:underline flex items-center justify-center gap-2"
              >
                <span>🔒</span> Change Password
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;