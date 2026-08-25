<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'

const route = useRoute()
const destino = computed(() =>
  typeof route.query.destino === 'string' &&
  route.query.destino.startsWith('/') &&
  !route.query.destino.startsWith('//')
    ? route.query.destino
    : '/',
)
</script>

<template>
  <div class="container py-5 text-center">
    <div class="category-icon-wrapper mb-4"><AppIcon name="shield" :size="32" /></div>
    <p class="text-primary-custom fw-bold text-uppercase small">Permissão necessária</p>
    <h1 class="display-6 fw-bold">Você não tem acesso a esta área</h1>
    <p class="text-muted mx-auto mb-4">
      Entre com um perfil que possua o nível de privilégio exigido para continuar.
    </p>
    <div class="d-flex justify-content-center gap-2">
      <RouterLink
        class="btn btn-primary-custom px-4"
        :to="{ name: 'login', query: { redirect: destino } }"
        >Entrar com outro perfil</RouterLink
      >
      <RouterLink class="btn btn-outline-secondary px-4" to="/">Voltar ao início</RouterLink>
    </div>
  </div>
</template>
