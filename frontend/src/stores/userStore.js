import { defineStore } from 'pinia';
import { ref } from 'vue';
import authService from '../services/authService.js';

export const useUserStore = defineStore('user', () => {
  // State
  const staffUser = ref(null);
  const authToken = ref(localStorage.getItem('authToken') || null);
  const isStaffLoggedIn = ref(!!authToken.value);
  const userError = ref(null);
  const isLoading = ref(false);

  // Actions

  /** Authenticate staff and persist JWT token to localStorage. */
  const staffLogin = async (credentials) => {
    isLoading.value = true;
    userError.value = null;
    try {
      const response = await authService.login(credentials);
      authToken.value = response.token;
      // Build user profile, falling back to credential values when API doesn't return them
      staffUser.value = {
        id: response.id || credentials.username,
        username: credentials.username,
        name: response.username || credentials.username.split('@')[0],
        role: response.role || 'Staff',
        email: credentials.username,
      };
      isStaffLoggedIn.value = true;
      localStorage.setItem('authToken', response.token);
      return response;
    } catch (error) {
      userError.value = error.message || 'Login failed';
      isStaffLoggedIn.value = false;
      throw error;
    } finally {
      isLoading.value = false;
    }
  };

  const staffLogout = () => {
    authService.logout();
    staffUser.value = null;
    authToken.value = null;
    isStaffLoggedIn.value = false;
    localStorage.removeItem('authToken');
  };

  const clearError = () => {
    userError.value = null;
  };

  return {
    staffUser,
    authToken,
    isStaffLoggedIn,
    userError,
    isLoading,
    staffLogin,
    staffLogout,
    clearError,
  };
});
