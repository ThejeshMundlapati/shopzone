import api from './api';

const productService = {
  // ============ Products ============

  getProducts: async (params = {}) => {
    const response = await api.get('/api/products', { params });
    return response.data;
  },

  getProductById: async (id) => {
    const response = await api.get(`/api/products/${id}`);
    return response.data;
  },

  getProductBySlug: async (slug) => {
    const response = await api.get(`/api/products/slug/${slug}`);
    return response.data;
  },

  getFeaturedProducts: async (limit = 8) => {
    const response = await api.get('/api/products', {
      params: { featured: true, size: limit, sortBy: 'createdAt', sortDir: 'desc' },
    });
    return response.data;
  },

  getProductsByCategory: async (categoryId, params = {}) => {
    const response = await api.get(`/api/products/category/${categoryId}`, { params });
    return response.data;
  },

  // ============ Categories ============

  getCategories: async () => {
    const response = await api.get('/api/categories');
    return response.data;
  },

  getCategoryById: async (id) => {
    const response = await api.get(`/api/categories/${id}`);
    return response.data;
  },

  getCategoryTree: async () => {
    const response = await api.get('/api/categories/tree');
    return response.data;
  },

  // ============ Search (Elasticsearch) ============

  searchProducts: async (query, params = {}) => {
    const response = await api.get('/api/search', {
      params: { q: query, ...params },
    });
    return response.data;
  },

  getAutocomplete: async (query) => {
    const response = await api.get('/api/search/autocomplete', {
      params: { q: query, limit: 5 },
    });
    return response.data;
  },

  getSimilarProducts: async (productId, limit = 4) => {
    const response = await api.get(`/api/search/similar/${productId}`, {
      params: { limit },
    });
    return response.data;
  },
};

export default productService;