<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { listarEventosParaGestao } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import { listarRelatorioAtividades, baixarRelatorioAtividades, type RelatorioAtividade, baixarFrequencia, baixarFrequenciaPdf, baixarInscritos, baixarInscritosPdf, listarFrequencias, listarInscritos } from '@/features/reports/services/relatoriosService'
import type { Frequencia, Inscrito } from '@/features/reports/services/relatoriosService'
import { ApiError } from '@/shared/services/httpClient'

const eventos = ref<EventoResumo[]>([])
const eventoId = ref('')
const inscritos = ref<Inscrito[]>([])
const frequencias = ref<Frequencia[]>([])
const carregando = ref(false)
const erro = ref('')
const detalhe = ref<RelatorioAtividade[]>([])
const atividadeFiltro = ref('')
const somenteReservas = ref(false)
const atividadesFiltro = computed(() => Array.from(new Map(detalhe.value.map(a => [a.atividadeId, a.atividadeTitulo])).entries()))
const linhasAtividade = computed(() => detalhe.value.filter(a => (!atividadeFiltro.value || a.atividadeId === atividadeFiltro.value) && (!somenteReservas.value || a.reservada)))
async function exportarAtividades(): Promise<void> {
  try { await baixarRelatorioAtividades(eventoId.value) }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível exportar.' }
}

async function carregar(): Promise<void> {
  inscritos.value = []; frequencias.value = []; detalhe.value = []; atividadeFiltro.value = ''
  if (!eventoId.value) return
  carregando.value = true
  erro.value = ''
  try {
    ;[inscritos.value, frequencias.value, detalhe.value] = await Promise.all([
      listarInscritos(eventoId.value), listarFrequencias(eventoId.value), listarRelatorioAtividades(eventoId.value),
    ])
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar os relatórios.'
  } finally { carregando.value = false }
}

async function baixar(tipo: 'inscritos' | 'frequencia', formato: 'csv' | 'pdf'): Promise<void> {
  erro.value = ''
  try {
    if (tipo === 'inscritos') await (formato === 'csv' ? baixarInscritos(eventoId.value) : baixarInscritosPdf(eventoId.value))
    else await (formato === 'csv' ? baixarFrequencia(eventoId.value) : baixarFrequenciaPdf(eventoId.value))
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível exportar o relatório.' }
}

watch(eventoId, carregar)
onMounted(async () => {
  try {
    eventos.value = await listarEventosParaGestao()
    eventoId.value = eventos.value[0]?.id ?? ''
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar seus eventos.' }
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <h1 class="h2 fw-bold">Relatórios</h1>
    <p class="text-muted">Inscrições e frequência calculadas com os registros persistidos do evento.</p>
    <label class="form-label" for="evento-relatorio">Evento</label>
    <select id="evento-relatorio" v-model="eventoId" class="form-select mb-4">
      <option value="">Selecione um evento</option>
      <option v-for="evento in eventos" :key="evento.id" :value="evento.id">{{ evento.titulo }}</option>
    </select>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="carregando" role="status">Carregando...</p>
    <template v-else-if="eventoId">
      <div class="d-flex flex-wrap gap-2 mb-3">
        <button class="btn btn-outline-primary" @click="baixar('inscritos', 'csv')">Inscritos CSV</button>
        <button class="btn btn-outline-primary" @click="baixar('inscritos', 'pdf')">Inscritos PDF</button>
        <button class="btn btn-outline-primary" @click="baixar('frequencia', 'csv')">Frequência CSV</button>
        <button class="btn btn-outline-primary" @click="baixar('frequencia', 'pdf')">Frequência PDF</button>
      </div>
      <section class="card shadow-sm mb-4">
        <div class="card-body"><h2 class="h5">Inscritos ({{ inscritos.length }})</h2></div>
        <div class="table-responsive"><table class="table mb-0">
          <thead><tr><th>Nome</th><th>E-mail</th><th>Situação</th></tr></thead>
          <tbody><tr v-for="item in inscritos" :key="item.usuarioId"><td>{{ item.nome }}</td><td>{{ item.email }}</td><td>{{ item.estado }}</td></tr></tbody>
        </table></div>
      </section>
      <section class="card shadow-sm">
        <div class="card-body"><h2 class="h5">Frequência</h2></div>
        <div class="table-responsive"><table class="table mb-0">
          <thead><tr><th>Participante</th><th>Presenças</th><th>Percentual</th><th>Mínimo</th><th>Situação</th></tr></thead>
          <tbody><tr v-for="item in frequencias" :key="item.usuarioId"><td>{{ item.nome }}</td><td>{{ item.presencasValidas }}/{{ item.atividadesObrigatorias }}</td><td>{{ item.percentual }}%</td><td>{{ item.minimoExigido }}%</td><td>{{ item.situacao }}</td></tr></tbody>
        </table></div>
      </section>
      <section class="card card-body mt-4">
        <h2 class="h5">Inscrições e frequência por atividade</h2>
        <label class="form-label" for="relatorio-atividade">Atividade</label>
        <select id="relatorio-atividade" v-model="atividadeFiltro" class="form-select mb-2"><option value="">Todas</option><option v-for="[id, nome] in atividadesFiltro" :key="id" :value="id">{{ nome }}</option></select>
        <label class="mb-3"><input v-model="somenteReservas" type="checkbox" class="form-check-input" /> Mostrar somente reservas de atividade</label>
        <p class="small">Vínculos: {{ linhasAtividade.length }} · Reservas ativas: {{ linhasAtividade.filter(a => a.reservada && a.inscricaoEstado === 'ATIVA').length }} · Presenças válidas de inscrições ativas: {{ linhasAtividade.filter(a => a.confirmada && a.inscricaoEstado === 'ATIVA').length }}</p>
        <button class="btn btn-outline-primary mb-3 align-self-start" @click="exportarAtividades">Exportar todas as atividades CSV</button>
        <div class="table-responsive"><table class="table">
          <thead><tr><th>Atividade</th><th>Participante</th><th>Inscrição</th><th>Reserva</th><th>Presença</th><th>Entrada</th><th>Saída</th><th>Origem / ajuste</th></tr></thead>
          <tbody><tr v-for="a in linhasAtividade" :key="a.atividadeId + a.usuarioId"><td>{{ a.atividadeTitulo }}</td><td>{{ a.nome }}</td><td>{{ a.inscricaoEstado }}</td><td>{{ a.reservada ? 'Sim' : 'Sem seleção' }}</td><td>{{ a.confirmada ? 'Confirmada' : 'Não confirmada' }}</td><td>{{ a.entradaEm ? new Date(a.entradaEm).toLocaleString('pt-BR') : '—' }}</td><td>{{ a.saidaEm ? new Date(a.saidaEm).toLocaleString('pt-BR') : '—' }}</td><td>{{ a.origem }} {{ a.motivo }}</td></tr></tbody>
        </table></div>
      </section>
    </template>
  </div>
</template>
