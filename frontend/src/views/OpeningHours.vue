<template>
  <div class="opening-hours-page">
    <div class="page-header">
      <button class="btn-back" @click="router.push({ name: 'staffDashboard' })">
        <ChevronLeft size="16" />
        <span>Back to Dashboard</span>
      </button>
      <h1>Opening Hours</h1>
      <p class="page-subtitle">Cramlington Station</p>
    </div>

    <!-- Error state -->
    <div v-if="stationError" class="state-message error">
      <p>{{ stationError }}</p>
    </div>

    <!-- Loading state -->
    <div v-else-if="!station" class="state-message">
      <p>Loading station information...</p>
    </div>

    <template v-else>
      <!-- Display Mode -->
      <div v-if="!editing" class="hours-card">
        <div class="hours-header">
          <h2>{{ station.name }}</h2>
          <button class="btn-edit" @click="startEdit">Edit Hours</button>
        </div>

        <table class="hours-table">
          <thead>
            <tr>
              <th>Day</th>
              <th>Opening Hours</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Monday – Friday</td>
              <td>{{ formatHours(station.weekdayOpeningHours) }}</td>
            </tr>
            <tr>
              <td>Saturday</td>
              <td>{{ formatHours(station.saturdayOpeningHours) }}</td>
            </tr>
            <tr :class="{ closed: station.closedOnSunday }">
              <td>Sunday</td>
              <td>
                {{ station.closedOnSunday ? 'Closed' : formatHours(station.saturdayOpeningHours) }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Edit Mode -->
      <div v-else class="hours-card edit-mode">
        <div class="hours-header">
          <h2>Edit Hours — {{ station.name }}</h2>
        </div>

        <form class="edit-form" novalidate @submit.prevent="saveHours">
          <div class="field-group">
            <label for="weekday-hours"
              >Monday – Friday <span class="format-hint">(HH:mm-HH:mm)</span></label
            >
            <input
              id="weekday-hours"
              v-model="form.weekdayOpeningHours"
              type="text"
              placeholder="e.g. 06:30-19:00"
              :class="{ invalid: fieldErrors.weekdayOpeningHours }"
            />
            <span v-if="fieldErrors.weekdayOpeningHours" class="field-error">{{
              fieldErrors.weekdayOpeningHours
            }}</span>
          </div>

          <div class="field-group">
            <label for="saturday-hours"
              >Saturday <span class="format-hint">(HH:mm-HH:mm)</span></label
            >
            <input
              id="saturday-hours"
              v-model="form.saturdayOpeningHours"
              type="text"
              placeholder="e.g. 07:00-17:00"
              :class="{ invalid: fieldErrors.saturdayOpeningHours }"
            />
            <span v-if="fieldErrors.saturdayOpeningHours" class="field-error">{{
              fieldErrors.saturdayOpeningHours
            }}</span>
          </div>

          <div class="field-group checkbox-group">
            <label class="checkbox-label">
              <input id="closed-sunday" v-model="form.closedOnSunday" type="checkbox" />
              <span>Closed on Sunday</span>
            </label>
          </div>

          <div v-if="saveError" class="state-message error form-error">{{ saveError }}</div>
          <div v-if="saveSuccess" class="state-message success">Hours updated successfully.</div>

          <div class="form-actions">
            <button type="button" class="btn-cancel" :disabled="isSaving" @click="cancelEdit">
              Cancel
            </button>
            <button type="submit" class="btn-save" :disabled="isSaving">
              {{ isSaving ? 'Saving...' : 'Save Changes' }}
            </button>
          </div>
        </form>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useStationStore } from '../stores/stationStore.js';
import stationService from '../services/stationService.js';
import { ChevronLeft } from 'lucide-vue-next';

const router = useRouter();
const stationStore = useStationStore();
const station = computed(() => stationStore.currentStation);
// Validates 24-hour time range format (e.g. "06:30-19:00")
const HOURS_PATTERN = /^([01]\d|2[0-3]):[0-5]\d-([01]\d|2[0-3]):[0-5]\d$/;
const stationError = ref('');
const editing = ref(false);
const isSaving = ref(false);
const saveError = ref('');
const saveSuccess = ref(false);

const form = reactive({
  weekdayOpeningHours: '',
  saturdayOpeningHours: '',
  closedOnSunday: false,
});

const fieldErrors = reactive({
  weekdayOpeningHours: '',
  saturdayOpeningHours: '',
});

onMounted(() => {
  if (!stationStore.currentStation) {
    stationStore.loadStations();
  }
});

function formatHours(raw) {
  if (!raw || !raw.includes('-')) return raw || 'N/A';
  const [open, close] = raw.split('-');
  return `${open} – ${close}`;
}

function startEdit() {
  form.weekdayOpeningHours = station.value.weekdayOpeningHours;
  form.saturdayOpeningHours = station.value.saturdayOpeningHours;
  form.closedOnSunday = station.value.closedOnSunday;
  fieldErrors.weekdayOpeningHours = '';
  fieldErrors.saturdayOpeningHours = '';
  saveError.value = '';
  saveSuccess.value = false;
  editing.value = true;
}

function cancelEdit() {
  editing.value = false;
  saveError.value = '';
  saveSuccess.value = false;
}

function validateForm() {
  let valid = true;
  fieldErrors.weekdayOpeningHours = '';
  fieldErrors.saturdayOpeningHours = '';

  if (!HOURS_PATTERN.test(form.weekdayOpeningHours)) {
    fieldErrors.weekdayOpeningHours = 'Must be in HH:mm-HH:mm 24-hour format (e.g. 06:30-19:00).';
    valid = false;
  } else {
    const [open, close] = form.weekdayOpeningHours.split('-');
    if (open >= close) {
      fieldErrors.weekdayOpeningHours = 'Opening time must be before closing time.';
      valid = false;
    }
  }

  if (!HOURS_PATTERN.test(form.saturdayOpeningHours)) {
    fieldErrors.saturdayOpeningHours = 'Must be in HH:mm-HH:mm 24-hour format (e.g. 07:00-17:00).';
    valid = false;
  } else {
    const [open, close] = form.saturdayOpeningHours.split('-');
    if (open >= close) {
      fieldErrors.saturdayOpeningHours = 'Opening time must be before closing time.';
      valid = false;
    }
  }

  return valid;
}

async function saveHours() {
  saveError.value = '';
  saveSuccess.value = false;
  if (!validateForm()) return;

  isSaving.value = true;
  try {
    const updated = await stationService.updateStationHours(stationStore.currentStationId, {
      weekdayOpeningHours: form.weekdayOpeningHours,
      saturdayOpeningHours: form.saturdayOpeningHours,
      closedOnSunday: form.closedOnSunday,
    });
    await stationStore.loadStations();
    saveSuccess.value = true;
    editing.value = false;
  } catch (error) {
    saveError.value = error.message || 'Failed to save. Please try again.';
  } finally {
    isSaving.value = false;
  }
}
</script>

<style scoped>
.opening-hours-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 2rem 1rem;
  font-family: inherit;
}

.page-header {
  margin-bottom: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.page-header h1 {
  font-size: 1.75rem;
  font-weight: 700;
  margin: 0 0 0.25rem;
  color: var(--color-text);
}

.page-subtitle {
  color: var(--color-text-muted);
  margin: 0;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 0.825rem;
  cursor: pointer;
  padding: 0;
  margin-bottom: 0.25rem;
  transition: color 0.15s;
}

.btn-back:hover {
  color: var(--color-primary-medium);
}

.hours-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: 0.75rem;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(62, 39, 35, 0.06);
}

.hours-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
}

.hours-header h2 {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  color: var(--color-text);
}

.hours-table {
  width: 100%;
  border-collapse: collapse;
}

.hours-table th,
.hours-table td {
  padding: 0.65rem 0.5rem;
  text-align: left;
  border-bottom: 1px solid var(--color-border-light);
}

.hours-table th {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-subtle);
  font-weight: 600;
}

.hours-table td:first-child {
  font-weight: 500;
  color: var(--color-primary-medium);
}

.hours-table td:last-child {
  color: var(--color-text);
}

.hours-table tr.closed td {
  color: var(--color-text-subtle);
}

/* Edit form */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.field-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.field-group label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-primary-medium);
}

.format-hint {
  font-weight: 400;
  color: var(--color-text-subtle);
  font-size: 0.8rem;
}

.field-group input[type='text'] {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 0.375rem;
  font-size: 0.9375rem;
  outline: none;
  transition: border-color 0.15s;
  color: var(--color-text);
}

.field-group input[type='text']:focus {
  border-color: var(--color-accent-warm);
  box-shadow: 0 0 0 2px rgba(141, 85, 36, 0.12);
}

.field-group input.invalid {
  border-color: var(--color-error);
}

.field-error {
  font-size: 0.8125rem;
  color: var(--color-error);
}

.checkbox-group {
  flex-direction: row;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  font-size: 0.9375rem;
  font-weight: 400;
  color: var(--color-primary-medium);
}

.checkbox-label input[type='checkbox'] {
  width: 1rem;
  height: 1rem;
  accent-color: #8d5524;
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding-top: 0.25rem;
}

/* Buttons */
.btn-edit,
.btn-save,
.btn-cancel {
  padding: 0.45rem 1rem;
  border-radius: 999px;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition:
    background 0.15s,
    opacity 0.15s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-edit {
  background: var(--color-primary-medium);
  color: var(--color-bg-card);
}

.btn-edit:hover {
  background: var(--color-accent-dark);
}

.btn-save {
  background: var(--color-primary-medium);
  color: var(--color-bg-card);
}

.btn-save:hover:not(:disabled) {
  background: var(--color-accent-dark);
}

.btn-cancel {
  background: var(--color-bg-card);
  color: var(--color-primary-medium);
  border: 1px solid var(--color-border);
}

.btn-cancel:hover:not(:disabled) {
  background: var(--color-bg-cream-light);
}

.btn-save:disabled,
.btn-cancel:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

/* State messages */
.state-message {
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.9rem;
  margin-bottom: 1rem;
}

.state-message.error {
  background: var(--color-error-bg-light);
  color: var(--color-error);
  border: 1px solid var(--color-error-border-light);
}

.state-message.success {
  background: #f5f0ed;
  color: var(--color-primary-medium);
  border: 1px solid var(--color-border);
}

.form-error {
  margin-bottom: 0;
}
</style>
