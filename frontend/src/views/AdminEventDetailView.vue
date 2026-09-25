<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { consultarEventoAdministrativo } from '@/features/admin/services/administracaoService'
import type { EventoAdministrativo } from '@/features/admin/services/administracaoService'
import { listarFrequencias, listarInscritos } from '@/features/reports/services/relatoriosService'
import type { Frequencia, Inscrito } from '@/features/reports/services/relatoriosService'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'

const route = useRoute()
const evento = ref<EventoAdministrativo | null>(null)
const inscritos = ref<Inscrito[]>([])
const frequencias = ref<Frequencia[]>([])
const carregando = ref(true)
const erro = ref('')
const erroRelatorios = ref('')

async function carregar(): Promise<void> {
  const id = String(route.params.id)
  carregando.value = true
  erro.value = ''
  erroRelatorios.value = ''
  try {
    evento.value = await consultarEventoAdministrativo(id)
    try {
      ;[inscritos.value, frequencias.value] = await Promise.all([
        listarInscritos(id), listarFrequencias(id),
      ])
    } catch (falha) {
      erroRelatorios.value = falha instanceof ApiError ? falha.message : 'Não foi possível consultar os relatórios.'
    }
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível consultar o evento.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <RouterLink to="/admin/eventos" class="d-inline-block mb-3">← Voltar aos eventos</RouterLink>
    <p v-if="carregando" role="status">Carregando dados do evento...</p>
    <p v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</p>
    <template v-if="evento && !carregando">
      <DashboardPageHeader
        eyebrow="Supervisão do evento"
        :title="evento.evento.titulo"
        :description="`${evento.organizadorNome} · ${evento.evento.estado}`"
      />
      <p v-if="erroRelatorios" class="alert alert-warning" role="alert">Relatórios: {{ erroRelatorios }}</p>
      <div class="row g-3 mb-4">
        <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><span class="text-muted">Atividades</span><strong class="h3 d-block">{{ evento.evento.atividades.length }}</strong></div></div></div>
        <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><span class="text-muted">Inscrições</span><strong class="h3 d-block">{{ inscritos.length }}</strong></div></div></div>
        <div class="col-md-4"><div class="card border-0 shadow-sm h-100"><div class="card-body"><span class="text-muted">Relatórios de frequência</span><strong class="h3 d-block">{{ frequencias.length }}</strong></div></div></div>
      </div>
      <section class="card border-0 shadow-sm mb-4"><div class="card-body">
        <h2 class="h5">Dados do evento</h2>
        <p class="mb-2">{{ evento.evento.descricao || 'Sem descrição disponível.' }}</p>
        <div class="small text-muted">ID: {{ evento.evento.id }}</div>
        <div class="small text-muted">Local: {{ evento.evento.local || 'Não informado' }}</div>
        <div class="small text-muted">Início: {{ evento.evento.dataInicio ? formatarData(evento.evento.dataInicio) : 'A definir' }}</div>
      </div></section>
      <section class="card border-0 shadow-sm mb-4"><div class="card-body"><h2 class="h5">Programação</h2>
        <p v-if="!evento.evento.atividades.length" class="text-muted mb-0">Nenhuma atividade cadastrada.</p>
        <ul v-else class="list-group list-group-flush">
          <li v-for="atividade in evento.evento.atividades" :key="atividade.id" class="list-group-item px-0">
            <strong>{{ atividade.titulo }}</strong>
            <span class="d-block small text-muted">{{ atividade.dataInicio ? formatarData(atividade.dataInicio) : 'Sem horário' }} · {{ atividade.local || 'Sem local' }}</span>
          </li>
        </ul>
      </div></section>
      <section class="card border-0 shadow-sm mb-4"><div class="card-body"><h2 class="h5">Inscritos</h2>
        <p v-if="!inscritos.length" class="text-muted mb-0">Nenhuma inscrição encontrada.</p>
        <div v-else class="table-responsive"><table class="table mb-0"><thead><tr><th>Nome</th><th>Situação</th></tr></thead>
          <tbody><tr v-for="pessoa in inscritos" :key="pessoa.usuarioId"><td>{{ pessoa.nome }}</td><td>{{ pessoa.estado }}</td></tr></tbody></table></div>
      </div></section>
      <section class="card border-0 shadow-sm"><div class="card-body"><h2 class="h5">Frequência</h2>
        <p v-if="!frequencias.length" class="text-muted mb-0">Nenhum registro de frequência encontrado.</p>
        <div v-else class="table-responsive"><table class="table mb-0"><thead><tr><th>Participante</th><th>Presenças</th><th>Percentual</th><th>Situação</th></tr></thead>
          <tbody><tr v-for="registro in frequencias" :key="registro.usuarioId"><td>{{ registro.nome }}</td><td>{{ registro.presencasValidas }}/{{ registro.atividadesObrigatorias }}</td><td>{{ registro.percentual }}%</td><td>{{ registro.situacao }}</td></tr></tbody></table></div>
      </div></section>
    </template>
  </div>
</template>
