<script setup lang="ts">
import AppIcon, { type IconName } from '@/components/icons/AppIcon.vue'

withDefaults(
  defineProps<{
    label: string
    value: string | number
    icon: IconName
    detail?: string
  }>(),
  { detail: '' },
)
</script>

<template>
  <article class="card stat-card h-100 border-0">
    <div class="card-body p-4">
      <div class="d-flex justify-content-between align-items-start gap-3">
        <div>
          <p class="small text-secondary fw-semibold mb-2">{{ label }}</p>
          <p class="h2 fw-bold mb-1">{{ value }}</p>
          <p v-if="detail" class="small text-secondary mb-0">{{ detail }}</p>
        </div>
        <span class="rounded-circle bg-primary-subtle text-primary-custom p-3">
          <AppIcon :name="icon" :size="22" />
        </span>
      </div>
    </div>
  </article>
</template>
