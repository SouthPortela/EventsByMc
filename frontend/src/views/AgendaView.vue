<script setup lang="ts">
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'

const atividades = [
  {
    id: 1,
    data: '10 SET',
    horario: '19:00',
    titulo: 'Abertura e Tendências de Ameaças',
    evento: 'Simpósio de Cibersegurança e Defesa',
    local: 'Auditório Principal',
    presenca: 'PENDENTE',
  },
  {
    id: 2,
    data: '10 SET',
    horario: '19:45',
    titulo: 'Segurança em APIs REST e OAuth2',
    evento: 'Simpósio de Cibersegurança e Defesa',
    local: 'Auditório Principal',
    presenca: 'PENDENTE',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      title="Minha agenda"
      description="Atividades escolhidas em ordem cronológica."
    >
      <template #actions>
        <span class="badge rounded-pill text-bg-primary-subtle text-primary px-3 py-2">
          {{ atividades.length }} atividades
        </span>
      </template>
    </DashboardPageHeader>

    <div class="alert alert-primary border-0" role="alert">
      A API validará vagas e conflitos de horário antes de confirmar cada atividade na agenda.
    </div>

    <div class="vstack gap-3">
      <article v-for="atividade in atividades" :key="atividade.id" class="card border-0 shadow-sm">
        <div class="card-body p-4">
          <div class="row align-items-center g-3">
            <div class="col-4 col-md-2 col-lg-1 text-center">
              <div class="bg-primary-subtle text-primary rounded-3 p-2">
                <span class="fw-bold d-block">{{ atividade.data }}</span>
                <span class="small">{{ atividade.horario }}</span>
              </div>
            </div>
            <div class="col-8 col-md-7 col-lg-8">
              <h2 class="h5 fw-bold mb-1">{{ atividade.titulo }}</h2>
              <p class="text-muted mb-1">{{ atividade.evento }}</p>
              <p class="small text-secondary mb-0">{{ atividade.local }}</p>
            </div>
            <div class="col-md-3 text-md-end">
              <span class="badge text-bg-warning">Presença pendente</span>
            </div>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>
