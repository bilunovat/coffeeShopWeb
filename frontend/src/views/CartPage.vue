<template>
  <div class="cart-overlay" @click.self="$emit('close')">
    <div class="cart-modal">
      <button class="close-btn" aria-label="Close cart" @click="$emit('close')">
        <X size="18" />
      </button>

      <h2>Your Cart</h2>
      <!-- Empty State View -->
      <div v-if="groupedCart.length === 0" class="empty-cart">Your cart is empty.</div>
      <!-- Cart List View -->
      <ul v-else class="cart-list">
        <li v-for="item in groupedCart" :key="item.key" class="cart-item">
          <div class="cart-details">
            <strong>{{ item.name }}</strong>
            <span>{{ item.size }} x {{ item.quantity }}</span>
            <small>£{{ item.unitPrice.toFixed(2) }} each</small>
          </div>
          <!-- Cart Actions View -->
          <div class="cart-actions">
            <span class="line-total">£{{ item.lineTotal.toFixed(2) }}</span>
            <button class="remove-btn" @click="$emit('remove', item)">Remove</button>
          </div>
        </li>
      </ul>
      <!-- Cart Footer View -->
      <div class="cart-footer">
        <p class="total">Total: £{{ Number(total || 0).toFixed(2) }}</p>
        <button
          class="checkout-btn"
          :disabled="isCheckingOut || groupedCart.length === 0"
          @click="$emit('checkout')"
        >
          <Loader2 v-if="isCheckingOut" size="16" class="spin" />
          <span>{{ isCheckingOut ? 'Processing...' : 'Checkout' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { X, Loader2 } from 'lucide-vue-next';

const props = defineProps({
  cart: { type: Array, default: () => [] },
  total: { type: [String, Number], default: 0 },
  isCheckingOut: { type: Boolean, default: false },
});

defineEmits(['close', 'checkout', 'remove']);

// Groups cart items by id+size, summing quantities and line totals
const groupedCart = computed(() => {
  const grouped = new Map();

  for (const item of props.cart) {
    const quantity = item.quantity || 1;
    const key = `${item.id}-${item.size}`;

    if (!grouped.has(key)) {
      grouped.set(key, {
        key,
        id: item.id,
        name: item.name,
        size: item.size,
        unitPrice: Number(item.price || 0),
        quantity,
        lineTotal: Number(item.price || 0) * quantity,
      });
      continue;
    }

    const existing = grouped.get(key);
    existing.quantity += quantity;
    existing.lineTotal += Number(item.price || 0) * quantity;
  }

  return Array.from(grouped.values());
});
</script>

<style scoped>
/* Overlay & Modal */
.cart-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.cart-modal {
  background: var(--color-bg-card);
  padding: 22px;
  border-radius: 16px;
  width: 90%;
  max-width: 420px;
  position: relative;
  box-shadow: var(--shadow-elevated);
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 12px;
  border: none;
  background: transparent;
  font-size: 1.1rem;
  cursor: pointer;
  color: #666;
}

h2 {
  margin: 0 0 16px;
  color: var(--color-text);
}

.empty-cart {
  text-align: center;
  color: #777;
  margin: 20px 0;
}

/* Cart Items */
.cart-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 250px;
  overflow-y: auto;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--color-divider);
}

.cart-item:last-child {
  border-bottom: none;
}

.cart-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cart-details strong {
  color: var(--color-text);
}

.cart-details span {
  font-size: 0.88rem;
  color: #666;
}

.cart-details small {
  color: var(--color-accent-brown);
}

.cart-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.line-total {
  font-weight: 700;
  color: var(--color-text);
}

.remove-btn {
  border: 1px solid var(--color-error-border);
  background: var(--color-bg-closed);
  color: var(--color-error);
  border-radius: 8px;
  padding: 4px 8px;
  cursor: pointer;
}

/* Footer & Actions */
.cart-footer {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.total {
  font-size: 1.05rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-text);
}

.checkout-btn {
  width: 100%;
  padding: 11px;
  border: none;
  border-radius: 10px;
  background: var(--color-primary);
  color: var(--color-bg-card);
  font-size: 1rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.checkout-btn:hover {
  background: var(--color-primary-medium);
}

.checkout-btn:disabled {
  background: #b9a9a5;
  cursor: not-allowed;
}
</style>
