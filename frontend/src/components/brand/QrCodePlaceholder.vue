<script setup lang="ts">
withDefaults(defineProps<{ size?: number }>(), { size: 180 })
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 180 180"
    role="img"
    aria-label="Exemplo visual de QR Code"
  >
    <rect width="180" height="180" rx="12" fill="white" />
    <g fill="#0B3A70">
      <path
        d="M15 15h50v50H15zm10 10v30h30V25zM115 15h50v50h-50zm10 10v30h30V25zM15 115h50v50H15zm10 10v30h30v-30z"
      />
      <path
        d="M80 15h15v15H80zM80 40h25v15H80zM75 65h20v20H75zM105 75h15v15h-15zM130 75h35v15h-35zM15 80h15v20H15zM40 75h20v15H40zM70 100h20v15H70zM95 95h15v35H95zM120 105h15v15h-15zM145 100h20v25h-20zM75 130h15v35H75zM105 140h20v25h-20zM135 130h30v15h-30zM140 155h25v10h-25z"
      />
    </g>
    <g fill="#1E40AF">
      <rect x="32" y="32" width="16" height="16" />
      <rect x="132" y="32" width="16" height="16" />
      <rect x="32" y="132" width="16" height="16" />
    </g>
  </svg>
</template>
