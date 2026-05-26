<template>
  <div class="home-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="corner-nav">
        <button class="corner-nav-btn" @click="handleNavigate('about')">
          <Info size="16" />
          <span>About</span>
        </button>
        <button class="corner-nav-btn" @click="handleNavigate('staffLogin')">
          <User size="16" />
          <span>Staff Login</span>
        </button>
      </div>

      <div class="hero-bg">
        <img alt="whistlestop coffee hut" src="../assets/background.png" class="hero-image" />
        <div class="hero-overlay"></div>
      </div>

      <div class="hero-content">
        <h1 class="hero-title">whistlestop<br />coffee hut</h1>
        <p class="hero-subtitle">Cramlington Station</p>

        <div class="hero-actions">
          <button class="order-now-btn" @click="handleOrderNow">
            <span>Order Now</span>
            <ArrowRight class="button-icon" size="18" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Info, User, ArrowRight } from 'lucide-vue-next';

const router = useRouter();

const handleOrderNow = () => {
  router.push({ name: 'menu' });
};

const handleNavigate = (routeName) => {
  router.push({ name: routeName });
};
</script>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100dvh;
  background: var(--color-bg-cream);
}

/* Hero Section */
.hero-section {
  position: relative;
  height: 100dvh;
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.corner-nav {
  position: absolute;
  top: clamp(16px, 3vw, 24px);
  left: clamp(16px, 3vw, 24px);
  z-index: 12;
  display: flex;
  gap: clamp(6px, 1.5vw, 12px);
  flex-wrap: wrap;
}

.corner-nav-btn {
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.15);
  color: var(--color-bg-card);
  border-radius: 999px;
  padding: clamp(8px, 1.5vw, 10px) clamp(10px, 2vw, 14px);
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  backdrop-filter: blur(8px);
  font-weight: 600;
  font-size: clamp(0.8rem, 1.5vw, 0.9rem);
  transition: all 0.2s ease;
}

.corner-nav-btn:hover {
  background: rgba(34, 23, 18, 0.7);
}

.hero-bg {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
}

.hero-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.hero-overlay {
  position: absolute;
  inset: 0;
}

.hero-content {
  position: relative;
  z-index: 10;
  text-align: center;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: clamp(16px, 3vw, 32px);
  padding: clamp(40px, 8vh, 64px) clamp(16px, 4vw, 24px);
  margin-top: 10vh;
  max-width: 700px;
  width: 100%;
}

.hero-actions {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  justify-content: center;
  width: 100%;
}

.hero-title {
  font-family: 'Bebas Neue', sans-serif;
  font-size: clamp(40px, 8vw, 72px);
  font-weight: 900;
  margin: 0;
  letter-spacing: clamp(2px, 1vw, 4px);
  color: var(--color-accent);
  line-height: 1.1;
}

.button-icon {
  margin-left: 10px;
}

.hero-subtitle {
  font-size: clamp(18px, 4vw, 28px);
  font-weight: 300;
  margin: 0;
  letter-spacing: clamp(1px, 0.5vw, 2px);
  color: #f5f5f5;
}

.order-now-btn {
  margin-top: clamp(24px, 4vw, 40px);
  padding: clamp(14px, 2vw, 16px) clamp(36px, 6vw, 48px);
  font-size: clamp(15px, 2.5vw, 18px);
  font-weight: 600;
  letter-spacing: 1px;
  color: var(--color-bg-card);
  background: var(--color-accent);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
  text-transform: uppercase;
  width: auto;
}

.order-now-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.4);
}

.order-now-btn:active {
  transform: translateY(-1px);
}

/* Responsive adjustments */
@media (max-width: 600px) {
  .corner-nav-btn span {
    display: none;
  }

  .corner-nav-btn {
    padding: 10px;
    justify-content: center;
  }

  .order-now-btn {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .hero-content {
    padding: 32px 16px;
  }
}
</style>
