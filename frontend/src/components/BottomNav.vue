<template>
  <nav class="bottom-nav">
    <div class="nav-content">
      <!-- Menu Tab -->
      <button
        :class="{ active: activeTab === 'menu' }"
        title="View Menu"
        @click="router.push({ name: 'menu' })"
      >
        <Coffee size="18" class="nav-icon" />
        <span class="nav-label">Menu</span>
      </button>
      <!-- Orders Button View -->
      <button
        :class="{ active: activeTab === 'orders' }"
        title="View Orders"
        @click="router.push({ name: 'orders' })"
      >
        <List size="18" class="nav-icon" />
        <span class="nav-label">Orders</span>
      </button>
      <!-- Cart Badge View -->
      <div v-if="cartCount > 0" class="cart-badge" title="View Cart" @click="$emit('toggleCart')">
        <ShoppingCart size="16" />
        <span class="cart-label">{{ cartCount }}</span>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Coffee, List, ShoppingCart } from 'lucide-vue-next';

defineProps(['activeTab', 'cartCount']);
defineEmits(['toggleCart']);
const router = useRouter();
</script>

<style scoped>
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: var(--color-bg-card);
  border-top: 1px solid var(--color-divider);
  z-index: 900;
}

.nav-content {
  max-width: 900px;
  margin: 0 auto;
  height: 70px;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

button {
  border: none;
  background: none;
  color: var(--color-primary-light);
  cursor: pointer;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s;
}

button:hover {
  color: var(--color-primary-medium);
}

button.active {
  color: var(--color-primary);
  font-weight: 800;
  border-bottom: 3px solid #ffab40;
}

.nav-label {
  font-size: 0.9rem;
  font-weight: 600;
}

.cart-label {
  font-weight: 700;
  font-size: 0.9rem;
}

.cart-badge {
  background: var(--color-primary);
  color: var(--color-bg-card);
  padding: 8px 14px;
  border-radius: 20px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: 0.2s;
}

.cart-badge:hover {
  background: var(--color-primary-medium);
  transform: scale(1.05);
}

@media (max-width: 768px) {
  .nav-content {
    height: 64px;
    padding: 0 10px;
  }

  button {
    gap: 6px;
    padding: 8px;
  }

  .nav-label,
  .cart-label {
    font-size: 0.8rem;
  }

  .cart-badge {
    padding: 7px 10px;
    border-radius: 16px;
  }
}
</style>
