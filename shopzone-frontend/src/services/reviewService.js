import api from './api';

const reviewService = {
  getProductReviews: async (productId, params = {}) => {
    const response = await api.get(`/api/reviews/product/${productId}`, { params });
    return response.data;
  },

  getReviewStats: async (productId) => {
    const response = await api.get(`/api/reviews/product/${productId}/stats`);
    return response.data;
  },

  createReview: async (data) => {
    const response = await api.post('/api/reviews', data);
    return response.data;
  },

  updateReview: async (id, data) => {
    const response = await api.put(`/api/reviews/${id}`, data);
    return response.data;
  },

  deleteReview: async (id) => {
    const response = await api.delete(`/api/reviews/${id}`);
    return response.data;
  },

  canReview: async (productId) => {
    const response = await api.get(`/api/reviews/product/${productId}/can-review`);
    return response.data;
  },

  getMyReviews: async (params = {}) => {
    const response = await api.get('/api/reviews/my-reviews', { params });
    return response.data;
  },

  markHelpful: async (reviewId) => {
    const response = await api.post(`/api/reviews/${reviewId}/helpful`);
    return response.data;
  },
};

export default reviewService;