<script setup lang="ts">
export type IconName =
  | 'search'
  | 'map-pin'
  | 'calendar'
  | 'user'
  | 'menu'
  | 'x'
  | 'heart'
  | 'share'
  | 'chevron-right'
  | 'music'
  | 'briefcase'
  | 'monitor'
  | 'ticket'
  | 'dashboard'
  | 'users'
  | 'settings'
  | 'report'
  | 'check'
  | 'logout'
  | 'plus'
  | 'clipboard'
  | 'shield'
  | 'book'

withDefaults(defineProps<{ name: IconName; size?: number }>(), { size: 20 })
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-if="name === 'search'">
      <circle cx="11" cy="11" r="8" />
      <path d="m21 21-4.3-4.3" />
    </template>
    <template v-else-if="name === 'map-pin'">
      <path d="M20 10c0 5-8 12-8 12S4 15 4 10a8 8 0 1 1 16 0Z" />
      <circle cx="12" cy="10" r="3" />
    </template>
    <template v-else-if="name === 'calendar'">
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M16 3v4M8 3v4M3 11h18" />
    </template>
    <template v-else-if="name === 'user'">
      <path d="M19 21a7 7 0 0 0-14 0" />
      <circle cx="12" cy="7" r="4" />
    </template>
    <template v-else-if="name === 'menu'"><path d="M4 6h16M4 12h16M4 18h16" /></template>
    <template v-else-if="name === 'x'"><path d="m18 6-12 12M6 6l12 12" /></template>
    <template v-else-if="name === 'heart'">
      <path
        d="M20.8 4.6a5.5 5.5 0 0 0-7.8 0L12 5.7l-1.1-1.1a5.5 5.5 0 0 0-7.8 7.8l1.1 1.1L12 21l7.8-7.5 1.1-1.1a5.5 5.5 0 0 0-.1-7.8Z"
      />
    </template>
    <template v-else-if="name === 'share'">
      <circle cx="18" cy="5" r="3" />
      <circle cx="6" cy="12" r="3" />
      <circle cx="18" cy="19" r="3" />
      <path d="m8.6 10.5 6.8-4M8.6 13.5l6.8 4" />
    </template>
    <template v-else-if="name === 'chevron-right'"><path d="m9 18 6-6-6-6" /></template>
    <template v-else-if="name === 'music'">
      <path d="M9 18V5l12-2v13M9 9l12-2" />
      <circle cx="6" cy="18" r="3" />
      <circle cx="18" cy="16" r="3" />
    </template>
    <template v-else-if="name === 'briefcase'">
      <rect x="3" y="7" width="18" height="13" rx="2" />
      <path d="M8 7V4h8v3M3 12h18M10 12v2h4v-2" />
    </template>
    <template v-else-if="name === 'monitor'">
      <rect x="2" y="3" width="20" height="14" rx="2" />
      <path d="M8 21h8M12 17v4" />
      <path d="m10 8 5 3-5 3Z" />
    </template>
    <template v-else-if="name === 'ticket'">
      <path d="M2 9a3 3 0 0 0 0 6v3h20v-3a3 3 0 0 0 0-6V6H2Z" />
      <path d="M13 6v2M13 11v2M13 16v2" />
    </template>
    <template v-else-if="name === 'dashboard'">
      <rect x="3" y="3" width="7" height="7" />
      <rect x="14" y="3" width="7" height="7" />
      <rect x="3" y="14" width="7" height="7" />
      <rect x="14" y="14" width="7" height="7" />
    </template>
    <template v-else-if="name === 'users'">
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.9M16 3.1a4 4 0 0 1 0 7.8" />
    </template>
    <template v-else-if="name === 'settings'">
      <circle cx="12" cy="12" r="3" />
      <path
        d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1-2.8 2.8-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.6v.2h-4V21a1.7 1.7 0 0 0-1-1.6 1.7 1.7 0 0 0-1.9.3l-.1.1L4.2 17l.1-.1a1.7 1.7 0 0 0 .3-1.9A1.7 1.7 0 0 0 3 14H2.8v-4H3a1.7 1.7 0 0 0 1.6-1 1.7 1.7 0 0 0-.3-1.9L4.2 7 7 4.2l.1.1a1.7 1.7 0 0 0 1.9.3A1.7 1.7 0 0 0 10 3V2.8h4V3a1.7 1.7 0 0 0 1 1.6 1.7 1.7 0 0 0 1.9-.3l.1-.1L19.8 7l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.6 1h.2v4H21a1.7 1.7 0 0 0-1.6 1Z"
      />
    </template>
    <template v-else-if="name === 'report'"><path d="M4 19V9M10 19V5M16 19v-7M22 19H2" /></template>
    <template v-else-if="name === 'check'"><path d="m20 6-11 11-5-5" /></template>
    <template v-else-if="name === 'logout'">
      <path d="M10 17l5-5-5-5M15 12H3M21 19V5a2 2 0 0 0-2-2h-6" />
    </template>
    <template v-else-if="name === 'plus'"><path d="M12 5v14M5 12h14" /></template>
    <template v-else-if="name === 'clipboard'">
      <rect x="5" y="4" width="14" height="17" rx="2" />
      <path d="M9 4V2h6v2M9 10h6M9 14h6M9 18h4" />
    </template>
    <template v-else-if="name === 'shield'">
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10Z" />
      <path d="m9 12 2 2 4-4" />
    </template>
    <template v-else-if="name === 'book'">
      <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
      <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2Z" />
    </template>
  </svg>
</template>
