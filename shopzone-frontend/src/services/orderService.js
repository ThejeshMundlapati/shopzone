import api from './api';

const orderService = {
  // ============ Checkout ============

  validateCart: async () => {
    const response = await api.get('/api/checkout/validate');
    return response.data;
  },

  getCheckoutPreview: async (addressId) => {
    const response = await api.get('/api/checkout/preview', {
      params: { addressId },
    });
    return response.data;
  },

  placeOrder: async (data) => {
    const response = await api.post('/api/checkout/place-order', data);
    return response.data;
  },

  // ============ Orders ============

  getOrders: async (params = {}) => {
    const response = await api.get('/api/orders', { params });
    return response.data;
  },

  getOrderByNumber: async (orderNumber) => {
    const response = await api.get(`/api/orders/${orderNumber}`);
    return response.data;
  },

  cancelOrder: async (orderNumber, reason) => {
    const response = await api.post(`/api/orders/${orderNumber}/cancel`, { reason });
    return response.data;
  },

  // ============ Addresses ============

  getAddresses: async () => {
    const response = await api.get('/api/addresses');
    return response.data;
  },

  createAddress: async (data) => {
    const response = await api.post('/api/addresses', data);
    return response.data;
  },

  updateAddress: async (id, data) => {
    const response = await api.put(`/api/addresses/${id}`, data);
    return response.data;
  },

  deleteAddress: async (id) => {
    const response = await api.delete(`/api/addresses/${id}`);
    return response.data;
  },

  setDefaultAddress: async (id) => {
    const response = await api.patch(`/api/addresses/${id}/set-default`);
    return response.data;
  },
};

export default orderService;