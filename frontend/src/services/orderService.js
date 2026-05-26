import apiClient from './apiClient.js';
class OrderService {
  /** Fetches the line items for a given order. */
  async getOrderItems(orderId) {
    try {
      return await apiClient.get(`/api/v1/order/${orderId}/items`);
    } catch (error) {
      throw new Error(error.message || 'Unable to load order items.');
    }
  }

  /** Fetches orders by phone number and maps them to a display shape. */
  async getOrdersByPhone(phoneNumber) {
    try {
      const orders = await apiClient.get(
        `/api/v1/order/phoneNumber/${encodeURIComponent(phoneNumber)}`,
      );
      return orders.map((order) => ({
        id: order.orderId?.toString() || '',
        summary: `Pickup at ${order.pickupTime || '--:--'}`,
        arrivalTime: order.pickupTime || '--:--',
        status: order.orderStatus || 'ACCEPTED',
        orderDate: order.orderDate || null,
        pickupTime: order.pickupTime || null,
        totalAmount: Number(order.totalAmount || 0),
        items: order.items || [],
      }));
    } catch (error) {
      throw new Error(error.message || 'Unable to load your orders.');
    }
  }
  /**
   * Pay with HorsePay via backend
   * @param {object} paymentPayload
   * @returns {Promise<object>} HorsePay API response
   */
  async payWithHorsePay(paymentPayload) {
    try {
      return await apiClient.post('/api/v1/checkout/pay', paymentPayload);
    } catch (error) {
      throw new Error(error.message || 'Unable to process payment right now.');
    }
  }

  /** Archives or unarchives an order. */
  async archiveOrder(orderId, archive) {
    try {
      return await apiClient.put(`/api/v1/order/${orderId}/archive?archive=${archive}`);
    } catch (error) {
      throw new Error(error.message || 'Unable to archive order right now.');
    }
  }
  /** Submits a new order. */
  async createOrder(orderData) {
    try {
      const response = await apiClient.post('/api/v1/order', orderData);
      return response;
    } catch (error) {
      throw error;
    }
  }

  /** Fetches a single order by ID. */
  async getOrderById(id) {
    try {
      return await apiClient.get(`/api/v1/order/${id}`);
    } catch (error) {
      throw new Error(error.message || 'Unable to load this order right now.');
    }
  }

  /** Fetches all orders and maps them to a display shape. */
  async getAllOrders() {
    try {
      const orders = await apiClient.get('/api/v1/order');
      return orders.map((order) => ({
        id: order.orderId?.toString() || '',
        summary: `Pickup at ${order.pickupTime || '--:--'}`,
        arrivalTime: order.pickupTime || '--:--',
        status: order.orderStatus || 'ACCEPTED',
        isArchived: order.isArchived || false,
        orderDate: order.orderDate || null,
        pickupTime: order.pickupTime || null,
        totalAmount: Number(order.totalAmount || 0),
      }));
    } catch (error) {
      throw new Error(error.message || 'Unable to load orders right now.');
    }
  }

  /** Fetches orders filtered by status and maps them to a display shape. */
  async getOrdersByStatus(status) {
    try {
      const orders = await apiClient.get(`/api/v1/order/status/${status}`);
      return orders.map((order) => ({
        id: order.orderId?.toString() || '',
        summary: `Pickup at ${order.pickupTime || '--:--'}`,
        arrivalTime: order.pickupTime || '--:--',
        status: order.orderStatus || 'ACCEPTED',
        isArchived: order.isArchived || false,
        orderDate: order.orderDate || null,
        pickupTime: order.pickupTime || null,
        totalAmount: Number(order.totalAmount || 0),
      }));
    } catch (error) {
      throw new Error(error.message || 'Unable to load orders by status right now.');
    }
  }

  /** Updates the status of an existing order. */
  async updateOrderStatus(orderId, staffId, status) {
    try {
      return await apiClient.put(`/api/v1/order/${orderId}/${staffId}/status`, {
        orderStatus: status,
      });
    } catch (error) {
      throw new Error(error.message || 'Unable to update order status right now.');
    }
  }
}

export default new OrderService();
