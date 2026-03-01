import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import cartService from '../services/cartService';
import toast from 'react-hot-toast';

// ============ Async Thunks ============

export const fetchCart = createAsyncThunk('cart/fetch', async (_, { rejectWithValue }) => {
  try {
    const response = await cartService.getCart();
    return response.data;
  } catch (error) {
    // Don't show error for cart fetch failures (e.g. after payment when cart is cleared)
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch cart');
  }
});

export const addToCart = createAsyncThunk('cart/add', async ({ productId, quantity = 1 }, { rejectWithValue }) => {
  try {
    const response = await cartService.addToCart(productId, quantity);
    toast.success('Added to cart!');
    return response.data;
  } catch (error) {
    const msg = error.response?.data?.message || 'Failed to add to cart';
    toast.error(msg);
    return rejectWithValue(msg);
  }
});

export const updateCartItem = createAsyncThunk('cart/update', async ({ productId, quantity }, { rejectWithValue }) => {
  try {
    const response = await cartService.updateQuantity(productId, quantity);
    return response.data;
  } catch (error) {
    const msg = error.response?.data?.message || 'Failed to update cart';
    toast.error(msg);
    return rejectWithValue(msg);
  }
});

export const removeFromCart = createAsyncThunk('cart/remove', async (productId, { rejectWithValue }) => {
  try {
    const response = await cartService.removeItem(productId);
    toast.success('Removed from cart');
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to remove item');
  }
});

export const clearCart = createAsyncThunk('cart/clear', async (_, { rejectWithValue }) => {
  try {
    await cartService.clearCart();
    toast.success('Cart cleared');
    return null;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to clear cart');
  }
});

// ============ Slice ============

const cartSlice = createSlice({
  name: 'cart',
  initialState: {
    items: [],
    totalItems: 0,
    subtotal: 0,
    loading: false,
    error: null,
  },
  reducers: {
    resetCart: (state) => {
      state.items = [];
      state.totalItems = 0;
      state.subtotal = 0;
    },
  },
  extraReducers: (builder) => {
    const handleCartResponse = (state, action) => {
      const data = action.payload;
      if (data) {
        state.items = data.items || [];
        state.totalItems = data.totalItems || 0;
        state.subtotal = data.subtotal || 0;
      } else {
        state.items = [];
        state.totalItems = 0;
        state.subtotal = 0;
      }
      state.loading = false;
      state.error = null;
    };

    builder
      .addCase(fetchCart.pending, (state) => { state.loading = true; })
      .addCase(fetchCart.fulfilled, handleCartResponse)
      .addCase(fetchCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(addToCart.pending, (state) => { state.loading = true; })
      .addCase(addToCart.fulfilled, handleCartResponse)
      .addCase(addToCart.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(updateCartItem.fulfilled, handleCartResponse)
      .addCase(removeFromCart.fulfilled, handleCartResponse)
      .addCase(clearCart.fulfilled, (state) => {
        state.items = [];
        state.totalItems = 0;
        state.subtotal = 0;
        state.loading = false;
      });
  },
});

export const { resetCart } = cartSlice.actions;
export default cartSlice.reducer;