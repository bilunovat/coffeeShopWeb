<template>
  <div class="about-page">
    <!-- Header Section -->
    <div class="header-section">
      <button class="back-btn" @click="router.push({ name: 'home' })">
        <ArrowLeft size="16" />
        <span>Home</span>
      </button>

      <h1 class="header-title">ABOUT US</h1>
    </div>

    <!-- About Content -->
    <div class="about-content">
      <p class="about-intro">
        Whistlestop coffee hut is the station-side coffee spot at Cramlington Station, where passion
        for coffee meets warm hospitality. Since opening, we've been committed to serving
        exceptional coffee with attention to detail and care for our customers. We source quality
        beans from trusted suppliers and take pride in our craft, ensuring every cup is crafted to
        perfection.

        <br />Our values center on community, sustainability, and delivering an unforgettable coffee
        experience to our neighbors.
      </p>
    </div>

    <!-- Story Section -->
    <div class="story-section">
      <div class="story-image-container">
        <img
          alt="whistlestop coffee hut interior"
          src="../assets/coffee_shop.png"
          class="story-image"
        />
      </div>

      <div class="story-text">
        <h2 class="story-title">whistlestop coffee hut</h2>

        <p class="story-paragraph">
          Welcome to Whistlestop coffee hut, your local haven for exceptional coffee in a warm and
          inviting atmosphere. As soon as you step through our door, you'll be greeted with the rich
          aroma of freshly prepared coffee and the welcoming smiles of our dedicated team.
        </p>

        <p class="story-paragraph">
          Our baristas are passionate professionals who take pride in perfecting their craft. We
          partner with quality suppliers to bring you consistently excellent coffee, carefully
          selected and responsibly sourced. We believe that coffee is more than just a beverage—it's
          a moment of connection and comfort.
        </p>

        <p class="story-paragraph">
          That's why we're dedicated to creating the perfect cup for every customer. Whether you're
          looking for your go-to morning espresso or a leisurely afternoon pour-over, our skilled
          team is here to craft it just right for you. Come in, relax, and be part of the
          whistlestop family.
        </p>
      </div>
    </div>

    <!-- Opening Hours Section -->
    <div class="hours-section">
      <div class="hours-card">
        <div class="hours-header">
          <Clock3 class="hours-icon" size="32" />
          <h2>Opening Hours</h2>
        </div>

        <div class="hours-grid">
          <div class="hours-row">
            <div class="day-info">
              <span class="day-label">Station</span>
            </div>
            <div class="time-info">
              <span class="time-value">{{ station?.name || 'N/A' }}</span>
            </div>
          </div>
          <div class="hours-row">
            <div class="day-info">
              <span class="day-label">Monday – Friday</span>
            </div>
            <div class="time-info">
              <span class="time-value">{{
                station?.weekdayOpeningHours.split('-')[0] || 'N/A'
              }}</span>
              <span class="time-separator">–</span>
              <span class="time-value">{{
                station?.weekdayOpeningHours.split('-')[1] || 'N/A'
              }}</span>
            </div>
          </div>

          <div class="hours-row">
            <div class="day-info">
              <span class="day-label">Saturday</span>
            </div>
            <div class="time-info">
              <span class="time-value">{{
                station?.saturdayOpeningHours.split('-')[0] || 'N/A'
              }}</span>
              <span class="time-separator">–</span>
              <span class="time-value">{{
                station?.saturdayOpeningHours.split('-')[1] || 'N/A'
              }}</span>
            </div>
          </div>

          <div class="hours-row" :class="{ closed: station?.closedOnSunday }">
            <div class="day-info">
              <span class="day-label">Sunday</span>
            </div>
            <div class="time-info">
              <template v-if="station?.closedOnSunday">
                <span class="status-badge closed">Closed</span>
              </template>
              <template v-else>
                <span class="time-value">{{
                  station?.saturdayOpeningHours.split('-')[0] || 'N/A'
                }}</span>
                <span class="time-separator">–</span>
                <span class="time-value">{{
                  station?.saturdayOpeningHours.split('-')[1] || 'N/A'
                }}</span>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-column">
          <h3>ABOUT US</h3>
          <ul>
            <li><a href="#">Our Story</a></li>
            <li><a href="#">Our Services</a></li>
            <li><a href="#">Vacancies</a></li>
          </ul>
        </div>

        <div class="footer-column">
          <h3>SUPPORT</h3>
          <ul>
            <li><a href="#">Contact Us</a></li>
            <li><a href="#">FAQs</a></li>
            <li><a href="#">Shipping & Returns</a></li>
          </ul>
        </div>

        <div class="footer-social">
          <h3>FOLLOW US ON SOCIAL MEDIA</h3>
          <div class="social-links">
            <a href="#"><Instagram size="14" /></a>
            <a href="#"><Facebook size="14" /></a>
            <a href="#"><Twitter size="14" /></a>
          </div>
          <p class="contact-info">
            <Phone size="14" /> +(50) 7931 724 589<br />
            <Mail size="14" /> info@whistlestopcoffeehut.com
          </p>
        </div>
      </div>

      <div class="footer-bottom">
        <p>&copy; 2026 whistlestop coffee hut. All Rights Reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useStationStore } from '../stores/stationStore.js';
import { useRouter } from 'vue-router';
import { Instagram, Facebook, Twitter, Phone, Mail, ArrowLeft, Clock3 } from 'lucide-vue-next';

const router = useRouter();
const stationStore = useStationStore();
const station = computed(() => stationStore.currentStation);

onMounted(() => {
  if (!stationStore.currentStation) {
    stationStore.loadStations();
  }
});
</script>

<style scoped>
.about-page {
  width: 100%;
  background: var(--color-bg-cream);
}

/* Header Section */
.header-section {
  background: var(--color-primary);
  padding: 40px 0;
  position: relative;
}

.back-btn {
  position: absolute;
  top: 20px;
  left: 24px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(34, 23, 18, 0.45);
  color: #fff;
  border-radius: 999px;
  padding: 10px 16px;
  backdrop-filter: blur(8px);
  font-weight: 600;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.back-btn:hover {
  background: rgba(34, 23, 18, 0.75);
}

.header-title {
  text-align: center;
  font-size: 48px;
  font-family: 'Bebas Neue', sans-serif;
  font-weight: 900;
  color: var(--color-bg-card);
  letter-spacing: 4px;
}

/* About Content */
.about-content {
  padding: 60px 40px;
  max-width: 1000px;
  margin: 20px auto 0;
  background: var(--color-bg-card);
  border-radius: 16px;
  position: relative;
  box-shadow: var(--shadow-card);
}

.about-intro {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-body);
  text-align: center;
  margin: 0;
}

/* Story Section */
.story-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 60px;
  padding: 80px 40px 0px 40px;
  max-width: 1400px;
  margin: 0 auto;
  align-items: center;
}

.story-image-container {
  overflow: hidden;
  border-radius: 8px;
  aspect-ratio: 4/5;
}

.story-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.story-text {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.story-title {
  font-size: 40px;
  font-weight: 900;
  font-family: 'Bebas Neue', sans-serif;
  color: var(--color-text-dark);
  margin: 0;
  letter-spacing: 2px;
  line-height: 1.2;
}

.story-paragraph {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-dark);
  margin: 0;
}

/* Opening Hours Section */
.hours-section {
  padding: clamp(40px, 6vw, 80px) clamp(20px, 4vw, 40px);
}

.hours-card {
  max-width: 700px;
  margin: 0 auto;
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: clamp(24px, 4vw, 40px);
  box-shadow: var(--shadow-card);
}

.hours-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: clamp(24px, 3vw, 32px);
  justify-content: center;
}

.hours-icon {
  color: var(--color-text);
}

.hours-header h2 {
  font-size: clamp(22px, 4vw, 28px);
  font-weight: 700;
  color: var(--color-text-dark);
  margin: 0;
}

.hours-grid {
  display: flex;
  flex-direction: column;
  gap: clamp(12px, 2vw, 20px);
}

.hours-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: clamp(12px, 2vw, 16px) clamp(16px, 2vw, 20px);
  background: var(--color-bg-cream-light);
  border-radius: 10px;
  gap: clamp(8px, 2vw, 16px);
}

.hours-row.closed {
  background: var(--color-bg-closed);
}

.day-info {
  display: flex;
  align-items: center;
}

.day-label {
  font-size: clamp(14px, 2vw, 16px);
  font-weight: 600;
  color: var(--color-text);
}

.time-info {
  display: flex;
  align-items: center;
  gap: clamp(4px, 1vw, 8px);
  flex-shrink: 0;
}

.time-value {
  font-size: clamp(13px, 1.8vw, 15px);
  color: var(--color-primary-medium);
  font-weight: 500;
}

.time-separator {
  color: var(--color-text-muted);
  font-weight: 300;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: clamp(12px, 1.5vw, 13px);
  font-weight: 600;
}

.status-badge.closed {
  background: var(--color-error-bg);
  color: var(--color-status-closed);
}

/* Footer */
.footer {
  background: var(--color-primary);
  color: var(--color-bg-card);
  padding: 60px 40px;
}

.footer-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 40px;
  max-width: 1400px;
  margin: 0 auto 40px;
}

.footer-column h3,
.footer-social h3 {
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 1px;
  margin: 0 0 20px 0;
  color: var(--color-bg-card);
}

.footer-column ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-column a {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  font-size: 13px;
  transition: color 0.2s ease;
}

.footer-social {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.social-links {
  display: flex;
  gap: 16px;
}

.social-links a {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-card);
  border-radius: 50%;
  color: var(--color-accent);
  font-size: 14px;
  text-decoration: none;
  transition: all 0.2s ease;
}

.social-links a:hover {
  background: var(--color-accent);
  color: var(--color-bg-card);
}

.contact-info {
  font-size: 13px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

.footer-bottom {
  text-align: center;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1400px;
  margin: 0 auto;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

/* Responsive */
@media (max-width: 768px) {
  .header-title {
    font-size: 36px;
  }

  .story-section {
    grid-template-columns: 1fr;
    padding: 40px 20px;
    gap: 40px;
  }

  .story-image-container {
    aspect-ratio: 1;
  }

  .footer-content {
    grid-template-columns: 1fr;
    gap: 30px;
  }

  .footer-bottom {
    flex-direction: column;
    gap: 20px;
  }

  .about-content {
    padding: 36px 20px;
  }

  .footer {
    padding: 44px 20px;
  }
}

@media (max-width: 480px) {
  .header-section {
    padding: 20px 0;
  }

  .header-title {
    font-size: 28px;
    margin-top: 20px;
    letter-spacing: 1px;
  }

  .about-intro,
  .story-paragraph {
    font-size: 14px;
    line-height: 1.65;
  }

  .footer-bottom {
    align-items: flex-start;
    text-align: left;
  }
}
</style>
