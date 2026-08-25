<script setup lang="ts">
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'

const inscricoes = [
  {
    id: 1,
    eventoId: 1,
    titulo: 'Simpósio de Cibersegurança e Defesa',
    data: '10 set 2026 · 19:00',
    local: 'Auditório Principal',
    status: 'CONFIRMADA',
  },
  {
    id: 2,
    eventoId: 3,
    titulo: 'Encontro de Design de Interfaces & UX',
    data: '18 set 2026 · 18:30',
    local: 'Sala Multiuso',
    status: 'CONFIRMADA',
  },
  {
    id: 3,
    eventoId: 9,
    titulo: 'Hackathon Universitário de Inovação',
    data: '10 out 2026 · 08:00',
    local: 'Ginásio Poliesportivo',
    status: 'EM_ANALISE',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Participação"
      title="Minhas inscrições"
      description="Consulte o status e acesse os detalhes dos eventos em que você se inscreveu."
    >
      <template #actions>
        <RouterLink class="btn btn-primary-custom" to="/">Encontrar eventos</RouterLink>
      </template>
    </DashboardPageHeader>

    <div class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Evento</th>
              <th scope="col">Data e local</th>
              <th scope="col">Situação</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inscricao in inscricoes" :key="inscricao.id">
              <td class="ps-4 py-3">
                <span class="fw-bold d-block">{{ inscricao.titulo }}</span>
                <span class="small text-secondary">Inscrição #{{ inscricao.id }}</span>
              </td>
              <td>
                <span class="small fw-semibold d-block">{{ inscricao.data }}</span>
                <span class="small text-secondary d-flex align-items-center gap-1">
                  <AppIcon name="map-pin" :size="14" /> {{ inscricao.local }}
                </span>
              </td>
              <td>
                <span
                  class="badge"
                  :class="inscricao.status === 'CONFIRMADA' ? 'text-bg-success' : 'text-bg-warning'"
                >
                  {{ inscricao.status === 'CONFIRMADA' ? 'Confirmada' : 'Em análise' }}
                </span>
              </td>
              <td class="text-end pe-4">
                <RouterLink
                  class="btn btn-sm btn-outline-primary-custom"
                  :to="`/eventos/${inscricao.eventoId}`"
                >
                  Detalhes
                </RouterLink>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
