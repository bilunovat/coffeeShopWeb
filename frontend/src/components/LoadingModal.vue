<template>
  <!-- Full-screen overlay blocks interaction while loading -->
  <div v-if="show" class="loading-modal-overlay">
    <div class="loading-modal-content">
      <!-- Uses CSS .spin for rotation animation -->
      <Loader2 size="32" class="spin" />
      <p>{{ message }}</p>
    </div>
  </div>
</template>

<script setup>
import { Loader2 } from 'lucide-vue-next';

defineProps({
  show: {
    type: Boolean,
    default: false,
  },
  message: {
    type: String,
    default: 'Processing...',
  },
});
</script>

<style scoped>
.loading-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.loading-modal-content {
  background: #fff;
  padding: 32px 28px;
  border-radius: 10px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.18);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}
</style>
