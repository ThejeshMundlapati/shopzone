import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import orderService from '../services/orderService';

// ============ Async Thunks ============

export const fetchOrders = createAsyncThunk('orders/fetch', async (params = {}, { rejectWithValue }) => {
  try {
    const response = await orderService.getOrders(params);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch orders');
  }
});

export const fetchOrderDetail = createAsyncThunk('orders/fetchDetail', async (orderNumber, { rejectWithValue }) => {
  try {
    const response = await orderService.getOrderByNumber(orderNumber);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Order not found');
  }
});

export const cancelOrder = createAsyncThunk('orders/cancel', async ({ orderNumber, reason }, { rejectWithValue }) => {
  try {
    const response = await orderService.cancelOrder(orderNumber, reason);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to cancel order');
  }
});

// ============ Slice ============

const orderSlice = createSlice({
  name: 'orders',
  initialState: {
    list: [],
    currentOrder: null,
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    loading: false,
    detailLoading: false,
    error: null,
  },
  reducers: {
    clearCurrentOrder: (state) => {
      state.currentOrder = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Orders
      .addCase(fetchOrders.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchOrders.fulfilled, (state, action) => {
        state.loading = false;
        const data = action.payload;
        state.list = data.content || data || [];
        state.totalElements = data.totalElements || 0;
        state.totalPages = data.totalPages || 0;
        state.currentPage = data.number || 0;
      })
      .addCase(fetchOrders.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch Detail
      .addCase(fetchOrderDetail.pending, (state) => {
        state.detailLoading = true;
      })
      .addCase(fetchOrderDetail.fulfilled, (state, action) => {
        state.detailLoading = false;
        state.currentOrder = action.payload;
      })
      .addCase(fetchOrderDetail.rejected, (state, action) => {
        state.detailLoading = false;
        state.error = action.payload;
      })
      // Cancel
      .addCase(cancelOrder.fulfilled, (state, action) => {
        state.currentOrder = action.payload;
        // Also update in list
        const idx = state.list.findIndex((o) => o.orderNumber === action.payload?.orderNumber);
        if (idx !== -1) state.list[idx] = action.payload;
      });
  },
});

export const { clearCurrentOrder } = orderSlice.actions;
export default orderSlice.reducer;