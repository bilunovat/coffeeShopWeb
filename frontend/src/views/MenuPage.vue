<template>
  <div class="menu-page">
    <div class="menu-header">
      <div class="menu-header-top">
        <button class="home-btn" aria-label="Back to home" @click="goHome">
          <ArrowLeft size="18" />
          <span>Home</span>
        </button>
        <!-- Station Status View -->
        <div class="station-status" aria-live="polite">
          <template v-if="stationName">
            <p class="station-name">{{ stationName }}</p>
            <p class="station-hours">
              <span class="status-pill" :class="{ closed: !isOpenNow }">{{
                isOpenNow ? 'Open now' : 'Closed'
              }}</span>
              <span>{{ todaysHoursText }}</span>
            </p>
          </template>
          <template v-else>
            <p class="station-name">Station info unavailable</p>
            <p class="station-hours">Unable to load opening hours.</p>
          </template>
        </div>
      </div>
      <!-- Menu Title View -->
      <h2>Our Menu</h2>
      <p>Choose your favorite brew and size.</p>
    </div>

    <div v-if="isLoading" class="loading-wrap">
      <Loader2 size="30" class="spin" />
      <p>Brewing your menu...</p>
    </div>
    <!-- Error Wrap View -->
    <div v-else-if="error" class="error-wrap">
      <AlertTriangle size="24" />
      <p>{{ error }}</p>
    </div>

    <!-- Menu Grid -->
    <div v-else class="menu-grid">
      <div v-for="item in menuItems" :key="item.id" class="menu-card">
        <div class="card-image-wrap">
          <img :src="getItemImage(item.name)" :alt="item.name" class="card-image" loading="lazy" />
        </div>

        <h3>{{ item.name }}</h3>
        <p v-if="item.description" class="menu-desc">{{ item.description }}</p>
        <div class="actions">
          <button @click="addItem(item, 'REGULAR')">
            Regular<span>£{{ Number(item.regPrice || 0).toFixed(2) }}</span>
          </button>
          <button @click="addItem(item, 'LARGE')">
            Large<span>£{{ Number(item.largePrice || item.regPrice || 0).toFixed(2) }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useCartStore } from '../stores/cartStore.js';
import { useStationStore } from '../stores/stationStore.js';
import menuService from '../services/menuService.js';
import americanoMilkImg from '../assets/americano_milk.jpg';
import americanoImg from '../assets/americano.jpg';
import cappuccinoImg from '../assets/cappuccino.jpg';
import hotChocolateImg from '../assets/hot_chocolate.jpg';
import latteImg from '../assets/latte.jpg';
import mochaImg from '../assets/mocha.jpg';
import mineralWaterImg from '../assets/mineral_water.png';
import { ArrowLeft, Loader2, AlertTriangle } from 'lucide-vue-next';

const router = useRouter();
const cartStore = useCartStore();

const menuItems = ref([]);
const isLoading = ref(false);
const error = ref('');

const goHome = () => router.push({ name: 'home' });

const imageMap = {
  'Americano with milk': americanoMilkImg,
  Americano: americanoImg,
  Cappuccino: cappuccinoImg,
  'Hot Chocolate': hotChocolateImg,
  Latte: latteImg,
  Mocha: mochaImg,
  'Mineral water': mineralWaterImg,
};

const FALLBACK_URL =
  'https://images.pexels.com/photos/373639/pexels-photo-373639.jpeg?auto=compress&cs=tinysrgb&w=600';

const getItemImage = (name = '') => imageMap[name] || FALLBACK_URL;

const loadMenu = async () => {
  isLoading.value = true;
  error.value = '';
  try {
    menuItems.value = await menuService.getAllMenuItems();
  } catch (err) {
    error.value = err.message || 'Failed to load menu items.';
  } finally {
    isLoading.value = false;
  }
};

// ─── Station / hours logic ───────────────────────────
const stationStore = useStationStore();
const station = computed(() => stationStore.currentStation);
const now = ref(new Date());

// Converts "HH:mm-HH:mm" string into total minutes for time comparison
const parseHourRange = (rangeText) => {
  if (!rangeText || !rangeText.includes('-')) return null;
  const [openText, closeText] = rangeText.split('-').map((part) => part.trim());
  if (!openText || !closeText) return null;
  const [openHour, openMinute] = openText.split(':').map(Number);
  const [closeHour, closeMinute] = closeText.split(':').map(Number);
  if ([openHour, openMinute, closeHour, closeMinute].some((v) => Number.isNaN(v))) return null;
  return {
    openMinutes: openHour * 60 + openMinute,
    closeMinutes: closeHour * 60 + closeMinute,
  };
};

const todaysHoursRaw = computed(() => {
  if (!station.value) return '';
  const day = now.value.getDay();
  if (day === 0 && station.value.closedOnSunday) return 'Closed';
  return (day === 6 || day === 0)
    ? station.value.saturdayOpeningHours
    : station.value.weekdayOpeningHours;
});

const stationName = computed(() => station.value?.name || '');
const todaysHoursText = computed(() => todaysHoursRaw.value || 'Hours unavailable');

const isOpenNow = computed(() => {
  if (!station.value) return false;
  const day = now.value.getDay();
  if (day === 0 && station.value.closedOnSunday) return false;
  const parsed = parseHourRange(todaysHoursRaw.value);
  if (!parsed) return false;
  const minutesNow = now.value.getHours() * 60 + now.value.getMinutes();
  return minutesNow >= parsed.openMinutes && minutesNow <= parsed.closeMinutes;
});

onMounted(() => {
  loadMenu();
  if (!stationStore.currentStation) {
    stationStore.loadStations();
  }
});

const addItem = (item, size) => cartStore.addToCart(item, size);
</script>

<style scoped>
.menu-page {
  max-width: 980px;
  margin: 0 auto;
  min-height: calc(100vh - 160px);
  padding: 26px 16px 24px;
  border-radius: 18px;
  box-shadow: 0 14px 36px rgba(45, 24, 16, 0.08);
  background: var(--color-bg-elevated);
}

.menu-header {
  margin-bottom: 22px;
}

.menu-header-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.home-btn {
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-primary-medium);
  border-radius: 999px;
  padding: 6px 12px;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  cursor: pointer;
}

.menu-desc {
  font-size: 0.98em;
  color: var(--color-primary-medium);
  margin: 6px 18px 8px;
}

.station-status {
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid var(--color-border-light);
  border-radius: 12px;
  padding: 8px 10px;
  min-width: 220px;
  text-align: right;
}

.station-name {
  margin: 0;
  color: var(--color-text);
  font-weight: 700;
  line-height: 1.2;
}

.station-hours {
  margin: 4px 0 0;
  color: var(--color-text-muted);
  font-size: 0.9rem;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  border-radius: 999px;
  padding: 2px 8px;
  font-size: 0.74rem;
  font-weight: 700;
  color: var(--color-status-open);
  background: var(--color-status-open-bg);
}

.status-pill.closed {
  color: var(--color-status-cancelled);
  background: var(--color-status-cancelled-bg);
}

.menu-header h2 {
  margin: 0;
  color: var(--color-text);
}

.menu-header p {
  margin: 6px 0 0;
  color: var(--color-text-muted);
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 16px;
}

.menu-card {
  background: var(--color-bg-cream-light);
  border: 1px solid var(--color-border-light);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: var(--shadow-card-heavy);
  display: flex;
  flex-direction: column;
}

/* ─── Image ──────────────────────────────────────────────────── */
.card-image-wrap {
  width: 100%;
  height: 160px;
  overflow: hidden;
  flex-shrink: 0;
}

.card-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.menu-card:hover .card-image {
  transform: scale(1.05);
}

/* ─── Card content ───────────────────────────────────────────── */
.menu-card h3 {
  margin: 14px 18px 4px;
  color: var(--color-text);
}

.actions {
  margin: auto 18px 14px;
  padding-top: 10px;
  display: flex;
  gap: 8px;
  width: calc(100% - 36px);
}

.actions button {
  flex: 1;
  border: none;
  border-radius: 9px;
  padding: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  background: var(--color-primary);
  color: var(--color-bg-card);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.actions button:hover {
  background: var(--color-accent);
}

.loading-wrap,
.error-wrap {
  display: grid;
  place-items: center;
  gap: 10px;
  color: var(--color-primary-medium);
}

@media (max-width: 768px) {
  .menu-page {
    min-height: calc(100vh - 150px);
    border-radius: 16px 16px 0 0;
    padding-bottom: 32px;
  }

  .menu-header-top {
    flex-direction: column;
    align-items: stretch;
  }

  .station-status {
    text-align: left;
  }
}
</style>
