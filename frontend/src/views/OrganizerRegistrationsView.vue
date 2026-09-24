<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { listarMeusEventos } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { baixarInscritos, listarInscritos } from '@/features/reports/services/relatoriosService'
import type { Inscrito } from '@/features/reports/services/relatoriosService'
import { ApiError } from '@/shared/services/httpClient'

const eventos = ref<EventoResumo[]>([])
const eventoId = ref('')
const inscritos = ref<Inscrito[]>([])
const busca = ref('')
const erro = ref('')
const carregando = ref(false)
const resultados = computed(() => inscritos.value.filter((item) =>
  normalizarTexto(`${item.nome} ${item.email}`).includes(normalizarTexto(busca.value)),
))

async function carregar(): Promise<void> {
  if (!eventoId.value) return
  carregando.value = true
  erro.value = ''
  try { inscritos.value = await listarInscritos(eventoId.value) }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar inscrições.' }
  finally { carregando.value = false }
}

async function exportar(): Promise<void> {
  try { await baixarInscritos(eventoId.value) }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível exportar inscrições.' }
}

watch(eventoId, carregar)
onMounted(async () => {
  try {
    eventos.value = await listarMeusEventos()
    eventoId.value = eventos.value[0]?.id ?? ''
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar eventos.' }
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div><h1 class="h2 fw-bold">Gestão de inscrições</h1><p class="text-muted">Acompanhe inscrições ativas e canceladas.</p></div>
      <button class="btn btn-outline-primary" :disabled="!eventoId" @click="exportar">Exportar CSV</button>
    </div>
    <label class="form-label" for="evento-inscricoes">Evento</label>
    <select id="evento-inscricoes" v-model="eventoId" class="form-select mb-3">
      <option value="">Selecione um evento</option>
      <option v-for="evento in eventos" :key="evento.id" :value="evento.id">{{ evento.titulo }}</option>
    </select>
    <input v-model="busca" type="search" class="form-control mb-3" aria-label="Buscar participante" placeholder="Buscar nome ou e-mail" />
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="carregando" role="status">Carregando...</p>
    <div v-else-if="eventoId" class="table-responsive card shadow-sm">
      <table class="table mb-0"><thead><tr><th>Participante</th><th>E-mail</th><th>Inscrição</th><th>Situação</th></tr></thead>
        <tbody><tr v-for="item in resultados" :key="item.usuarioId">
          <td>{{ item.nome }}</td><td>{{ item.email }}</td><td>{{ new Date(item.inscritaEm).toLocaleString('pt-BR') }}</td><td>{{ item.estado }}</td>
        </tr></tbody>
      </table>
      <p v-if="!resultados.length" class="p-3 text-muted">Nenhuma inscrição encontrada.</p>
    </div>
  </div>
</template>
