<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AvaliacaoEvento from '@/features/evaluations/components/AvaliacaoEvento.vue'
import { useParticipacao } from '@/features/attendance/composables/useParticipacao'

const eventoId = String(useRoute().params.eventoId ?? '')
const { dados, carregando, erro, recarregar } = useParticipacao()
const inscricao = computed(() => dados.value?.inscricoes.find(i => i.eventoId === eventoId))
const atividades = computed(() => dados.value?.atividades.filter(a => a.eventoId === eventoId) ?? [])
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <h1 class="h2 fw-bold">Avaliações</h1>
    <p v-if="carregando" role="status">Carregando atividades...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">{{ erro }} <button class="btn btn-link" @click="recarregar">Tentar novamente</button></div>
    <p v-else-if="!inscricao || inscricao.estado !== 'ATIVA'" class="alert alert-warning">É necessária uma inscrição ativa neste evento.</p>
    <template v-else>
      <p class="text-muted">{{ inscricao.eventoTitulo }}. A avaliação requer presença válida; questionários ainda não publicados não aparecem aqui.</p>
      <AvaliacaoEvento :evento-id="eventoId" />
      <section v-for="atividade in atividades" :key="atividade.id" class="mb-4">
        <h2 class="h5">{{ atividade.titulo }}</h2>
        <AvaliacaoEvento :atividade-id="atividade.id" />
      </section>
    </template>
  </div>
</template>
