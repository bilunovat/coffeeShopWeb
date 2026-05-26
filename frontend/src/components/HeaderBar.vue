<script setup></script>
<template>
  <!-- Sticky Header -->
  <header class="header-bar">
    <div class="header-content">
      <h1>Whistlestop Coffee Hut</h1>
    </div>
  </header>
</template>

<style scoped>
.header-bar {
  width: 100%;
  background-color: var(--color-primary);
  color: var(--color-text-light);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: var(--shadow-header);
}

.header-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 25px 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

h1 {
  font-size: 2rem;
  font-family: 'Bebas Neue', sans-serif;
  margin: 0;
  letter-spacing: 1px;
  font-weight: 700;
}

@media (max-width: 768px) {
  .header-content {
    padding: 12px 14px;
    gap: 8px;
  }

  h1 {
    font-size: 1.5rem;
    letter-spacing: 1px;
  }
}
</style>
