import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Public endpoints that don't require authentication
const publicEndpoints = [
  '/auth/register',
  '/auth/login',
  '/test'
];

api.interceptors.request.use(
  (config) => {
    // Check if this is a public endpoint
    const isPublicEndpoint = publicEndpoints.some(endpoint => config.url.includes(endpoint));

    // Only add token for non-public endpoints
    if (!isPublicEndpoint) {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }

    console.log('Making request to:', config.url);
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error.response?.data || error.message);
    return Promise.reject(error);
  }
);

export default api;