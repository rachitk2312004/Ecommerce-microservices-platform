import axiosClient from '../api/axiosClient';

const unwrap = (response) => response.data.data;

export const productService = {
  getProducts: async (page = 0, size = 20) => {
    const response = await axiosClient.get('/api/products', {
      params: { page, size },
    });
    return unwrap(response);
  },

  getProductById: async (id) => {
    const response = await axiosClient.get(`/api/products/${id}`);
    return unwrap(response);
  },

  searchProducts: async (name, page = 0, size = 20) => {
    const response = await axiosClient.get('/api/products/search', {
      params: { name, page, size },
    });
    return unwrap(response);
  },

  getProductsByCategory: async (categoryId) => {
    const response = await axiosClient.get(`/api/products/category/${categoryId}`);
    return unwrap(response);
  },

  createProduct: async (productData) => {
    const response = await axiosClient.post('/api/products', productData);
    return unwrap(response);
  },

  updateProduct: async (id, productData) => {
    const response = await axiosClient.put(`/api/products/${id}`, productData);
    return unwrap(response);
  },

  deleteProduct: async (id) => {
    const response = await axiosClient.delete(`/api/products/${id}`);
    return response.data;
  },

  getCategories: async () => {
    const response = await axiosClient.get('/api/categories');
    return unwrap(response);
  },

  createCategory: async (categoryData) => {
    const response = await axiosClient.post('/api/categories', categoryData);
    return unwrap(response);
  },

  updateCategory: async (id, categoryData) => {
    const response = await axiosClient.put(`/api/categories/${id}`, categoryData);
    return unwrap(response);
  },

  deleteCategory: async (id) => {
    const response = await axiosClient.delete(`/api/categories/${id}`);
    return response.data;
  },
};
