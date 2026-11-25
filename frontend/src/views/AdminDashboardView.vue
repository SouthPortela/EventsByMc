<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import { consultarResumo, type ResumoAdmin } from '@/features/admin/services/adminService'
import { ApiError } from '@/shared/services/httpClient'

const resumo = ref<ResumoAdmin | null>(null)
const erro = ref('')
const carregando = ref(false)
async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try { resumo.value = await consultarResumo() }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar os indicadores.' }
  finally { carregando.value = false }
}
onMounted(carregar)
</script>
<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Administração" title="Visão geral da plataforma" description="Indicadores calculados com os registros do banco de dados." />
    <button class="btn btn-outline-primary mb-3" :disabled="carregando" @click="carregar">Atualizar</button>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="carregando" role="status">Carregando indicadores...</p>
    <div v-else-if="resumo" class="row g-3">
      <div class="col-md-6 col-xl-3"><StatCard label="Usuários cadastrados" :value="resumo.usuarios" icon="users" /></div>
      <div class="col-md-6 col-xl-3"><StatCard label="Organizadores" :value="resumo.organizadores" icon="briefcase" /></div>
      <div class="col-md-6 col-xl-3"><StatCard label="Eventos publicados" :value="resumo.eventosPublicados" icon="calendar" /></div>
      <div class="col-md-6 col-xl-3"><StatCard label="Eventos bloqueados" :value="resumo.eventosBloqueados" icon="shield" /></div>
      <div class="col-md-6"><StatCard label="Registros de presença" :value="resumo.registrosPresenca" icon="check" detail="Marcações QR/código e manuais, incluindo entrada e saída." /></div>
    </div>
    <div class="d-flex flex-wrap gap-2 mt-4">
      <RouterLink class="btn btn-primary" to="/admin/usuarios">Gerenciar usuários</RouterLink>
      <RouterLink class="btn btn-outline-primary" to="/admin/eventos">Gerenciar eventos</RouterLink>
      <RouterLink class="btn btn-outline-primary" to="/admin/auditoria">Consultar auditoria</RouterLink>
    </div>
  </div>
</template>
