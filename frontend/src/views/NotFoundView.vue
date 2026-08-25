<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <div class="container text-center py-5">
    <p class="display-1 fw-bold text-primary-custom mb-0">404</p>
    <h1 class="h2">Página não encontrada</h1>
    <p class="text-muted">O endereço informado não corresponde a uma página disponível.</p>
    <RouterLink class="btn btn-primary-custom" to="/">Voltar para o início</RouterLink>
  </div>
</template>
