<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import { listarEventosAdministrativos, listarModeracoes, listarUsuariosAdministrativos } from '@/features/admin/services/administracaoService'
import type { EventoAdministrativo, RegistroModeracao, UsuarioAdministrativo } from '@/features/admin/services/administracaoService'
import { ApiError } from '@/shared/services/httpClient'

const eventos = ref<EventoAdministrativo[]>([])
const usuarios = ref<UsuarioAdministrativo[]>([])
const moderacoes = ref<RegistroModeracao[]>([])
const carregando = ref(true)
const erro = ref('')
const publicados = computed(() => eventos.value.filter((item) => item.evento.estado === 'PUBLICADO').length)
const suspensos = computed(() => eventos.value.filter((item) => item.evento.estado === 'SUSPENSO').length)
const organizadores = computed(() => usuarios.value.filter((item) => item.perfis.includes('ORGANIZADOR')).length)

onMounted(async () => {
  try {
    ;[eventos.value, usuarios.value, moderacoes.value] = await Promise.all([
      listarEventosAdministrativos(), listarUsuariosAdministrativos(), listarModeracoes(),
    ])
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível carregar o painel.'
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Administração" title="Visão geral da plataforma"
      description="Acompanhe eventos, usuários e decisões de moderação em tempo real." />
    <p v-if="carregando" role="status">Carregando painel...</p>
    <p v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</p>
    <template v-if="!carregando && !erro">
      <div class="row g-3 mb-4">
        <div class="col-md-6 col-xl-3"><StatCard label="Usuários" :value="usuarios.length" icon="users" detail="Contas cadastradas" /></div>
        <div class="col-md-6 col-xl-3"><StatCard label="Organizadores" :value="organizadores" icon="briefcase" detail="Com perfil de organização" /></div>
        <div class="col-md-6 col-xl-3"><StatCard label="Eventos publicados" :value="publicados" icon="calendar" detail="Visíveis no catálogo" /></div>
        <div class="col-md-6 col-xl-3"><StatCard label="Eventos suspensos" :value="suspensos" icon="shield" detail="Retirados de circulação" /></div>
      </div>
      <section class="card border-0 shadow-sm"><div class="card-body">
        <h2 class="h5">Supervisão</h2>
        <p class="text-muted">{{ eventos.length }} eventos cadastrados · {{ moderacoes.length }} decisões registradas.</p>
        <div class="d-flex flex-wrap gap-2">
          <RouterLink to="/admin/eventos" class="btn btn-primary-custom">Gerenciar eventos</RouterLink>
          <RouterLink to="/admin/usuarios" class="btn btn-outline-primary-custom">Ver usuários</RouterLink>
          <RouterLink to="/admin/auditoria" class="btn btn-outline-primary-custom">Ver auditoria</RouterLink>
        </div>
      </div></section>
    </template>
  </div>
</template>
