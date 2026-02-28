import api from './api';

const paymentService = {
  createPaymentIntent: async (orderNumber) => {
    const response = await api.post('/api/payments/create-intent', { orderNumber });
    return response.data;
  },

  getPaymentStatus: async (orderNumber) => {
    const response = await api.get(`/api/payments/${orderNumber}`);
    return response.data;
  },

  getPaymentHistory: async (params = {}) => {
    const response = await api.get('/api/payments/history', { params });
    return response.data;
  },

  checkRefundEligibility: async (orderNumber) => {
    const response = await api.get(`/api/payments/${orderNumber}/refund-eligibility`);
    return response.data;
  },
};

export default paymentService;