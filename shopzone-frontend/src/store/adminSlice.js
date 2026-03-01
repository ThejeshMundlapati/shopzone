import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import adminService from '../services/adminService';

// ============ Dashboard Thunks ============

export const fetchDashboardStats = createAsyncThunk(
  'admin/fetchDashboardStats',
  async (_, { rejectWithValue }) => {
    try {
      const res = await adminService.getDashboardStats();
      return res.data.data;
    } catch (e) {
      return rejectWithValue(e.response?.data?.message || 'Failed to fetch stats');
    }
  }
);

export const fetchRecentOrders = createAsyncThunk(
  'admin/fetchRecentOrders',
  async (limit = 10, { rejectWithValue }) => {
    try {
      const res = await adminService.getRecentOrders(limit);
      return res.data.data;
    } catch (e) {
      return rejectWithValue(e.response?.data?.message || 'Failed to fetch recent orders');
    }
  }
);

export const fetchTopProducts = createAsyncThunk(
  'admin/fetchTopProducts',
  async (limit = 10, { rejectWithValue }) => {
    try {
      const res = await adminService.getTopProducts(limit);
      return res.data.data;
    } catch (e) {
      return rejectWithValue(e.response?.data?.message || 'Failed to fetch top products');
    }
  }
);

export const fetchTopCustomers = createAsyncThunk(
  'admin/fetchTopCustomers',
  async (limit = 10, { rejectWithValue }) => {
    try {
      const res = await adminService.getTopCustomers(limit);
      return res.data.data;
    } catch (e) {
      return rejectWithValue(e.response?.data?.message || 'Failed to fetch top customers');
    }
  }
);

// ============ Slice ============

const adminSlice = createSlice({
  name: 'admin',
  initialState: {
    dashboardStats: null,
    recentOrders: [],
    topProducts: [],
    topCustomers: [],
    loading: false,
    error: null,
  },
  reducers: {
    clearAdminError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Dashboard Stats
      .addCase(fetchDashboardStats.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchDashboardStats.fulfilled, (state, action) => {
        state.loading = false;
        state.dashboardStats = action.payload;
      })
      .addCase(fetchDashboardStats.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Recent Orders
      .addCase(fetchRecentOrders.fulfilled, (state, action) => {
        state.recentOrders = action.payload;
      })
      // Top Products
      .addCase(fetchTopProducts.fulfilled, (state, action) => {
        state.topProducts = action.payload;
      })
      // Top Customers
      .addCase(fetchTopCustomers.fulfilled, (state, action) => {
        state.topCustomers = action.payload;
      });
  },
});

export const { clearAdminError } = adminSlice.actions;
export default adminSlice.reducer;