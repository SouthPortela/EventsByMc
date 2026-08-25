<script setup lang="ts">
import AppIcon, { type IconName } from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'

interface Relatorio {
  titulo: string
  descricao: string
  icon: IconName
}
const relatorios: Relatorio[] = [
  {
    titulo: 'Inscrições por evento',
    descricao: 'Volume, confirmações, cancelamentos e ocupação.',
    icon: 'users',
  },
  {
    titulo: 'Frequência por atividade',
    descricao: 'Comparecimento e ausências em cada etapa do cronograma.',
    icon: 'check',
  },
  {
    titulo: 'Avaliações dos participantes',
    descricao: 'Notas e indicadores consolidados de satisfação.',
    icon: 'report',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Inteligência"
      title="Relatórios"
      description="Acompanhe indicadores para melhorar suas próximas edições."
    />
    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <StatCard label="Taxa de ocupação" value="82%" icon="ticket" detail="+7% no período" />
      </div>
      <div class="col-md-4">
        <StatCard label="Comparecimento" value="74%" icon="check" detail="Média dos eventos" />
      </div>
      <div class="col-md-4">
        <StatCard label="Avaliação média" value="4,7" icon="report" detail="Escala de 1 a 5" />
      </div>
    </div>
    <div class="row g-4">
      <div v-for="relatorio in relatorios" :key="relatorio.titulo" class="col-lg-4">
        <article class="card border-0 shadow-sm h-100">
          <div class="card-body p-4">
            <span
              class="d-inline-flex rounded-circle bg-primary-subtle text-primary-custom p-3 mb-3"
              ><AppIcon :name="relatorio.icon" :size="24"
            /></span>
            <h2 class="h5 fw-bold">{{ relatorio.titulo }}</h2>
            <p class="text-secondary small">{{ relatorio.descricao }}</p>
            <button class="btn btn-outline-primary-custom w-100 mt-2" type="button">
              Gerar relatório
            </button>
          </div>
        </article>
      </div>
    </div>
    <p class="small text-secondary mt-4 mb-0">
      As exportações serão geradas pelo backend com autorização do organizador e filtros
      parametrizados.
    </p>
  </div>
</template>
