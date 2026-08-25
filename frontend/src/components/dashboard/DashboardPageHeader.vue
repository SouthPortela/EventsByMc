<script setup lang="ts">
defineProps<{
  eyebrow: string
  title: string
  description: string
}>()
</script>

<template>
  <header
    class="d-flex flex-column flex-md-row justify-content-between align-items-md-end gap-3 mb-4"
  >
    <div>
      <p class="small fw-bold text-primary-custom text-uppercase mb-1">{{ eyebrow }}</p>
      <h1 class="h2 fw-bold mb-1">{{ title }}</h1>
      <p class="text-secondary mb-0">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="flex-shrink-0">
      <slot name="actions"></slot>
    </div>
  </header>
</template>
