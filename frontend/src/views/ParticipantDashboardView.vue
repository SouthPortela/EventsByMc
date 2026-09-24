<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import { useParticipacao } from '@/features/attendance/composables/useParticipacao'
import { resumirParticipacao } from '@/features/attendance/utils/resumoParticipacao'
import { formatarData } from '@/features/events/utils/formatarData'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const { dados, carregando, erro, recarregar } = useParticipacao()
const resumo = computed(() => (dados.value ? resumirParticipacao(dados.value) : null))
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      :title="`Olá, ${auth.nome?.split(' ')[0] ?? 'participante'}!`"
      description="Acompanhe inscrições, atividades e presenças registradas."
    >
      <template #actions
        ><RouterLink class="btn btn-primary-custom px-4" to="/"
          >Explorar eventos</RouterLink
        ></template
      >
    </DashboardPageHeader>

    <p v-if="carregando" role="status">Carregando sua participação...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button class="btn btn-outline-primary ms-3" type="button" @click="recarregar">
        Tentar novamente
      </button>
    </div>
    <template v-else-if="resumo">
      <div class="row g-3 mb-4">
        <div class="col-md-4">
          <StatCard label="Inscrições ativas" :value="resumo.inscricoesAtivas" icon="ticket" />
        </div>
        <div class="col-md-4">
          <StatCard label="Atividades futuras" :value="resumo.atividadesFuturas" icon="calendar" />
        </div>
        <div class="col-md-4">
          <StatCard
            label="Presenças confirmadas"
            :value="resumo.presencasConfirmadas"
            icon="check"
          />
        </div>
      </div>

      <div class="row g-4">
        <div class="col-xl-8">
          <section class="card border-0 shadow-sm h-100">
            <div class="card-header bg-white d-flex justify-content-between align-items-center p-4">
              <h2 class="h5 fw-bold mb-0">Próxima atividade</h2>
              <RouterLink
                class="small fw-bold text-primary-custom text-decoration-none"
                to="/participante/agenda"
                >Ver agenda</RouterLink
              >
            </div>
            <div class="card-body p-4">
              <template v-if="resumo.proxima">
                <p class="small text-primary fw-semibold mb-2">
                  {{ formatarData(resumo.proxima.dataInicio) }}
                </p>
                <h3 class="h4 fw-bold mb-2">{{ resumo.proxima.titulo }}</h3>
                <p class="text-secondary mb-2">{{ resumo.proxima.eventoTitulo }}</p>
                <p class="small d-flex align-items-center gap-2 mb-0">
                  <AppIcon name="map-pin" :size="16" />
                  {{ resumo.proxima.local ?? 'Local a divulgar' }}
                </p>
              </template>
              <p v-else class="text-secondary mb-0">
                Você ainda não possui atividades futuras nos eventos em que está inscrito.
              </p>
            </div>
          </section>
        </div>
        <div class="col-xl-4">
          <section class="card border-0 shadow-sm h-100">
            <div class="card-body text-center p-4">
              <span
                class="d-inline-flex rounded-circle bg-primary-subtle text-primary-custom p-3 mb-3"
                ><AppIcon name="check" :size="24"
              /></span>
              <h2 class="h5 fw-bold">Confirmação de presença</h2>
              <p class="small text-secondary">
                Quando o organizador abrir a chamada, escaneie o QR Code exibido por ele ou digite o
                código.
              </p>
              <RouterLink class="btn btn-outline-primary-custom w-100" to="/presenca"
                >Confirmar presença</RouterLink
              >
            </div>
          </section>
        </div>
      </div>
    </template>
  </div>
</template>
