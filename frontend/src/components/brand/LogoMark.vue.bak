<script setup lang="ts">
withDefaults(
  defineProps<{
    light?: boolean
    size?: number
  }>(),
  { light: false, size: 40 },
)
</script>

<template>
  <svg
    viewBox="0 0 100 100"
    fill="none"
    :width="size"
    :height="size"
    :stroke="light ? '#ffffff' : '#0b3a70'"
    stroke-width="4"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <polygon points="50,10 90,30 90,70 50,90 10,70 10,30" />
    <polygon points="50,10 70,50 50,90 30,50" :stroke="light ? '#9ca3af' : '#6b7280'" />
    <circle cx="10" cy="30" r="5" :fill="light ? '#ffffff' : '#0b3a70'" />
    <circle cx="50" cy="10" r="5" :fill="light ? '#ffffff' : '#0b3a70'" />
    <circle cx="90" cy="30" r="5" :fill="light ? '#9ca3af' : '#6b7280'" />
    <circle cx="90" cy="70" r="5" :fill="light ? '#ffffff' : '#0b3a70'" />
    <circle cx="50" cy="90" r="5" :fill="light ? '#ffffff' : '#0b3a70'" />
    <circle cx="10" cy="70" r="5" :fill="light ? '#9ca3af' : '#6b7280'" />
    <circle cx="50" cy="50" r="5" :fill="light ? '#ffffff' : '#0b3a70'" />
  </svg>
</template>
