import axiosClient from '../api/axiosClient';

const unwrap = (response) => response.data.data;

export const authService = {
  login: async (credentials) => {
    const response = await axiosClient.post('/api/auth/login', credentials);
    return unwrap(response);
  },

  register: async (userData) => {
    const response = await axiosClient.post('/api/auth/register', userData);
    return unwrap(response);
  },

  getProfile: async () => {
    const response = await axiosClient.get('/api/users/profile');
    return unwrap(response);
  },

  updateProfile: async (profileData) => {
    const response = await axiosClient.put('/api/users/profile', profileData);
    return unwrap(response);
  },

  changePassword: async (passwordData) => {
    const response = await axiosClient.put('/api/users/change-password', passwordData);
    return response.data;
  },
};
