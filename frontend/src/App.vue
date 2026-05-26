<template>
  <div class="app-container" :class="{ 'has-bottom-nav': showNav }">
    <HeaderBar v-if="showNav" />

    <RouterView />

    <CartPage
      v-if="cartStore.showCart"
      :cart="cartStore.cart"
      :total="cartStore.formattedTotal"
      :is-checking-out="cartStore.isLoading"
      @close="cartStore.showCart = false"
      @checkout="handleCheckout"
      @remove="handleRemoveFromCart"
    />

    <BottomNav
      v-if="showNav"
      :active-tab="currentRouteName"
      :cart-count="cartStore.cartCount"
      @toggle-cart="cartStore.showCart = true"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import HeaderBar from './components/HeaderBar.vue';
import BottomNav from './components/BottomNav.vue';
import CartPage from './views/CartPage.vue';
import { useCartStore } from './stores/cartStore.js';
import { useStationStore } from './stores/stationStore.js';
import { useUserStore } from './stores/userStore.js';

const route = useRoute();
const router = useRouter();
const cartStore = useCartStore();
const stationStore = useStationStore();
const userStore = useUserStore();

// Routes that display the header and bottom navigation bar
const NAV_ROUTES = new Set(['menu', 'orders']);
const showNav = computed(() => NAV_ROUTES.has(route.name));
const currentRouteName = computed(() => route.name);

const handleCheckout = () => {
  if (cartStore.cart.length === 0) return;
  cartStore.showCart = false;
  router.push({ name: 'checkout' });
};

// Decrements quantity by 1; store removes item when quantity hits 0
const handleRemoveFromCart = (group) => {
  const index = cartStore.cart.findIndex(
    (item) => item.id === group.id && item.size === group.size,
  );
  if (index === -1) return;
  const item = cartStore.cart[index];
  const nextQty = (item.quantity || 1) - 1;
  cartStore.updateCartItemQuantity(index, nextQty);
};

// Bootstrap: load station data, then redirect staff to their dashboard
onMounted(async () => {
  await stationStore.loadStations().catch(() => null);

  if (userStore.isStaffLoggedIn) {
    router.replace({ name: 'staffDashboard' });
  }
});
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: transparent;
}

.app-container.has-bottom-nav {
  padding-bottom: 88px;
}

@media (max-width: 768px) {
  .app-container.has-bottom-nav {
    padding-bottom: 84px;
  }
}
</style>
