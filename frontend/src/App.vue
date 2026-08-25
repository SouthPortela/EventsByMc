<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const route = useRoute()
</script>

<template>
  <div class="app-shell d-flex flex-column">
    <AppHeader />
    <main class="flex-grow-1"><RouterView /></main>
    <AppFooter v-if="!route.meta.dashboard" />
  </div>
</template>
