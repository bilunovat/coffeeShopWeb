import apiClient from './apiClient.js';

class CustomerService {
  /** Creates a new customer. */
  async createCustomer(customerData) {
    try {
      return await apiClient.post('/api/v1/customer', customerData);
    } catch (error) {
      throw new Error(error.message || 'Unable to add customer details right now.');
    }
  }

  /** Deletes a customer by ID. */
  async deleteCustomer(customerId) {
    try {
      return await apiClient.delete(`/api/v1/customer/${customerId}`);
    } catch (error) {
      throw new Error(error.message || 'Unable to remove customer details right now.');
    }
  }
}

export default new CustomerService();
