<template>
  <div class="checkout-page">
    <h2>Order Summary</h2>
    <!-- Empty State View -->
    <div v-if="cartStore.cart.length === 0" class="empty-state">
      <p>Your cart is empty.</p>
      <button @click="router.push({ name: 'menu' })">Back to Menu</button>
    </div>
    <!-- Checkout Card View -->
    <div v-else class="checkout-card">
      <ul>
        <li v-for="item in cartStore.cart" :key="item.id + '-' + item.size">
          <span>{{ item.name }} ({{ item.size }}) x {{ item.quantity || 1 }}</span>
          <strong>£{{ (Number(item.price || 0) * Number(item.quantity || 1)).toFixed(2) }}</strong>
        </li>
      </ul>
      <!-- Form Grid View -->
      <div class="form-grid">
        <label>
          First Name
          <input
            v-model.trim="customerFirstName"
            type="text"
            placeholder="Jane"
            :class="{ 'input-error': firstNameError }"
            @blur="validateFirstName"
            @input="firstNameError = ''"
          />
          <span v-if="firstNameError" class="field-error">{{ firstNameError }}</span>
        </label>
        <!-- Last Name Input View -->
        <label>
          Last Name
          <input
            v-model.trim="customerLastName"
            type="text"
            placeholder="Doe"
            :class="{ 'input-error': lastNameError }"
            @blur="validateLastName"
            @input="lastNameError = ''"
          />
          <span v-if="lastNameError" class="field-error">{{ lastNameError }}</span>
        </label>
        <!-- Phone Number Input View -->
        <label>
          Phone Number
          <input
            v-model.trim="customerPhoneNumber"
            type="tel"
            placeholder="07123456789"
            :class="{ 'input-error': phoneError }"
            @blur="validatePhone"
            @input="phoneError = ''"
          />
          <span v-if="phoneError" class="field-error">{{ phoneError }}</span>
        </label>
        <!-- Pickup Time Input View -->
        <label>
          Pickup Time
          <input
            v-model="pickupTime"
            type="time"
            :class="{ 'input-error': pickupTimeError }"
            @blur="validatePickupTime"
            @change="pickupTimeError = ''"
          />
          <span v-if="pickupTimeError" class="field-error">{{ pickupTimeError }}</span>
        </label>
      </div>

      <p v-if="validationError" class="error-text">{{ validationError }}</p>
      <p class="total">Total: £{{ cartTotal.toFixed(2) }}</p>

      <div class="actions">
        <button class="secondary" @click="router.push({ name: 'menu' })">Back</button>
        <button :disabled="isPaying" @click="handleConfirm">
          <span v-if="isPaying">Processing...</span>
          <span v-else>Place Order</span>
        </button>
      </div>

      <PaymentStatusModal
        v-if="showPaymentModal"
        :success="paymentSuccessStatus"
        :message="paymentModalMessage"
        @close="handleModalClose"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import orderService from '../services/orderService.js';
import { useCartStore } from '../stores/cartStore.js';
import { useStationStore } from '../stores/stationStore.js';
import PaymentStatusModal from '../components/PaymentStatusModal.vue';
import customerService from '../services/customerService.js';

const cartStore = useCartStore();
const stationStore = useStationStore();
const router = useRouter();

const customerFirstName = ref('');
const customerLastName = ref('');
const customerPhoneNumber = ref('');
const pickupTime = ref('');
const validationError = ref('');

const firstNameError = ref('');
const lastNameError = ref('');
const phoneError = ref('');
const pickupTimeError = ref('');

const NAME_REGEX = /^[A-Za-z '-]{2,50}$/;
const PHONE_REGEX = /^07\d{9}$/;

const validateFirstName = () => {
  if (!customerFirstName.value) {
    firstNameError.value = 'First name is required.';
  } else if (!NAME_REGEX.test(customerFirstName.value)) {
    firstNameError.value = 'Enter a valid first name (letters only, 2–50 characters).';
  } else {
    firstNameError.value = '';
  }
};

const validateLastName = () => {
  if (!customerLastName.value) {
    lastNameError.value = 'Last name is required.';
  } else if (!NAME_REGEX.test(customerLastName.value)) {
    lastNameError.value = 'Enter a valid last name (letters only, 2–50 characters).';
  } else {
    lastNameError.value = '';
  }
};

const validatePhone = () => {
  const digits = customerPhoneNumber.value.replace(/\s/g, '');
  if (!digits) {
    phoneError.value = 'Phone number is required.';
  } else if (!PHONE_REGEX.test(digits)) {
    phoneError.value = 'Enter a valid UK mobile number (e.g. 07123456789).';
  } else {
    phoneError.value = '';
  }
};

const validatePickupTime = () => {
  if (!pickupTime.value) {
    pickupTimeError.value = 'Please select a pickup time.';
  } else {
    pickupTimeError.value = '';
  }
};

const validateAll = () => {
  validateFirstName();
  validateLastName();
  validatePhone();
  validatePickupTime();
  return (
    !firstNameError.value && !lastNameError.value && !phoneError.value && !pickupTimeError.value
  );
};

// Maps backend field names to local error refs for displaying server-side validation
const BACKEND_FIELD_MAP = {
  customerFirstName: firstNameError,
  customerLastName: lastNameError,
  customerPhoneNumber: phoneError,
  pickupTime: pickupTimeError,
};

const applyBackendFieldErrors = (errors) => {
  let hasUnmapped = false;
  for (const [field, message] of Object.entries(errors)) {
    if (BACKEND_FIELD_MAP[field]) {
      BACKEND_FIELD_MAP[field].value = message;
    } else {
      hasUnmapped = true;
    }
  }
  if (hasUnmapped) {
    validationError.value = 'Some fields could not be submitted. Please review and try again.';
  }
};
const isPaying = ref(false);
let createdOrderId = null;

// Modal state
const showPaymentModal = ref(false);
const paymentSuccessStatus = ref(false);
const paymentModalMessage = ref('');

onMounted(() => {
  cartStore.clearError();
});

watch([customerFirstName, customerLastName, customerPhoneNumber, pickupTime], () => {
  validationError.value = '';
});

const cartTotal = computed(() => {
  return cartStore.cart.reduce(
    (sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 1),
    0,
  );
});

// Multi-step flow: create customer → create order → process payment via HorsePay
const handleConfirm = async () => {
  validationError.value = '';

  if (!validateAll()) return;

  if (!stationStore.currentStationId) {
    validationError.value =
      'Station information is unavailable. Please return to menu and try again.';
    return;
  }

  isPaying.value = true;

  try {
    // 1. Create customer
    const customerPayload = {
      customerFirstName: customerFirstName.value,
      customerLastName: customerLastName.value,
      customerPhoneNumber: customerPhoneNumber.value,
    };
    const customerRes = await customerService.createCustomer(customerPayload);
    const customerId = customerRes.customerId;
    if (!customerId) throw new Error('Customer creation failed.');

    // 2. Create order
    const orderPayload = {
      customerId,
      orderItems: cartStore.cart.map((item) => ({
        menuItemTypeId: item.menuItemTypeId,
        quantity: item.quantity || 1,
        size: (item.size || 'REGULAR').trim().toUpperCase(),
        price: item.price,
      })),
      pickupTime: pickupTime.value,
      stationId: stationStore.currentStationId,
      totalAmount: cartTotal.value,
    };

    const orderRes = await orderService.createOrder(orderPayload);
    createdOrderId = orderRes.orderId || orderRes.id;
    if (!createdOrderId) throw new Error('Order creation failed.');

    // 3. Process payment
    const now = new Date();
    const pad = (n) => n.toString().padStart(2, '0');
    const date = `${pad(now.getDate())}/${pad(now.getMonth() + 1)}/${now.getFullYear()}`;
    const time = `${pad(now.getHours())}:${pad(now.getMinutes())}`;
    const timeZone = import.meta.env.VITE_PAYMENT_TIMEZONE || 'Europe/London';

    const paymentPayload = {
      storeID: 'Team02',
      customerID: customerId,
      orderID: createdOrderId,
      date,
      time,
      timeZone,
      transactionAmount: cartTotal.value,
      currencyCode: 'GBP',
      forcePaymentStatusReturnType: true,
    };

    // Attempt payment
    let paymentMessage = '';
    let paymentSuccessful = false;

    try {
      const data = await orderService.payWithHorsePay(paymentPayload);
      if (data.paymentSuccess?.Status) {
        paymentSuccessful = true;
        paymentMessage = 'Payment successful! Your order has been placed.';
      } else {
        paymentSuccessful = true;
        paymentMessage = 'Your order has been placed! Payment confirmation is pending.';
      }
    } catch (paymentErr) {
      paymentSuccessful = true;
      paymentMessage = 'Your order has been placed! Payment confirmation is pending.';
    }

    // Show modal with payment status
    paymentSuccessStatus.value = paymentSuccessful;
    paymentModalMessage.value = paymentMessage;
    showPaymentModal.value = true;
  } catch (err) {
    // Field-level validation errors from the backend (400 with errors map)
    if (err.fieldErrors) {
      applyBackendFieldErrors(err.fieldErrors);
      return;
    }

    // Business logic / service errors (400 with message)
    if (err.status === 400) {
      validationError.value =
        err.serverMessage ||
        err.message ||
        'Some details are invalid. Please review and try again.';
      return;
    }

    // Unexpected / server errors — show modal
    paymentSuccessStatus.value = false;
    paymentModalMessage.value = err.message || 'Unable to process your order. Please try again.';
    showPaymentModal.value = true;
  } finally {
    isPaying.value = false;
  }
};

// Navigate to orders if order was created, otherwise back to menu
const handleModalClose = () => {
  showPaymentModal.value = false;

  if (createdOrderId) {
    cartStore.clearCart();
    router.replace({ name: 'orders' });
  } else {
    router.replace({ name: 'menu' });
  }

  customerFirstName.value = '';
  customerLastName.value = '';
  customerPhoneNumber.value = '';
  pickupTime.value = '';
  createdOrderId = null;
};
</script>

<style scoped>
.checkout-page {
  max-width: 760px;
  margin: 0 auto;
  padding: 24px 16px;
}

h2 {
  color: var(--color-text);
  margin-bottom: 14px;
}

.checkout-card {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 16px;
  background: var(--color-bg-card);
}

ul {
  margin: 0 0 14px;
  padding: 0;
  list-style: none;
}

li {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid var(--color-border-light);
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 12px;
}

label {
  display: grid;
  gap: 6px;
  color: var(--color-primary-medium);
}

input {
  padding: 8px;
  border-radius: 8px;
  border: 1px solid var(--color-border-medium);
}

input.input-error {
  border-color: var(--color-error);
  outline-color: #b03c3c;
}

.field-error {
  color: var(--color-error);
  font-size: 0.82rem;
  margin-top: 2px;
}

.error-text {
  margin-top: 12px;
  color: var(--color-error);
  font-size: 0.92rem;
}

.total {
  font-weight: 700;
  color: var(--color-text);
  margin-top: 14px;
}

/* Buttons & Actions */
.actions {
  margin-top: 14px;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

button {
  border: none;
  border-radius: 8px;
  background: var(--color-primary);
  color: var(--color-bg-card);
  padding: 10px 12px;
  cursor: pointer;
}

button.secondary {
  background: var(--color-primary-light);
}

button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.empty-state {
  border: 1px dashed var(--color-border-medium);
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

@media (max-width: 640px) {
/* Form & Inputs */
.form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
