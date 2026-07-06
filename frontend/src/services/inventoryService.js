import axiosClient from '../api/axiosClient';

const unwrap = (response) => response.data.data;

export const inventoryService = {
  getByProductId: async (productId) => {
    const response = await axiosClient.get(`/api/inventory/${productId}`);
    return unwrap(response);
  },

  updateStock: async (productId, availableQuantity) => {
    const response = await axiosClient.put(`/api/inventory/${productId}`, {
      availableQuantity,
    });
    return unwrap(response);
  },
};
