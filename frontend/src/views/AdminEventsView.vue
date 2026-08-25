<script setup lang="ts">
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'

const eventos = [
  {
    id: 1,
    titulo: 'Simpósio de Cibersegurança e Defesa',
    organizador: 'Núcleo de Tecnologia',
    data: '10 set 2026',
    status: 'PUBLICADO',
  },
  {
    id: 21,
    titulo: 'Congresso Regional de Inovação',
    organizador: 'Organização #72',
    data: '16 out 2026',
    status: 'EM_REVISAO',
  },
  {
    id: 22,
    titulo: 'Feira de Empreendedorismo Criativo',
    organizador: 'Organização #36',
    data: '22 out 2026',
    status: 'EM_REVISAO',
  },
  {
    id: 23,
    titulo: 'Fórum de Tecnologia Aplicada',
    organizador: 'Organização #19',
    data: '05 nov 2026',
    status: 'SUSPENSO',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Moderação"
      title="Eventos da plataforma"
      description="Revise publicações e aplique as políticas da plataforma com rastreabilidade."
    />
    <section class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Evento</th>
              <th scope="col">Organizador</th>
              <th scope="col">Data</th>
              <th scope="col">Situação</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="evento in eventos" :key="evento.id">
              <td class="ps-4 py-3">
                <span class="fw-bold d-block">{{ evento.titulo }}</span
                ><span class="small text-secondary">#{{ evento.id }}</span>
              </td>
              <td>{{ evento.organizador }}</td>
              <td>{{ evento.data }}</td>
              <td>
                <span
                  class="badge"
                  :class="{
                    'text-bg-success': evento.status === 'PUBLICADO',
                    'text-bg-warning': evento.status === 'EM_REVISAO',
                    'text-bg-secondary': evento.status === 'SUSPENSO',
                  }"
                  >{{ evento.status.replace('_', ' ').toLocaleLowerCase('pt-BR') }}</span
                >
              </td>
              <td class="text-end pe-4">
                <RouterLink
                  class="btn btn-sm btn-outline-primary-custom"
                  :to="`/eventos/${evento.id}`"
                  >Revisar</RouterLink
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
