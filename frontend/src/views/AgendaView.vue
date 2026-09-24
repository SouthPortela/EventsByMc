<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { consultarAgenda, removerAgenda, type ItemAgenda } from '@/features/events/services/programacaoService'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'

const itens = ref<ItemAgenda[]>([])
const carregando = ref(true)
const ocupado = ref('')
const erro = ref('')

async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    itens.value = await consultarAgenda()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a agenda.'
  } finally {
    carregando.value = false
  }
}
async function remover(atividadeId: string): Promise<void> {
  if (ocupado.value) return
  ocupado.value = atividadeId
  erro.value = ''
  try {
    await removerAgenda(atividadeId)
    itens.value = itens.value.filter(item => item.atividade.id !== atividadeId)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível remover a atividade.'
  } finally {
    ocupado.value = ''
  }
}
onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      title="Minha agenda"
      description="Atividades que você escolheu, salvas na sua conta e ordenadas por horário."
    >
      <template #actions><span class="badge rounded-pill text-bg-primary-subtle text-primary px-3 py-2">{{ itens.length }} atividades</span></template>
    </DashboardPageHeader>
    <p v-if="carregando" role="status">Carregando agenda...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }} <button class="btn btn-outline-primary ms-3" type="button" @click="carregar">Tentar novamente</button>
    </div>
    <div v-else-if="!itens.length" class="card border-0 shadow-sm">
      <div class="card-body p-4">Sua agenda está vazia. Abra um evento, inscreva-se e escolha as atividades da programação. <RouterLink to="/">Explorar eventos</RouterLink></div>
    </div>
    <div v-else class="vstack gap-3">
      <article v-for="item in itens" :key="item.atividade.id" class="card border-0 shadow-sm">
        <div class="card-body p-4 d-flex flex-wrap justify-content-between align-items-center gap-3">
          <div>
            <span class="small fw-semibold text-primary">{{ formatarData(item.atividade.dataInicio) }}</span>
            <h2 class="h5 fw-bold mb-1">{{ item.atividade.titulo }}</h2>
            <p class="small text-secondary mb-0">{{ item.atividade.espaco ?? item.atividade.local }}<span v-if="item.atividade.trilha"> · {{ item.atividade.trilha }}</span></p>
          </div>
          <button class="btn btn-sm btn-outline-danger" type="button" :disabled="!!ocupado" @click="remover(item.atividade.id)">Remover da agenda</button>
        </div>
      </article>
    </div>
  </div>
</template>
