<template>
  <!-- Fade transition on mount (appear) and unmount -->
  <Transition name="modal-fade" appear>
    <!-- Backdrop click dismisses -->
    <div v-if="message" class="payment-status-modal" @click.self="$emit('close')">
      <!-- success prop toggles icon, colors, and button styling -->
      <div class="modal-content" :class="{ success, error: !success }">

        <!-- Status Icon -->
        <div class="modal-icon">
          <svg
            v-if="success"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M20 6L9 17L4 12" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="12" y1="8" x2="12" y2="12" />
            <line x1="12" y1="16" x2="12.01" y2="16" />
          </svg>
        </div>

        <!-- Message & Dismiss -->
        <p class="modal-message">{{ message }}</p>
        <button class="modal-button" @click="$emit('close')">
          <span>Got it</span>
        </button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  success: Boolean,
  message: String,
});
const emit = defineEmits(['close']);
</script>

<style scoped>
.payment-status-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.2s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-content {
  background: var(--color-bg-card);
  border-radius: 24px;
  padding: 32px 28px;
  min-width: 280px;
  max-width: 90vw;
  text-align: center;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  animation: modal-slide-up 0.3s cubic-bezier(0.34, 1.2, 0.64, 1);
}

@keyframes modal-slide-up {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.modal-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.modal-content.success .modal-icon {
  background: linear-gradient(
    135deg,
    var(--color-status-done-bg) 0%,
    var(--color-status-done-light) 100%
  );
  color: var(--color-status-done);
}

.modal-content.error .modal-icon {
  background: linear-gradient(135deg, var(--color-error-bg) 0%, #ffcdd2 100%);
  color: var(--color-status-closed);
}

.modal-icon svg {
  width: 32px;
  height: 32px;
}

.modal-message {
  font-size: 16px;
  font-weight: 500;
  line-height: 1.5;
  margin: 0 0 24px;
  color: var(--color-cool-text);
}

.modal-content.success .modal-message {
  color: var(--color-success-dark);
}

.modal-content.error .modal-message {
  color: var(--color-status-closed);
}

.modal-button {
  margin-top: 0;
  padding: 10px 20px;
  border-radius: 40px;
  border: none;
  background: var(--color-cool-text);
  color: var(--color-bg-card);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
  letter-spacing: 0.3px;
}

.modal-button:hover {
  background: var(--color-cool-text);
  transform: scale(1.02);
}

.modal-button:active {
  transform: scale(0.98);
}

.modal-content.success .modal-button {
  background: var(--color-success-accent);
}

.modal-content.success .modal-button:hover {
  background: var(--color-success-dark);
}

.modal-content.error .modal-button {
  background: var(--color-status-closed);
}

.modal-content.error .modal-button:hover {
  background: var(--color-error);
}
</style>
