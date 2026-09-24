<script setup lang="ts">
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { useParticipacao } from '@/features/attendance/composables/useParticipacao'
import { formatarData } from '@/features/events/utils/formatarData'

const { dados, carregando, erro, recarregar } = useParticipacao()
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Participação"
      title="Minhas inscrições"
      description="Consulte os eventos em que você se inscreveu e a situação de cada inscrição."
    >
      <template #actions
        ><RouterLink class="btn btn-primary-custom" to="/">Encontrar eventos</RouterLink></template
      >
    </DashboardPageHeader>

    <p v-if="carregando" role="status">Carregando inscrições...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button class="btn btn-outline-primary ms-3" type="button" @click="recarregar">
        Tentar novamente
      </button>
    </div>
    <div v-else-if="!dados?.inscricoes.length" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        Você ainda não se inscreveu em nenhum evento.
        <RouterLink to="/">Explorar eventos</RouterLink>
      </div>
    </div>
    <div v-else class="card border-0 shadow-sm">
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
            <tr v-for="inscricao in dados.inscricoes" :key="inscricao.id">
              <td class="ps-4 py-3">
                <span class="fw-bold d-block">{{ inscricao.eventoTitulo }}</span
                ><span class="small text-secondary">Inscrição {{ inscricao.id.slice(0, 8) }}</span>
              </td>
              <td>
                <span class="small fw-semibold d-block">{{
                  formatarData(inscricao.eventoInicio)
                }}</span
                ><span class="small text-secondary d-flex align-items-center gap-1"
                  ><AppIcon name="map-pin" :size="14" />
                  {{ inscricao.eventoLocal ?? 'Local a divulgar' }}</span
                >
              </td>
              <td>
                <span
                  class="badge"
                  :class="inscricao.estado === 'ATIVA' ? 'text-bg-success' : 'text-bg-secondary'"
                  >{{ inscricao.estado === 'ATIVA' ? 'Ativa' : 'Cancelada' }}</span
                >
              </td>
              <td class="text-end pe-4">
                <RouterLink
                  v-if="inscricao.eventoEstado === 'PUBLICADO'"
                  class="btn btn-sm btn-outline-primary-custom"
                  :to="`/eventos/${inscricao.eventoId}`"
                  >Detalhes</RouterLink
                >
                <span v-else class="small text-secondary">{{
                  inscricao.eventoEstado === 'ENCERRADO' ? 'Evento encerrado' : 'Fora do catálogo'
                }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
