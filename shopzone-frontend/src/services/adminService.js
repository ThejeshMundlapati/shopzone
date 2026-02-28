import api from './api';

const adminService = {
  // ============ Dashboard ============
  getDashboardStats: () => api.get('/api/admin/dashboard/stats'),
  getRecentOrders: (limit = 10) => api.get(`/api/admin/dashboard/recent-orders?limit=${limit}`),
  getTopProducts: (limit = 10) => api.get(`/api/admin/dashboard/top-products?limit=${limit}`),
  getTopCustomers: (limit = 10) => api.get(`/api/admin/dashboard/top-customers?limit=${limit}`),

  // ============ Orders ============
  getAllOrders: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size) query.append('size', params.size);
    if (params.status) query.append('status', params.status);
    if (params.paymentStatus) query.append('paymentStatus', params.paymentStatus);
    if (params.startDate) query.append('startDate', params.startDate);
    if (params.endDate) query.append('endDate', params.endDate);
    if (params.sortBy) query.append('sortBy', params.sortBy);
    if (params.sortDir) query.append('sortDir', params.sortDir);
    return api.get(`/api/admin/orders?${query.toString()}`);
  },
  getOrderDetail: (orderNumber) => api.get(`/api/admin/orders/${orderNumber}`),
  updateOrderStatus: (orderNumber, data) => api.patch(`/api/admin/orders/${orderNumber}/status`, data),
  searchOrders: (query, page = 0, size = 20) =>
    api.get(`/api/admin/orders/search?query=${encodeURIComponent(query)}&page=${page}&size=${size}`),
  getOrderStats: () => api.get('/api/admin/orders/stats'),

  // ============ Products ============
  getAllProducts: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size) query.append('size', params.size);
    if (params.sortBy) query.append('sortBy', params.sortBy);
    if (params.sortDir) query.append('sortDir', params.sortDir);
    return api.get(`/api/products?${query.toString()}`);
  },
  getProductById: (id) => api.get(`/api/products/${id}`),
  createProduct: (data) => api.post('/api/products', data),
  updateProduct: (id, data) => api.put(`/api/products/${id}`, data),
  deleteProduct: (id) => api.delete(`/api/products/${id}`),
  uploadProductImages: (id, formData) =>
    api.post(`/api/products/${id}/images`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
  removeProductImage: (id, imageUrl) =>
    api.delete(`/api/products/${id}/images?imageUrl=${encodeURIComponent(imageUrl)}`),

  // ============ Categories ============
  getAllCategories: () => api.get('/api/categories'),
  getCategoryTree: () => api.get('/api/categories/tree'),
  getCategoryById: (id) => api.get(`/api/categories/${id}`),
  createCategory: (data) => api.post('/api/categories', data),
  updateCategory: (id, data) => api.put(`/api/categories/${id}`, data),
  deleteCategory: (id) => api.delete(`/api/categories/${id}`),

  // ============ Users ============
  getAllUsers: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size) query.append('size', params.size);
    if (params.sortBy) query.append('sortBy', params.sortBy);
    if (params.sortDir) query.append('sortDir', params.sortDir);
    if (params.role) query.append('role', params.role);
    if (params.search) query.append('search', params.search);
    return api.get(`/api/admin/users?${query.toString()}`);
  },
  getUserById: (userId) => api.get(`/api/admin/users/${userId}`),
  updateUser: (userId, data) => api.put(`/api/admin/users/${userId}`, data),
  enableUser: (userId) => api.patch(`/api/admin/users/${userId}/enable`),
  disableUser: (userId) => api.patch(`/api/admin/users/${userId}/disable`),
  lockUser: (userId) => api.patch(`/api/admin/users/${userId}/lock`),
  unlockUser: (userId) => api.patch(`/api/admin/users/${userId}/unlock`),
  changeUserRole: (userId, role) => api.patch(`/api/admin/users/${userId}/role?role=${role}`),
  getUserStatsSummary: () => api.get('/api/admin/users/stats/summary'),

  // ============ Reviews ============
  getProductReviews: (productId, params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size) query.append('size', params.size);
    return api.get(`/api/reviews/product/${productId}?${query.toString()}`);
  },
  adminDeleteReview: (reviewId) => api.delete(`/api/reviews/admin/${reviewId}`),

  // ============ Payments ============
  getAllPayments: (params = {}) => {
    const query = new URLSearchParams();
    if (params.page !== undefined) query.append('page', params.page);
    if (params.size) query.append('size', params.size);
    if (params.status) query.append('status', params.status);
    if (params.sortBy) query.append('sortBy', params.sortBy);
    if (params.sortDir) query.append('sortDir', params.sortDir);
    return api.get(`/api/admin/payments?${query.toString()}`);
  },
  getPaymentStats: () => api.get('/api/admin/payments/stats'),

  // ============ Reports ============
  getRevenueReport: (startDate, endDate) => {
    const query = new URLSearchParams();
    if (startDate) query.append('startDate', startDate);
    if (endDate) query.append('endDate', endDate);
    return api.get(`/api/admin/reports/revenue?${query.toString()}`);
  },
  getSalesReport: (startDate, endDate) => {
    const query = new URLSearchParams();
    if (startDate) query.append('startDate', startDate);
    if (endDate) query.append('endDate', endDate);
    return api.get(`/api/admin/reports/sales?${query.toString()}`);
  },
  getUserGrowthReport: (startDate, endDate) => {
    const query = new URLSearchParams();
    if (startDate) query.append('startDate', startDate);
    if (endDate) query.append('endDate', endDate);
    return api.get(`/api/admin/reports/user-growth?${query.toString()}`);
  },
};

export default adminService;