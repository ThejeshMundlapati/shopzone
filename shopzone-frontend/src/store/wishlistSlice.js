import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../services/api';
import toast from 'react-hot-toast';

// ============ Async Thunks ============

export const fetchWishlist = createAsyncThunk('wishlist/fetch', async (_, { rejectWithValue }) => {
  try {
    const response = await api.get('/api/wishlist');
    return response.data.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch wishlist');
  }
});

export const addToWishlist = createAsyncThunk('wishlist/add', async (productId, { rejectWithValue }) => {
  try {
    const response = await api.post(`/api/wishlist/add/${productId}`);
    toast.success('Added to wishlist!');
    return response.data.data;
  } catch (error) {
    const msg = error.response?.data?.message || 'Failed to add to wishlist';
    toast.error(msg);
    return rejectWithValue(msg);
  }
});

export const removeFromWishlist = createAsyncThunk('wishlist/remove', async (productId, { rejectWithValue }) => {
  try {
    const response = await api.delete(`/api/wishlist/remove/${productId}`);
    toast.success('Removed from wishlist');
    return response.data.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to remove from wishlist');
  }
});

export const moveToCart = createAsyncThunk('wishlist/moveToCart', async (productId, { rejectWithValue }) => {
  try {
    const response = await api.post(`/api/wishlist/move-to-cart/${productId}`);
    toast.success('Moved to cart!');
    return { wishlistData: response.data.data, productId };
  } catch (error) {
    const msg = error.response?.data?.message || 'Failed to move to cart';
    toast.error(msg);
    return rejectWithValue(msg);
  }
});

export const moveAllToCart = createAsyncThunk('wishlist/moveAllToCart', async (_, { rejectWithValue }) => {
  try {
    const response = await api.post('/api/wishlist/move-all-to-cart');
    toast.success('All items moved to cart!');
    return response.data.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to move items');
  }
});

// ============ Slice ============

const wishlistSlice = createSlice({
  name: 'wishlist',
  initialState: {
    items: [],
    itemCount: 0,
    loading: false,
    error: null,
  },
  reducers: {
    resetWishlist: (state) => {
      state.items = [];
      state.itemCount = 0;
    },
  },
  extraReducers: (builder) => {
    const handleWishlistResponse = (state, action) => {
      const data = action.payload;
      if (data) {
        state.items = data.items || [];
        state.itemCount = data.itemCount || data.items?.length || 0;
      }
      state.loading = false;
    };

    builder
      .addCase(fetchWishlist.pending, (state) => { state.loading = true; })
      .addCase(fetchWishlist.fulfilled, handleWishlistResponse)
      .addCase(fetchWishlist.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(addToWishlist.fulfilled, handleWishlistResponse)
      .addCase(removeFromWishlist.fulfilled, handleWishlistResponse)
      .addCase(moveToCart.fulfilled, (state, action) => {
        state.items = state.items.filter((i) => i.productId !== action.payload.productId);
        state.itemCount = state.items.length;
      })
      .addCase(moveAllToCart.fulfilled, (state) => {
        state.items = [];
        state.itemCount = 0;
      });
  },
});

export const { resetWishlist } = wishlistSlice.actions;
export default wishlistSlice.reducer;