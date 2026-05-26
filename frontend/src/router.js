import { createRouter, createWebHistory } from 'vue-router';

const routes = [
  { path: '/', name: 'home', component: () => import('./views/HomePage.vue') },           // landing
  { path: '/about', name: 'about', component: () => import('./views/AboutPage.vue') },    // about
  { path: '/menu', name: 'menu', component: () => import('./views/MenuPage.vue') },       // menu browser
  { path: '/orders', name: 'orders', component: () => import('./views/OrdersPage.vue') }, // order tracking
  { path: '/checkout', name: 'checkout', component: () => import('./views/CheckoutPage.vue') }, // checkout flow
  { path: '/staff/login', name: 'staffLogin', component: () => import('./views/StaffLogin.vue') },           // staff auth
  { path: '/staff/dashboard', name: 'staffDashboard', component: () => import('./views/StaffDashboard.vue') }, // staff hub
  { path: '/staff/opening-hours', name: 'openingHours', component: () => import('./views/OpeningHours.vue') }, // hours mgmt
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
