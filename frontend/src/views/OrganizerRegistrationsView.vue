<script setup lang="ts">
import { computed, ref } from 'vue'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'

const busca = ref('')
const inscricoes = [
  {
    id: 1048,
    participante: 'Ana Martins',
    evento: 'Simpósio de Cibersegurança',
    data: '18 ago 2026',
    status: 'CONFIRMADA',
  },
  {
    id: 1049,
    participante: 'Rafael Costa',
    evento: 'Simpósio de Cibersegurança',
    data: '18 ago 2026',
    status: 'CONFIRMADA',
  },
  {
    id: 1050,
    participante: 'Beatriz Lima',
    evento: 'Encontro de Design & UX',
    data: '19 ago 2026',
    status: 'PENDENTE',
  },
  {
    id: 1051,
    participante: 'João Almeida',
    evento: 'Containers com Docker',
    data: '19 ago 2026',
    status: 'CANCELADA',
  },
]

const resultados = computed(() => {
  const termo = normalizarTexto(busca.value)
  if (!termo) return inscricoes
  return inscricoes.filter((item) =>
    normalizarTexto(`${item.participante} ${item.evento}`).includes(termo),
  )
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Público"
      title="Gestão de inscrições"
      description="Acompanhe os participantes inscritos nos eventos sob sua responsabilidade."
    >
      <template #actions>
        <button class="btn btn-outline-primary-custom" type="button">
          <AppIcon name="report" :size="17" /> Exportar lista
        </button>
      </template>
    </DashboardPageHeader>

    <section class="card border-0 shadow-sm">
      <div class="card-header bg-white p-4">
        <div class="row g-3 align-items-center">
          <div class="col-md-7 col-lg-5">
            <div class="input-group search-input-group">
              <span class="input-group-text"><AppIcon name="search" :size="17" /></span>
              <input
                v-model="busca"
                class="form-control"
                type="search"
                placeholder="Buscar participante ou evento"
              />
            </div>
          </div>
          <div class="col-md-auto ms-md-auto">
            <span class="small text-secondary">{{ resultados.length }} registro(s)</span>
          </div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Inscrição</th>
              <th scope="col">Participante</th>
              <th scope="col">Evento</th>
              <th scope="col">Data</th>
              <th class="pe-4" scope="col">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resultados" :key="item.id">
              <td class="ps-4 fw-semibold">#{{ item.id }}</td>
              <td>{{ item.participante }}</td>
              <td>{{ item.evento }}</td>
              <td class="text-secondary">{{ item.data }}</td>
              <td class="pe-4">
                <span
                  class="badge"
                  :class="{
                    'text-bg-success': item.status === 'CONFIRMADA',
                    'text-bg-warning': item.status === 'PENDENTE',
                    'text-bg-secondary': item.status === 'CANCELADA',
                  }"
                >
                  {{ item.status.toLocaleLowerCase('pt-BR') }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
