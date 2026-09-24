<script setup lang="ts">
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { useParticipacao } from '@/features/attendance/composables/useParticipacao'
import { formatarData } from '@/features/events/utils/formatarData'

const { dados, carregando, erro, recarregar } = useParticipacao()
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      title="Minha agenda"
      description="Atividades dos eventos em que sua inscrição está ativa, em ordem cronológica."
    >
      <template #actions
        ><span v-if="dados" class="badge rounded-pill text-bg-primary-subtle text-primary px-3 py-2"
          >{{ dados.atividades.length }} atividades</span
        ></template
      >
    </DashboardPageHeader>

    <p v-if="carregando" role="status">Carregando agenda...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button class="btn btn-outline-primary ms-3" type="button" @click="recarregar">
        Tentar novamente
      </button>
    </div>
    <div v-else-if="!dados?.atividades.length" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        Ainda não há atividades nos seus eventos inscritos.
        <RouterLink to="/">Explorar eventos</RouterLink>
      </div>
    </div>
    <div v-else class="vstack gap-3">
      <article
        v-for="atividade in dados.atividades"
        :key="atividade.id"
        class="card border-0 shadow-sm"
      >
        <div class="card-body p-4">
          <div class="row align-items-center g-3">
            <div class="col-md-2">
              <span class="small fw-semibold text-primary">{{
                formatarData(atividade.dataInicio)
              }}</span>
            </div>
            <div class="col-md-7">
              <h2 class="h5 fw-bold mb-1">{{ atividade.titulo }}</h2>
              <p class="text-muted mb-1">{{ atividade.eventoTitulo }}</p>
              <p class="small text-secondary mb-0">{{ atividade.local ?? 'Local a divulgar' }}</p>
            </div>
            <div class="col-md-3 text-md-end">
              <span v-if="atividade.presencaRegistradaEm" class="badge text-bg-success"
                >Presença confirmada</span
              >
              <RouterLink v-else class="btn btn-sm btn-outline-primary-custom" to="/presenca"
                >Confirmar presença</RouterLink
              >
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
