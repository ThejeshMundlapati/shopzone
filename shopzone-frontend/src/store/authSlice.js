  import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
  import authService from '../services/authService';
  import { setTokens, clearTokens } from '../services/api';

  // ============ Async Thunks ============

  export const login = createAsyncThunk('auth/login', async (credentials, { rejectWithValue }) => {
    try {
      const response = await authService.login(credentials);
      const { accessToken, refreshToken } = response.data;
      setTokens(accessToken, refreshToken);
      // Fetch user profile after login
      const profile = await authService.getProfile();
      localStorage.setItem('user', JSON.stringify(profile.data));
      return { user: profile.data, token: accessToken };
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Login failed');
    }
  });

  export const register = createAsyncThunk('auth/register', async (data, { rejectWithValue }) => {
    try {
      const response = await authService.register(data);
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Registration failed');
    }
  });

  export const fetchProfile = createAsyncThunk('auth/fetchProfile', async (_, { rejectWithValue }) => {
    try {
      const response = await authService.getProfile();
      localStorage.setItem('user', JSON.stringify(response.data));
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch profile');
    }
  });

  export const updateProfile = createAsyncThunk('auth/updateProfile', async (data, { rejectWithValue }) => {
    try {
      const response = await authService.updateProfile(data);
      localStorage.setItem('user', JSON.stringify(response.data));
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update profile');
    }
  });

  export const logout = createAsyncThunk('auth/logout', async () => {
    await authService.logout();
    clearTokens();
  });

  // ============ Slice ============

  const storedUser = localStorage.getItem('user');
  const storedToken = localStorage.getItem('accessToken');

  const authSlice = createSlice({
    name: 'auth',
    initialState: {
      user: storedUser ? JSON.parse(storedUser) : null,
      token: storedToken || null,
      isAuthenticated: !!storedToken,
      loading: false,
      error: null,
      registerSuccess: false,
    },
    reducers: {
      clearError: (state) => {
        state.error = null;
      },
      clearRegisterSuccess: (state) => {
        state.registerSuccess = false;
      },
    },
    extraReducers: (builder) => {
      builder
        // Login
        .addCase(login.pending, (state) => {
          state.loading = true;
          state.error = null;
        })
        .addCase(login.fulfilled, (state, action) => {
          state.loading = false;
          state.user = action.payload.user;
          state.token = action.payload.token;
          state.isAuthenticated = true;
          state.error = null;
        })
        .addCase(login.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload;
        })
        // Register
        .addCase(register.pending, (state) => {
          state.loading = true;
          state.error = null;
          state.registerSuccess = false;
        })
        .addCase(register.fulfilled, (state) => {
          state.loading = false;
          state.registerSuccess = true;
        })
        .addCase(register.rejected, (state, action) => {
          state.loading = false;
          state.error = action.payload;
        })
        // Fetch Profile
        .addCase(fetchProfile.fulfilled, (state, action) => {
          state.user = action.payload;
        })
        // Update Profile
        .addCase(updateProfile.fulfilled, (state, action) => {
          state.user = action.payload;
        })
        // Logout
        .addCase(logout.fulfilled, (state) => {
          state.user = null;
          state.token = null;
          state.isAuthenticated = false;
        });
    },
  });

  export const { clearError, clearRegisterSuccess } = authSlice.actions;
  export default authSlice.reducer;