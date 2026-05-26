<template>
  <!-- Backdrop closes modal on outside click -->
  <div v-if="show" class="modal-overlay" @click.self="close">
    <div class="modal-content error-modal">
      <button class="close-btn" @click="close">&times;</button>

      <!-- Icon (overridable via slot) -->
      <div class="modal-icon">
        <slot name="icon">
          <svg width="32" height="32" fill="none" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" fill="var(--color-error-bg)" />
            <path
              d="M12 8v4m0 4h.01"
              stroke="var(--color-error)"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </slot>
      </div>

      <!-- Body -->
      <h3 class="modal-title">
        <slot name="title">Error</slot>
      </h3>
      <div class="modal-message">
        <slot>{{ message }}</slot>
      </div>

      <!-- Actions -->
      <div class="modal-actions">
        <button class="action-btn" @click="close">OK</button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  show: Boolean,
  message: {
    type: String,
    default: '',
  },
});
const emit = defineEmits(['close']);
const close = () => emit('close');
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}
.modal-content {
  background: var(--color-bg-card);
  padding: 32px 24px 24px;
  border-radius: 10px;
  box-shadow: 0 2px 24px rgba(0, 0, 0, 0.18);
  min-width: 320px;
  max-width: 90vw;
  text-align: center;
  position: relative;
}
.error-modal {
  border-top: 6px solid var(--color-error);
}
.close-btn {
  position: absolute;
  top: 10px;
  right: 14px;
  background: none;
  border: none;
  font-size: 1.6em;
  color: var(--color-error);
  cursor: pointer;
}
.modal-icon {
  margin-bottom: 10px;
}
.modal-title {
  margin: 0 0 8px;
  color: var(--color-error);
  font-size: 1.25rem;
}
.modal-message {
  color: var(--color-primary-medium);
  margin-bottom: 18px;
  font-size: 1.05em;
}
.modal-actions {
  margin-top: 8px;
}
.action-btn {
  padding: 8px 24px;
  font-size: 1em;
  border: none;
  border-radius: 4px;
  background: var(--color-error);
  color: var(--color-bg-card);
  cursor: pointer;
  transition: background 0.2s;
}
.action-btn:hover {
  background: var(--color-error);
}
</style>
