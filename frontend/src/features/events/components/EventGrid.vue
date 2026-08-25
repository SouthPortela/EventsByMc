<script setup lang="ts">
import EventCard from './EventCard.vue'
import type { EventoResumo } from '../types/evento'

defineProps<{
  eventos: EventoResumo[]
}>()
</script>

<template>
  <div v-if="eventos.length > 0" class="row g-4">
    <div v-for="evento in eventos" :key="evento.id" class="col-md-6 col-xl-4">
      <EventCard :evento="evento" />
    </div>
  </div>

  <div v-else class="card border-0 shadow-sm text-center" role="status">
    <div class="card-body py-5">
      <p class="h5 mb-2">Nenhum evento encontrado</p>
      <p class="text-muted mb-0">Tente remover um filtro ou pesquisar por outro termo.</p>
    </div>
  </div>
</template>
