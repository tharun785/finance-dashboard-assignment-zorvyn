import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000,
});

// Add token to requests
api.interceptors.request.use(
  (config) => {
    console.log('📤 Request:', config.method.toUpperCase(), config.url);
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// Handle response errors
api.interceptors.response.use(
  (response) => {
    console.log('📥 Response:', response.status, response.config.url);
    return response;
  },
  (error) => {
    console.error('❌ Response Error:', error.message);
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ============= AUTH APIS =============
export const login = async (username, password) => {
  try {
    const response = await api.post('/auth/login', { username, password });
    return response;
  } catch (error) {
    console.error('Login error:', error);
    throw error;
  }
};

export const signup = async (userData) => {
  try {
    const response = await api.post('/auth/signup', userData);
    return response;
  } catch (error) {
    console.error('Signup error:', error);
    throw error;
  }
};

// ============= RECORD APIS =============
export const getRecords = async (filters = {}) => {
  try {
    const params = new URLSearchParams();
    if (filters.type) params.append('type', filters.type);
    if (filters.category) params.append('category', filters.category);
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);

    const queryString = params.toString();
    const url = `/records${queryString ? `?${queryString}` : ''}`;

    console.log('Fetching records from:', url);
    const response = await api.get(url);
    console.log('Records response:', response.data);
    return response;
  } catch (error) {
    console.error('Error fetching records:', error);
    throw error;
  }
};

export const createRecord = async (record) => {
  try {
    console.log('Creating record:', record);
    const response = await api.post('/records', record);
    console.log('Create record response:', response.data);
    return response;
  } catch (error) {
    console.error('Error creating record:', error);
    throw error;
  }
};

export const updateRecord = async (id, record) => {
  try {
    const response = await api.put(`/records/${id}`, record);
    return response;
  } catch (error) {
    console.error('Error updating record:', error);
    throw error;
  }
};

export const deleteRecord = async (id) => {
  try {
    const response = await api.delete(`/records/${id}`);
    return response;
  } catch (error) {
    console.error('Error deleting record:', error);
    throw error;
  }
};

// ============= DASHBOARD APIS =============
export const getDashboardSummary = () => {
  return api.get('/dashboard/summary');
};

export const getRecentActivity = () => {
  return api.get('/dashboard/recent-activity');
};

// ============= USER MANAGEMENT APIS =============
export const getAllUsers = () => {
  return api.get('/users');
};

export const updateUserStatus = (userId, active) => {
  return api.put(`/users/${userId}/status`, { active });
};

export const updateUserRoles = (userId, roles) => {
  return api.put(`/users/${userId}/roles`, { roles });
};

export const deleteUser = (userId) => {
  return api.delete(`/users/${userId}`);
};

export default api;