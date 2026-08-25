<script setup lang="ts">
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'

const alertas = [
  {
    id: 1,
    titulo: '3 eventos aguardam revisão',
    descricao: 'Publicações enviadas por organizadores.',
    destino: '/admin/eventos',
  },
  {
    id: 2,
    titulo: '2 solicitações de perfil',
    descricao: 'Pedidos de acesso como organizador.',
    destino: '/admin/usuarios',
  },
  {
    id: 3,
    titulo: 'Auditoria sem ocorrências críticas',
    descricao: 'Nenhuma anomalia detectada nas últimas 24 horas.',
    destino: '/admin/auditoria',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Administração"
      title="Visão geral da plataforma"
      description="Monitore cadastros, publicações e operações protegidas."
    />
    <div class="row g-3 mb-4">
      <div class="col-md-6 col-xl-3">
        <StatCard label="Usuários ativos" value="1.284" icon="users" detail="+38 neste mês" />
      </div>
      <div class="col-md-6 col-xl-3">
        <StatCard
          label="Organizadores"
          :value="96"
          icon="briefcase"
          detail="12 aguardam verificação"
        />
      </div>
      <div class="col-md-6 col-xl-3">
        <StatCard
          label="Eventos publicados"
          :value="142"
          icon="calendar"
          detail="18 nesta semana"
        />
      </div>
      <div class="col-md-6 col-xl-3">
        <StatCard label="Presenças validadas" value="8.461" icon="check" detail="Histórico total" />
      </div>
    </div>
    <section class="card border-0 shadow-sm">
      <div class="card-header bg-white p-4">
        <h2 class="h5 fw-bold mb-0">Atenção necessária</h2>
      </div>
      <div class="list-group list-group-flush">
        <RouterLink
          v-for="alerta in alertas"
          :key="alerta.id"
          class="list-group-item list-group-item-action d-flex align-items-center gap-3 p-4"
          :to="alerta.destino"
        >
          <span class="d-flex rounded-circle bg-primary-subtle text-primary-custom p-3"
            ><AppIcon name="shield" :size="20"
          /></span>
          <span class="flex-grow-1"
            ><strong class="d-block">{{ alerta.titulo }}</strong
            ><span class="small text-secondary">{{ alerta.descricao }}</span></span
          >
          <AppIcon class="text-secondary" name="chevron-right" :size="20" />
        </RouterLink>
      </div>
    </section>
  </div>
</template>
