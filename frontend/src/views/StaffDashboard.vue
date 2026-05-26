<template>
  <section class="staff-dashboard">
    <header class="dashboard-header">
      <div class="header-text">
        <p class="eyebrow">Staff Console</p>
        <h2>Order Dashboard</h2>
        <p class="subtitle">
          {{
            staffUser?.name ? `Signed in as ${staffUser.name}` : 'Track and process live orders.'
          }}
        </p>
      </div>
      <button class="logout-btn" @click="handleLogout">
        <LogOut size="16" />
        <span class="btn-text">Logout</span>
      </button>
    </header>
    <!-- Toolbar View -->
    <div class="toolbar">
      <div class="filter-group">
        <button
          v-for="status in statusFilters"
          :key="status"
          :class="['filter-btn', { active: activeFilter === status }]"
          @click="
            activeFilter = status;
            loadOrders(status);
          "
        >
          {{ prettyStatus(status) }}
        </button>
      </div>
      <div class="action-group">
        <button
          class="filter-btn"
          :class="{ active: isLoading }"
          :disabled="isLoading"
          @click="loadOrders(activeFilter)"
        >
          {{ isLoading ? 'REFRESHING' : 'REFRESH' }}
        </button>
        <button class="filter-btn" @click="showArchived = !showArchived">
          {{ showArchived ? 'HIDE ARCHIVED' : 'SHOW ARCHIVED' }}
        </button>
        <button class="filter-btn" @click="router.push({ name: 'openingHours' })">
          OPENING HOURS
        </button>
      </div>
    </div>
    <!-- Error Banner View -->
    <div v-if="showErrorModal" class="error-banner">
      <ErrorModal :show="showErrorModal" :message="errorMessage" @close="showErrorModal = false" />
    </div>
    <!-- Loading Modal View -->
    <div v-if="showLoadingModal">
      <LoadingModal :show="showLoadingModal" :message="loadingMessage" />
    </div>
    <!-- State Wrap View -->
    <div v-if="isLoading && !filteredOrders.length" class="state-wrap">
      <Loader2 size="24" class="spin" />
      <p>Loading orders...</p>
    </div>
    <!-- Empty State View -->
    <div v-else-if="!filteredOrders.length" class="state-wrap empty">
      <Package size="24" />
      <p>No {{ prettyStatus(activeFilter).toLowerCase() }} orders at the moment.</p>
    </div>
    <!-- Orders Grid View -->
    <div v-else class="orders-grid">
      <article
        v-for="order in filteredOrders"
        :key="order.id"
        class="order-card"
        role="button"
        tabindex="0"
        @click="toggleOrder(order.id)"
        @keydown.enter.prevent="toggleOrder(order.id)"
      >
        <header class="card-top">
          <h3>Order #{{ order.id }}</h3>
          <span :class="['status-chip', chipClass(order.status)]">{{
            prettyStatus(order.status)
          }}</span>
        </header>

        <div class="order-info">
          <p class="summary">
            <span class="label">Total: </span>
            {{ order.totalAmount ? `£${order.totalAmount.toFixed(2)}` : 'Order in progress' }}
          </p>
          <p class="meta">Pickup: {{ order.pickupTime || order.arrivalTime || '--:--' }}</p>
        </div>
        <div class="actions">
          <template v-if="order.status !== 'COLLECTED' && order.status !== 'CANCELLED'">
            <button
              v-if="order.status === 'ACCEPTED'"
              class="action-btn in-progress"
              @click.stop="changeStatus(order.id, staffUser.id, 'IN_PROGRESS')"
            >
              Start
            </button>

            <button
              v-if="order.status === 'IN_PROGRESS'"
              class="action-btn ready"
              @click.stop="changeStatus(order.id, staffUser.id, 'COMPLETED')"
            >
              Complete
            </button>

            <button
              v-if="order.status === 'COMPLETED'"
              class="action-btn ready"
              @click.stop="changeStatus(order.id, staffUser.id, 'COLLECTED')"
            >
              Collected
            </button>

            <button
              v-if="order.status !== 'CANCELLED' && order.status !== 'COLLECTED'"
              class="action-btn cancel"
              @click.stop="changeStatus(order.id, staffUser.id, 'CANCELLED')"
            >
              Cancel
            </button>
          </template>

          <button
            v-if="!order.isArchived || (order.isArchived && showArchived)"
            class="action-btn archive"
            @click.stop="archiveOrder(order.id, !order.isArchived)"
          >
            <span>{{ order.isArchived ? 'Unarchive' : 'Archive' }}</span>
          </button>
        </div>

        <div v-if="isExpanded(order.id)" class="order-details">
          <h4>Items</h4>
          <ul v-if="orderItemsMap[order.id]?.length" class="items-list">
            <li
              v-for="(item, index) in orderItemsMap[order.id]"
              :key="`${order.id}-${index}`"
              class="item"
            >
              <div class="item-info">
                <span class="item-name"
                  >{{ item.itemName || 'Item' }} ({{ item.size || 'REGULAR' }})</span
                >
                <div v-if="item.description" class="item-desc">{{ item.description }}</div>
              </div>
              <div class="item-pricing">
                <span>£{{ item.unitPrice.toFixed(2) }}</span>
                <strong>×{{ item.quantity || 1 }}</strong>
              </div>
            </li>
          </ul>
          <p v-else class="no-items">Item details are unavailable for this order.</p>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/userStore.js';
import orderService from '../services/orderService.js';
import ErrorModal from '../components/ErrorModal.vue';
import LoadingModal from '../components/LoadingModal.vue';
import { LogOut, Loader2, Package } from 'lucide-vue-next';

const router = useRouter();
const userStore = useUserStore();

const staffUser = computed(() => userStore.staffUser);

const handleLogout = () => {
  userStore.staffLogout();
  router.push({ name: 'home' });
};

const statusFilters = ['ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'COLLECTED', 'CANCELLED'];
const activeFilter = ref('ACCEPTED');
const showArchived = ref(false);

const orders = ref([]);
const isLoading = ref(false);
const showErrorModal = ref(false);
const errorMessage = ref('');
const showLoadingModal = ref(false);
const loadingMessage = ref('Processing...');

// When showArchived is on, show all archived orders; otherwise filter by status and exclude archived
const filteredOrders = computed(() => {
  if (showArchived.value) {
    return orders.value.filter((order) => order.isArchived === true);
  }
  return orders.value.filter((order) => order.status === activeFilter.value && !order.isArchived);
});

const expandedOrders = ref(new Set());
const orderItemsMap = ref({});

const toggleOrder = async (orderId) => {
  const key = String(orderId);
  if (expandedOrders.value.has(key)) {
    expandedOrders.value.delete(key);
  } else {
    expandedOrders.value.add(key);
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

const loadOrders = async (status) => {
  isLoading.value = true;
  try {
    orders.value = await orderService.getOrdersByStatus(status);
  } catch (err) {
  } finally {
    isLoading.value = false;
  }
};

const changeStatus = async (orderId, staffId, newStatus) => {
  showLoadingModal.value = true;
  loadingMessage.value = 'Updating order status...';
  try {
    await orderService.updateOrderStatus(orderId, staffId, newStatus);
    const order = orders.value.find((o) => o.id === orderId);
    if (order) {
      order.status = newStatus;
    }
  } catch (err) {
    showErrorModal.value = true;
    errorMessage.value = err.message || 'Failed to update order status.';
  } finally {
    showLoadingModal.value = false;
  }
};

const archiveOrder = async (orderId, value) => {
  showLoadingModal.value = true;
  loadingMessage.value = value ? 'Archiving order...' : 'Unarchiving order...';
  try {
    await orderService.archiveOrder(orderId, value);
    const order = orders.value.find((o) => o.id === orderId);
    if (order) {
      order.isArchived = value;
    }
  } catch (err) {
    showErrorModal.value = true;
    errorMessage.value = err.message || 'Failed to archive order.';
  } finally {
    showLoadingModal.value = false;
  }
};

const isExpanded = (orderId) => expandedOrders.value.has(String(orderId));
const prettyStatus = (status = '') => String(status).replace('_', ' ');
const chipClass = (status = '') => {
  if (status === 'COMPLETED') return 'done';
  if (status === 'IN_PROGRESS') return 'progress';
  if (status === 'CANCELLED') return 'cancelled';
  if (status === 'COLLECTED') return 'collected';
  return 'accepted';
};

onMounted(() => loadOrders(activeFilter.value));
</script>

<style scoped>
.staff-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: clamp(16px, 3vw, 32px) clamp(12px, 2vw, 24px);
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: clamp(12px, 3vw, 24px);
  margin-bottom: clamp(12px, 2vw, 20px);
  flex-wrap: wrap;
}

.header-text {
  flex: 1;
  min-width: 0;
}

.eyebrow {
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: clamp(0.65rem, 1.5vw, 0.75rem);
  color: var(--color-text-muted);
}

h2 {
  margin: clamp(2px, 0.5vw, 6px) 0;
  color: var(--color-text);
  font-size: clamp(1.2rem, 2.5vw, 1.5rem);
  font-family: 'Bebas Neue', sans-serif;
  letter-spacing: 2px;
}

.subtitle {
  margin: 0;
  color: var(--color-text-muted);
  font-size: clamp(0.8rem, 1.5vw, 0.95rem);
}

.logout-btn {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: var(--color-text-subtle);
  color: var(--color-bg-card);
  border-radius: 999px;
  padding: clamp(8px, 1.5vw, 10px) clamp(10px, 2vw, 14px);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  font-weight: 600;
  font-size: clamp(0.8rem, 1.5vw, 0.9rem);
  transition: all 0.2s ease;
}

.logout-btn:hover {
  background: rgba(34, 23, 18, 0.7);
}

.toolbar {
  display: flex;
  gap: clamp(8px, 2vw, 16px);
  flex-wrap: wrap;
  margin-bottom: clamp(12px, 2vw, 20px);
  justify-content: space-between;
  align-items: flex-start;
}

.filter-group {
  display: flex;
  gap: clamp(4px, 1vw, 8px);
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;
}

.action-group {
  display: flex;
  gap: clamp(4px, 1vw, 8px);
  flex-wrap: wrap;
}

.filter-btn,
.order-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: clamp(10px, 2vw, 16px);
  transition: all 0.2s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: clamp(6px, 1vw, 10px);
}

.order-card:hover {
  box-shadow: 0 2px 8px rgba(62, 39, 35, 0.1);
  border-color: var(--color-border);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: clamp(6px, 1vw, 12px);
}

.card-top h3 {
  margin: 0;
  font-size: clamp(0.9rem, 1.6vw, 1.1rem);
  color: var(--color-text);
  min-width: 0;
}

.status-chip {
  border-radius: 999px;
  font-size: clamp(0.65rem, 1.2vw, 0.75rem);
  font-weight: 700;
  padding: clamp(3px, 0.5vw, 5px) clamp(6px, 1vw, 10px);
  white-space: nowrap;
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

.collected {
  background: var(--color-status-collected-bg);
  color: var(--color-status-collected);
}

.cancelled {
  background: var(--color-error-bg);
  color: var(--color-error);
}

.order-info {
  display: flex;
  flex-direction: column;
  gap: clamp(2px, 0.5vw, 4px);
}

.summary,
.meta {
  margin: 0;
  color: var(--color-text-muted);
  font-size: clamp(0.8rem, 1.4vw, 0.95rem);
}

.label {
  font-weight: 600;
}

.actions {
  display: flex;
  gap: clamp(4px, 0.8vw, 8px);
  flex-wrap: wrap;
  margin-top: auto;
}

.action-btn {
  border: none;
  border-radius: 8px;
  padding: clamp(10px, 1.5vw, 14px) clamp(8px, 1.5vw, 14px);
  color: #fff;
  cursor: pointer;
  font-size: clamp(0.7rem, 1.2vw, 0.85rem);
  font-family: 'Bebas Neue', sans-serif;
  letter-spacing: 2px;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s ease;
  flex: 1;
  min-width: 0;
}

.action-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.in-progress {
  background: var(--color-status-progress);
}

.ready {
  background: var(--color-status-done);
}

.cancel {
  background: var(--color-error);
}

.archive {
  background: var(--color-text-muted);
}

.order-details {
  border-top: 1px solid var(--color-border-light);
  padding-top: clamp(8px, 1.5vw, 12px);
  margin-top: clamp(4px, 1vw, 8px);
}

.order-details h4 {
  margin: 0 0 clamp(6px, 1vw, 10px) 0;
  font-size: clamp(0.85rem, 1.5vw, 1rem);
  color: var(--color-text);
}

.items-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  gap: clamp(6px, 1vw, 10px);
}

.orders-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: clamp(10px, 2vw, 16px);
  max-width: 860px;
  margin: 0 auto;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: clamp(6px, 1vw, 12px);
  padding: clamp(6px, 1vw, 8px);
  background: var(--color-bg-elevated);
  border-radius: 6px;
  flex-wrap: wrap;
}

.item-info {
  flex: 1;
  min-width: 0;
}

.item-name {
  font-weight: 500;
  color: var(--color-text);
}

.item-desc {
  font-size: 0.8em;
  color: var(--color-text-muted);
  margin-top: 2px;
}

.item-pricing {
  display: flex;
  align-items: center;
  gap: clamp(4px, 0.8vw, 8px);
  white-space: nowrap;
}

.no-items {
  color: var(--color-text-muted);
  font-style: italic;
}

.state-wrap {
  display: grid;
  place-items: center;
  gap: clamp(6px, 1vw, 12px);
  color: var(--color-text-muted);
  padding: clamp(20px, 4vw, 40px) 0;
  font-size: clamp(0.85rem, 1.5vw, 1rem);
}

/* Mobile */
@media (max-width: 640px) {
  .orders-grid {
    grid-template-columns: 1fr;
  }

  .toolbar {
    flex-direction: column;
  }

  .filter-group,
  .action-group {
    width: 100%;
  }

  .filter-btn {
    flex: 1;
    justify-content: center;
  }

  .btn-text {
    display: none;
  }

  .logout-btn .btn-text {
    display: none;
  }

  .card-top {
    flex-direction: column;
    align-items: flex-start;
  }

  .status-chip {
    align-self: flex-start;
  }

  .item {
    flex-direction: column;
  }

  .item-pricing {
    align-self: flex-end;
  }
}

/* Tablet adjustments */
@media (min-width: 641px) and (max-width: 1024px) {
  .orders-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* Large screens */
@media (min-width: 1025px) {
  .orders-grid {
    grid-template-columns: repeat(2, 1fr);
    max-width: 860px;
  }
}
</style>
