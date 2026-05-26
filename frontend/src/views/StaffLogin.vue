<template>
  <div class="staff-login-page">
    <div class="login-shell">
      <div class="login-panel">
    <!-- Brand Header -->
    <div class="brand-block">
          <div class="brand-mark"><User size="24" /></div>
          <div>
            <h1>Staff Login</h1>
            <p>Sign in to manage orders.</p>
          </div>
        </div>

    <!-- Login Form -->
    <form class="login-form" @submit.prevent="submitLogin">
          <div class="field">
            <label>Email or Staff ID</label>
            <div class="input-wrap">
              <User size="18" />
              <input v-model="username" type="text" placeholder="staff@whistlestop.co" />
            </div>
          </div>

          <div class="field">
            <label>Password</label>
            <div class="input-wrap">
              <Lock size="18" />
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="password"
              />
              <button
                type="button"
                class="visibility-btn"
                :title="showPassword ? 'Hide password' : 'Show password'"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                @click="togglePasswordVisibility"
              >
                <Eye v-if="showPassword" size="18" />
                <EyeOff v-else size="18" />
              </button>
            </div>
          </div>

          <p v-if="displayError" class="error-message">{{ displayError }}</p>

          <button class="sign-in-btn" type="submit" :disabled="isSubmitting">
            <Loader2 v-if="isSubmitting" size="18" class="spin" />
            <span>{{ isSubmitting ? 'Signing In...' : 'Sign In' }}</span>
            <ArrowRight v-if="!isSubmitting" size="18" />
          </button>
        </form>

        <button class="cancel-btn" @click="cancelLogin">Back to Store</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '../stores/userStore.js';
import { User, Lock, EyeOff, Eye, Loader2, ArrowRight } from 'lucide-vue-next';

const router = useRouter();
const userStore = useUserStore();

const username = ref('');
const password = ref('');
const showPassword = ref(false);
const errorMessage = ref('');
// Merges store-level and local error messages
const displayError = computed(() => userStore.userError || errorMessage.value);
const isSubmitting = computed(() => userStore.isLoading);

const togglePasswordVisibility = () => {
  showPassword.value = !showPassword.value;
};

const submitLogin = async () => {
  if (isSubmitting.value) return;

  if (!username.value || !password.value) {
    errorMessage.value = 'Please enter your email or ID and password.';
    return;
  }

  errorMessage.value = '';
  try {
    await userStore.staffLogin({
      username: username.value,
      password: password.value,
      role: 'Staff',
      // Derive display name from email local part or use raw username
      name: username.value.includes('@') ? username.value.split('@')[0] : username.value,
    });
    router.push({ name: 'staffDashboard' });
  } catch {
    // Error is available via userStore.userError, displayed through displayError
  }
};

const cancelLogin = () => {
  userStore.clearError();
  router.push({ name: 'home' });
};
</script>

<style scoped>
.staff-login-page {
  display: flex;
  min-height: 100vh;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-cream);
  padding: 40px 20px;
}

.login-shell {
  width: 100%;
  max-width: 520px;
}

.login-panel {
  background: var(--color-bg-card);
  border-radius: 28px;
  box-shadow: 0 24px 80px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

/* Brand Block */
.brand-block {
  padding: 32px 32px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.brand-mark {
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: var(--color-accent);
  display: grid;
  place-items: center;
  color: #fff;
}

.brand-block h1 {
  margin: 0 0 6px;
  font-size: 28px;
  letter-spacing: -0.03em;
}

.brand-block p {
  margin: 0;
  color: #6f5b52;
  line-height: 1.6;
}

.login-form {
  padding: 32px;
  display: grid;
  gap: 18px;
}

.field label {
  display: block;
  margin-bottom: 10px;
  font-size: 0.9rem;
  color: var(--color-text-muted);
}

.input-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  padding: 14px 16px;
  background: var(--color-bg-cream-light);
}

.input-wrap input {
  border: none;
  outline: none;
  width: 100%;
  font-size: 1rem;
  background: transparent;
  color: var(--color-text);
}

.visibility-btn {
  border: none;
  background: transparent;
  color: var(--color-text-muted);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  padding: 0;
}

.visibility-btn:hover {
  color: var(--color-text);
}

.error-message {
  margin: 0;
  color: var(--color-error);
  font-size: 0.9rem;
}

/* Buttons */
.sign-in-btn,
.cancel-btn {
  width: 100%;
  border: none;
  border-radius: 14px;
  font-size: 1rem;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  cursor: pointer;
}

.sign-in-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.sign-in-btn {
  margin-top: 14px;
  padding: 16px 20px;
  background: var(--color-primary);
  color: var(--color-bg-card);
}

.sign-in-btn:hover {
  background: var(--color-accent);
}

.cancel-btn {
  padding: 14px 20px;
  margin: 0 32px 32px;
  width: calc(100% - 64px);
  background: transparent;
  color: var(--color-text);
  border: 1px solid var(--color-border-medium);
}

.cancel-btn:hover {
  background: var(--color-bg-cream-light);
}

@media (max-width: 480px) {
  .staff-login-page {
    padding: 20px 12px;
  }

  .login-panel {
    border-radius: 20px;
  }

  .brand-block,
/* Form Fields */
.login-form {
    padding: 20px 16px;
  }

  .cancel-btn {
    margin: 0 16px 20px;
    width: calc(100% - 32px);
  }

  .brand-block h1 {
    font-size: 22px;
  }

  .input-wrap {
    padding: 12px 12px;
  }
}
</style>
