import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import productService from '../services/productService';

// ============ Async Thunks ============

export const fetchProducts = createAsyncThunk('products/fetch', async (params, { rejectWithValue }) => {
  try {
    const response = await productService.getProducts(params);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch products');
  }
});

export const fetchProductById = createAsyncThunk('products/fetchById', async (id, { rejectWithValue }) => {
  try {
    const response = await productService.getProductById(id);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Product not found');
  }
});

export const fetchFeaturedProducts = createAsyncThunk('products/fetchFeatured', async (_, { rejectWithValue }) => {
  try {
    const response = await productService.getFeaturedProducts(8);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch featured products');
  }
});

export const fetchCategories = createAsyncThunk('products/fetchCategories', async (_, { rejectWithValue }) => {
  try {
    const response = await productService.getCategories();
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Failed to fetch categories');
  }
});

export const searchProducts = createAsyncThunk('products/search', async ({ query, params }, { rejectWithValue }) => {
  try {
    const response = await productService.searchProducts(query, params);
    return response.data;
  } catch (error) {
    return rejectWithValue(error.response?.data?.message || 'Search failed');
  }
});

// ============ Slice ============

const productSlice = createSlice({
  name: 'products',
  initialState: {
    items: [],
    featured: [],
    categories: [],
    currentProduct: null,
    totalElements: 0,
    totalPages: 0,
    currentPage: 0,
    loading: false,
    productLoading: false,
    error: null,
  },
  reducers: {
    clearCurrentProduct: (state) => {
      state.currentProduct = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Products
      .addCase(fetchProducts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchProducts.fulfilled, (state, action) => {
        state.loading = false;
        const data = action.payload;
        state.items = data.content || data || [];
        state.totalElements = data.totalElements || 0;
        state.totalPages = data.totalPages || 0;
        state.currentPage = data.number || 0;
      })
      .addCase(fetchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch by ID
      .addCase(fetchProductById.pending, (state) => {
        state.productLoading = true;
      })
      .addCase(fetchProductById.fulfilled, (state, action) => {
        state.productLoading = false;
        state.currentProduct = action.payload;
      })
      .addCase(fetchProductById.rejected, (state, action) => {
        state.productLoading = false;
        state.error = action.payload;
      })
      // Featured
      .addCase(fetchFeaturedProducts.fulfilled, (state, action) => {
        const data = action.payload;
        state.featured = data.content || data || [];
      })
      // Categories
      .addCase(fetchCategories.fulfilled, (state, action) => {
        state.categories = action.payload || [];
      })
      // Search
      .addCase(searchProducts.pending, (state) => {
        state.loading = true;
      })
      .addCase(searchProducts.fulfilled, (state, action) => {
        state.loading = false;
        const data = action.payload;
        // FIXED: Handling both standard Mongo response and Elasticsearch SearchResultResponse
        state.items = data.products || data.content || data || [];
        state.totalElements = data.totalHits || data.totalElements || data.total || 0;
        state.totalPages = data.totalPages || 0;
        state.currentPage = data.currentPage || data.number || 0;
      })
      .addCase(searchProducts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearCurrentProduct } = productSlice.actions;
export default productSlice.reducer;