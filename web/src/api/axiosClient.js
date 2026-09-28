import axios from 'axios';

// Create a centralized Axios instance
const axiosClient = axios.create({
  // Point this to your C# Backend API using Vite's Environment Variables
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:5000/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor: Automatically attach JWT token to every request if it exists
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Catch 401 (Unauthorized) and 403 (Forbidden) globally
axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {

    const originalRequest = error.config;

    // Ignore 401s from the login endpoint itself so the UI can show the error message!
    if (originalRequest && originalRequest.url && originalRequest.url.includes('/auth/login')) {
      return Promise.reject(error);
    }

    // Global DataAnnotation Validation Error Extractor
    // Intercepts 400 Bad Request from C# and formats the `.errors` dictionary into a readable string
    if (error.response && error.response.status === 400 && error.response.data && error.response.data.errors) {
      const validationErrors = error.response.data.errors;
      const errorMessages = Object.values(validationErrors)
        .flat()
        .join(' | '); // Join all validation messages with a pipe
      
      // Inject the extracted message back into the error object so our JSX files can read it easily
      error.response.data.message = errorMessages;
    }

    // If the FAT Backend rejects the request due to missing/expired token or invalid role
    if (error.response && (error.response.status === 401 || error.response.status === 403)) {
      // Clear invalid credentials
      localStorage.removeItem('token');
      localStorage.removeItem('userRole');
      localStorage.removeItem('userName');
      localStorage.removeItem('menu');

      // Force redirect to login page (Thin Client reacting to backend security)
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
