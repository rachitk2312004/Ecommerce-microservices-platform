import axiosClient from '../api/axiosClient';

const unwrap = (response) => response.data.data;

export const orderService = {
  createOrder: async (orderData) => {
    const response = await axiosClient.post('/api/orders', orderData);
    return unwrap(response);
  },

  getOrderById: async (id) => {
    const response = await axiosClient.get(`/api/orders/${id}`);
    return unwrap(response);
  },

  getMyOrders: async () => {
    const response = await axiosClient.get('/api/orders/my-orders');
    return unwrap(response);
  },

  cancelOrder: async (id) => {
    const response = await axiosClient.post(`/api/orders/${id}/cancel`);
    return unwrap(response);
  },
};
