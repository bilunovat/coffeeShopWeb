import apiClient from './apiClient.js';

class AuthService {
  /** Authenticates with credentials and stores the returned token. */
  async login(credentials) {
    try {
      const response = await apiClient.post('/api/v1/auth/login', credentials);
      if (response.token) {
        apiClient.setAuthToken(response.token);
      }
      return response;
    } catch (error) {
      throw new Error(error.message || 'Unable to sign in right now.');
    }
  }

  /** Clears the stored auth token. */
  logout() {
    apiClient.clearAuthToken();
  }

  /** Returns true if an auth token is present. */
  isAuthenticated() {
    return !!apiClient.getAuthToken();
  }
}

export default new AuthService();
