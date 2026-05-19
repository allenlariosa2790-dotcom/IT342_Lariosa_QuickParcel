import apiClient from '../../shared/utils/apiClient';

export const login = async (data) => {
  try {
    const response = await apiClient.post('/auth/login', data);
    return response.data;
  } catch (error) {
    console.error('Login error:', error.response?.data || error.message);
    throw error;
  }
};

export const register = async (data) => {
  try {
    const response = await apiClient.post('/auth/register', data);
    return response.data;
  } catch (error) {
    console.error('Register error:', error.response?.data || error.message);
    throw error;
  }
};