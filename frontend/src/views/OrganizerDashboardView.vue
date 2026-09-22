<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { listarMeusEventos, alterarEstadoEvento } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'

const eventos = ref<EventoResumo[]>([])
const carregando = ref(true)
const erro = ref('')
const mensagem = ref('')
const alterando = ref<string | null>(null)
const publicados = computed(() => eventos.value.filter((e) => e.estado === 'PUBLICADO').length)
const rascunhos = computed(() => eventos.value.filter((e) => e.estado === 'RASCUNHO').length)

async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    eventos.value = await listarMeusEventos()
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar seus eventos.'
  } finally {
    carregando.value = false
  }
}

async function alterar(evento: EventoResumo, acao: 'publicacao' | 'encerramento'): Promise<void> {
  if (alterando.value) return
  if (
    acao === 'encerramento' &&
    !window.confirm(
      'Encerrar este evento? Ele deixará de aparecer no catálogo público e não poderá ser reaberto por esta tela.',
    )
  )
    return
  alterando.value = evento.id
  mensagem.value = ''
  erro.value = ''
  try {
    const atualizado = await alterarEstadoEvento(evento.id, acao)
    eventos.value = eventos.value.map((e) => (e.id === atualizado.id ? atualizado : e))
    mensagem.value = acao === 'publicacao' ? 'Evento publicado.' : 'Evento encerrado.'
  } catch (e) {
    erro.value =
      e instanceof ApiError
        ? e.message
        : 'Não foi possível confirmar a alteração. Atualize a lista antes de tentar novamente.'
  } finally {
    alterando.value = null
  }
}
onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <div class="d-flex flex-wrap justify-content-between gap-3 mb-4">
      <div>
        <h1 class="h2 fw-bold">Meus eventos</h1>
        <p class="text-muted mb-0">Gerencie seus rascunhos e publicações.</p>
      </div>
      <RouterLink class="btn btn-primary-custom align-self-start" to="/organizador/eventos/novo"
        >Criar evento</RouterLink
      >
    </div>
    <p v-if="carregando" role="status">Carregando eventos...</p>
    <div v-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button
        class="btn btn-outline-primary ms-2"
        :disabled="carregando || !!alterando"
        @click="carregar"
      >
        Atualizar lista
      </button>
    </div>
    <div v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</div>
    <template v-if="!carregando">
      <div class="row g-3 mb-4">
        <div
          v-for="item in [
            { titulo: 'Total', valor: eventos.length },
            { titulo: 'Publicados', valor: publicados },
            { titulo: 'Rascunhos', valor: rascunhos },
          ]"
          :key="item.titulo"
          class="col-sm-4"
        >
          <div class="card border-0 shadow-sm">
            <div class="card-body">
              <p class="text-muted mb-1">{{ item.titulo }}</p>
              <strong class="h2">{{ item.valor }}</strong>
            </div>
          </div>
        </div>
      </div>
      <div class="card border-0 shadow-sm">
        <div class="table-responsive">
          <table class="table align-middle mb-0">
            <thead>
              <tr>
                <th class="p-3">Evento</th>
                <th>Início</th>
                <th>Estado</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="evento in eventos" :key="evento.id">
                <td class="p-3">
                  <strong>{{ evento.titulo }}</strong>
                  <div class="small text-muted">{{ evento.local }}</div>
                </td>
                <td>{{ formatarData(evento.dataInicio) }}</td>
                <td>{{ evento.estado }}</td>
                <td>
                  <RouterLink
                    v-if="evento.estado === 'PUBLICADO'"
                    class="btn btn-sm btn-outline-primary me-2"
                    :to="`/eventos/${evento.id}`"
                    >Ver página</RouterLink
                  >
                  <button
                    v-if="evento.estado === 'RASCUNHO'"
                    class="btn btn-sm btn-primary"
                    :disabled="!!alterando"
                    @click="alterar(evento, 'publicacao')"
                  >
                    Publicar
                  </button>
                  <button
                    v-if="evento.estado === 'PUBLICADO'"
                    class="btn btn-sm btn-outline-danger"
                    :disabled="!!alterando"
                    @click="alterar(evento, 'encerramento')"
                  >
                    Encerrar
                  </button>
                </td>
              </tr>
              <tr v-if="!eventos.length && !erro">
                <td colspan="4" class="p-4 text-muted text-center">
                  Você ainda não criou eventos.
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>
