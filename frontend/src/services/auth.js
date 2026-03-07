import api from './api';

export const login = async (data) => {
  try {
    const response = await api.post('/auth/login', data);
    console.log('Full API response:', response);
    console.log('Response data:', response.data);

    // Based on your earlier success response, the data structure is:
    // {
    //   email: "...",
    //   firstName: "...",
    //   id: 2,
    //   lastName: "...",
    //   token: "...",
    //   type: "Bearer",
    //   userType: "SENDER"
    // }

    // Return the entire response.data (not response.data.data)
    return response.data;
  } catch (error) {
    console.error('Login error details:', error.response?.data || error.message);
    throw error;
  }
};

export const register = async (data) => {
  try {
    const response = await api.post('/auth/register', data);
    return response.data;
  } catch (error) {
    console.error('Register error:', error.response?.data || error.message);
    throw error;
  }
};