<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import { listarEventos } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import { formatarData } from '@/features/events/utils/formatarData'

const eventos = ref<EventoResumo[]>([])

onMounted(async () => {
  eventos.value = await listarEventos()
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Painel operacional"
      title="Gestão de eventos"
      description="Acompanhe publicação, inscrições e programação."
    >
      <template #actions>
        <RouterLink class="btn btn-primary-custom btn-lg" to="/organizador/eventos/novo"
          >Criar evento</RouterLink
        >
      </template>
    </DashboardPageHeader>

    <div class="alert alert-primary border-0" role="alert">
      Este painel está preparado para autorização por perfil. A API deverá permitir acesso apenas a
      organizadores.
    </div>

    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <StatCard
          label="Eventos cadastrados"
          :value="eventos.length"
          icon="calendar"
          detail="5 publicados"
        />
      </div>
      <div class="col-md-4">
        <StatCard
          label="Inscrições confirmadas"
          :value="148"
          icon="users"
          detail="+24 esta semana"
        />
      </div>
      <div class="col-md-4">
        <StatCard label="Atividades publicadas" :value="12" icon="clipboard" detail="3 hoje" />
      </div>
    </div>

    <section class="card border-0 shadow-sm">
      <div class="card-header bg-white border-bottom p-4">
        <h2 class="h5 fw-bold mb-0">Seus eventos</h2>
      </div>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Evento</th>
              <th scope="col">Data</th>
              <th scope="col">Situação</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="evento in eventos.slice(0, 5)" :key="evento.id">
              <td class="ps-4">
                <span class="fw-semibold d-block">{{ evento.titulo }}</span>
                <span class="small text-muted">{{ evento.local }}</span>
              </td>
              <td>{{ formatarData(evento.dataInicio) }}</td>
              <td><span class="badge text-bg-success">Publicado</span></td>
              <td class="text-end pe-4">
                <RouterLink
                  class="btn btn-sm btn-outline-primary-custom"
                  :to="`/eventos/${evento.id}`"
                >
                  Visualizar
                </RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
