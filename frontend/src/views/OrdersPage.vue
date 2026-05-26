<template>
  <div class="orders-page">
    <!-- Header -->
    <div class="orders-header-row">
      <h2 class="orders-title">Your Orders</h2>
      <button v-if="!showPhoneModal" class="reopen-modal-btn" @click="showPhoneModal = true">
        Enter Phone Number
      </button>
    </div>

    <!-- Phone Number Modal as component -->
    <PhoneNumberModal
      v-if="showPhoneModal"
      :show="showPhoneModal"
      :is-loading="isLoading"
      :error="modalError"
      :phone-number="phoneNumber"
      @submit="onPhoneModalSubmit"
      @close="showPhoneModal = false"
    />

    <!-- Loading State -->
    <div v-if="isLoading" class="state-wrap">
      <Loader2 size="26" class="spin" />
      <p>Loading your orders...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="!orders.length" class="state-wrap empty">
      <Package size="24" />
      <p>No orders yet. Place your first one from the menu.</p>
    </div>

    <!-- Orders List -->
    <div v-else class="orders-list">
      <div
        v-for="order in orders"
        :key="order.id"
        class="order-card"
        role="button"
        tabindex="0"
        @click="toggleOrder(order.id)"
        @keydown.enter.prevent="toggleOrder(order.id)"
      >
        <div class="order-main">
          <div>
            <h3>Order #{{ order.id }}</h3>
            <p>{{ order.summary || 'Order placed' }}</p>
            <div class="order-meta">
              <small v-if="order.totalAmount !== undefined"
                >Total: £{{ order.totalAmount.toFixed(2) }}</small
              >
            </div>
          </div>

          <span class="status" :class="statusClass(order.status)">
            {{ formatStatus(order.status) }}
          </span>
        </div>

        <div v-if="isExpanded(order.id)" class="order-details">
          <h4>Items</h4>
          <ul v-if="orderItemsMap[order.id]?.length" class="items-list">
            <li
              v-for="(item, index) in orderItemsMap[order.id]"
              :key="`${order.id}-${index}`"
              class="item-row"
            >
              <span>{{ item.itemName || 'Item' }} ({{ item.size || 'REGULAR' }})</span>

              <span class="item-price">Unit: £{{ item.unitPrice.toFixed(2) }}</span>
              <strong>x{{ item.quantity || 1 }}</strong>
            </li>
          </ul>
          <p v-else class="no-items">Item details are unavailable for this order.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import orderService from '../services/orderService.js';
import PhoneNumberModal from '../components/PhoneNumberModal.vue';
import { Loader2, Package } from 'lucide-vue-next';

const orders = ref([]);
const isLoading = ref(false);

const showPhoneModal = ref(true);
const phoneNumber = ref('');
const modalError = ref('');

const expandedOrders = ref(new Set());
const orderItemsMap = ref({}); // Lazy-loaded: { [orderId]: [items] }

const toggleOrder = async (orderId) => {
  const key = String(orderId);
  if (expandedOrders.value.has(key)) {
    expandedOrders.value.delete(key);
  } else {
    expandedOrders.value.add(key);
    // Fetch items on first expand
    try {
      const items = await orderService.getOrderItems(orderId);
      orderItemsMap.value[key] = Array.isArray(items.data) ? items.data : items;
    } catch (err) {
      orderItemsMap.value[key] = [];
    }
  }
  // Re-assign Set to trigger Vue reactivity
  expandedOrders.value = new Set(expandedOrders.value);
};

const isExpanded = (orderId) => expandedOrders.value.has(String(orderId));

const statusClass = (status = '') => {
  const normalized = String(status).toUpperCase();
  if (normalized === 'COMPLETED' || normalized === 'READY') return 'done';
  if (normalized === 'IN_PROGRESS') return 'progress';
  if (normalized === 'CANCELLED') return 'cancelled';
  return 'accepted';
};

const formatStatus = (status = '') => String(status).replace('_', ' ');

const onPhoneModalSubmit = async (enteredPhoneNumber) => {
  modalError.value = '';
  phoneNumber.value = enteredPhoneNumber;
  isLoading.value = true;
  try {
    const response = await orderService.getOrdersByPhone(phoneNumber.value);
    const result = Array.isArray(response.data) ? response.data : response;
    orders.value = Array.isArray(result) ? result : [];
    showPhoneModal.value = false;
  } catch (err) {
    modalError.value = err.message || 'Failed to fetch orders.';
    orders.value = [];
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
/* Header */
.orders-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
}

.reopen-modal-btn {
  background: var(--color-primary);
  color: var(--color-bg-card);
  border: none;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 0.92em;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}
.reopen-modal-btn:hover {
  background: var(--color-primary-medium);
}

.orders-title {
  margin: 0;
  color: var(--color-text);
  font-size: 1.5rem;
  font-weight: 700;
}
.orders-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px;
}

h2 {
  margin: 0 0 16px;
  color: var(--color-text);
}

/* Order Cards */
.orders-list {
  display: grid;
  gap: 12px;
}

.order-card {
  background: #fff;
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 14px;
  display: grid;
  gap: 10px;
  cursor: pointer;
}

.order-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.order-card h3 {
  margin: 0 0 4px;
  color: var(--color-text);
}

.order-card p {
  margin: 0 0 4px;
  color: var(--color-text-muted);
}

.order-details {
  border-top: 1px solid var(--color-border-light);
  padding-top: 10px;
}

.order-meta {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.order-details h4 {
  margin: 0 0 8px;
  color: var(--color-text);
  font-size: 0.92rem;
}

.items-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: 6px;
}

.items-list li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-muted);
  gap: 16px;
}

.item-row {
  background: var(--color-bg-elevated);
  border: 1px solid var(--color-border-light);
  border-radius: 6px;
  padding: 8px 12px;
}
.item-price {
  font-weight: 600;
  color: var(--color-text);
  margin-left: auto;
}
.no-items {
  margin: 0;
  color: var(--color-text-muted);
  font-size: 0.9rem;
}

/* Status Badges */
.status {
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
}

.accepted {
  background: var(--color-status-accepted-bg);
  color: var(--color-status-accepted);
}

.progress {
  background: var(--color-status-progress-bg);
  color: var(--color-status-progress);
}

.done {
  background: var(--color-status-done-bg);
  color: var(--color-status-done);
}

.cancelled {
  background: var(--color-error-bg);
  color: var(--color-error);
}

.state-wrap {
  display: grid;
  place-items: center;
  gap: 8px;
  color: var(--color-primary-medium);
  padding: 28px 0;
}
</style>
