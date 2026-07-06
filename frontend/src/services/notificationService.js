import axiosClient from '../api/axiosClient';

const unwrap = (response) => response.data.data;

export const notificationService = {
  getByUserId: async (userId) => {
    const response = await axiosClient.get(`/api/notifications/user/${userId}`);
    return unwrap(response);
  },
};
