<template>
  <Transition name="modal-fade" appear>
    <div v-if="show" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-content">
        <button class="modal-close" aria-label="Close" @click="$emit('close')">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 6L6 18M6 6L18 18" stroke-linecap="round" />
          </svg>
        </button>

        <div class="modal-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path
              d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72c.127.96.362 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.338 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"
            />
          </svg>
        </div>

        <h3 class="modal-title">Enter Your Phone Number</h3>
        <p class="modal-subtitle">We'll retrieve your order details for this number</p>

        <div class="input-wrapper">
          <input
            v-model="localPhoneNumber"
            type="tel"
            placeholder="Phone number"
            maxlength="15"
            class="phone-input"
            :class="{ 'has-error': modalError }"
            autofocus
            @keyup.enter="submit"
          />
          <span v-if="localPhoneNumber && !modalError" class="input-valid-icon">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 6L9 17L4 12" stroke-linecap="round" />
            </svg>
          </span>
        </div>

        <div class="modal-actions">
          <button class="submit-btn" :disabled="isLoading || !localPhoneNumber" @click="submit">
            <span v-if="!isLoading">Submit</span>
            <div v-else class="spinner"></div>
          </button>
        </div>

        <Transition name="error-fade">
          <p v-if="modalError" class="modal-error">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            {{ modalError }}
          </p>
        </Transition>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  show: Boolean,
  isLoading: Boolean,
  error: String,
  phoneNumber: String,
});

const emit = defineEmits(['submit', 'close']);

const localPhoneNumber = ref(props.phoneNumber || '');
const modalError = ref('');

watch(
  () => props.phoneNumber,
  (val) => {
    localPhoneNumber.value = val || '';
  },
);

watch(
  () => props.error,
  (val) => {
    if (val) {
      modalError.value = val;
    }
  },
);

const submit = () => {
  modalError.value = '';

  if (!localPhoneNumber.value) {
    modalError.value = 'Please enter your phone number';
    return;
  }

  // Basic phone number validation
  const phoneRegex = /^[\d+\-\s()]{8,}$/;
  if (!phoneRegex.test(localPhoneNumber.value)) {
    modalError.value = 'Please enter a valid phone number';
    return;
  }

  emit('submit', localPhoneNumber.value);
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.25s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-content {
  position: relative;
  background: var(--color-bg-card);
  padding: 40px 32px;
  border-radius: 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  min-width: 380px;
  max-width: 90vw;
  text-align: center;
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

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  background: var(--color-cool-bg);
  border: none;
  border-radius: 12px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--color-cool-text-light);
  transition: all 0.2s ease;
  padding: 0;
}

.modal-close:hover {
  background: var(--color-cool-bg-hover);
  color: var(--color-cool-text);
  transform: scale(1.05);
}

.modal-close svg {
  width: 18px;
  height: 18px;
}

.modal-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #432c27 100%);
  border-radius: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-icon svg {
  width: 32px;
  height: 32px;
  color: white;
}

.modal-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--color-cool-text);
  letter-spacing: -0.3px;
}

.modal-subtitle {
  font-size: 14px;
  color: var(--color-cool-text-light);
  line-height: 1.5;
}

.input-wrapper {
  position: relative;
  margin-bottom: 20px;
}

.phone-input {
  width: 100%;
  padding: 14px 16px;
  font-size: 16px;
  border: 2px solid var(--color-cool-border);
  border-radius: 16px;
  background: var(--color-cool-bg);
  transition: all 0.2s ease;
  font-family: inherit;
  outline: none;
}

.phone-input:focus {
  border-color: var(--color-primary);
  background: var(--color-bg-card);
  box-shadow: 0 0 0 4px rgba(62, 39, 35, 0.1);
}

.phone-input.has-error {
  border-color: var(--color-error);
  background: var(--color-modern-error-bg);
}

.phone-input.has-error:focus {
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.1);
}

.input-valid-icon {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-success);
}

.input-valid-icon svg {
  width: 20px;
  height: 20px;
}

.modal-actions {
  margin-top: 8px;
}

.submit-btn {
  width: 100%;
  padding: 14px 24px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--color-primary) 0%, #432c27 100%);
  color: var(--color-bg-card);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px -5px rgba(62, 39, 35, 0.4);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.spinner {
  width: 20px;
  height: 20px;
  margin: 0 auto;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

.modal-error {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--color-error);
  font-size: 13px;
  margin-top: 16px;
  padding: 10px;
  background: var(--color-modern-error-bg);
  border-radius: 12px;
}

.modal-error svg {
  width: 16px;
  height: 16px;
}

.error-fade-enter-active,
.error-fade-leave-active {
  transition: all 0.2s ease;
}

.error-fade-enter-from,
.error-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}
</style>
