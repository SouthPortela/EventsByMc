<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarEventosAdministrativos, moderarEvento } from '@/features/admin/services/administracaoService'
import type { EstadoEvento, EventoAdministrativo } from '@/features/admin/services/administracaoService'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'

type Acao = 'suspensao' | 'restauracao' | 'exclusao'
const eventos = ref<EventoAdministrativo[]>([])
const carregando = ref(true)
const executando = ref(false)
const erro = ref('')
const mensagem = ref('')
const busca = ref('')
const estado = ref<EstadoEvento | ''>('')
const selecionado = ref<EventoAdministrativo | null>(null)
const acao = ref<Acao | null>(null)
const motivo = ref('')

const resultados = computed(() => {
  const termo = normalizarTexto(busca.value)
  return eventos.value.filter((item) => {
    const correspondeEstado = !estado.value || item.evento.estado === estado.value
    const correspondeBusca = !termo || normalizarTexto(
      `${item.evento.titulo} ${item.organizadorNome} ${item.evento.id}`,
    ).includes(termo)
    return correspondeEstado && correspondeBusca
  })
})

async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    eventos.value = await listarEventosAdministrativos()
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível consultar os eventos.'
  } finally {
    carregando.value = false
  }
}

function preparar(item: EventoAdministrativo, proximaAcao: Acao): void {
  selecionado.value = item
  acao.value = proximaAcao
  motivo.value = ''
  erro.value = ''
  mensagem.value = ''
}

async function confirmar(): Promise<void> {
  if (!selecionado.value || !acao.value || executando.value) return
  const justificativa = motivo.value.trim()
  if (justificativa.length < 10 || justificativa.length > 500) {
    erro.value = 'Informe um motivo com 10 a 500 caracteres.'
    return
  }
  if (acao.value === 'exclusao' && !window.confirm(
    'Excluir este evento? A ação é irreversível e removerá seu conteúdo, mantendo apenas os vínculos históricos.',
  )) return

  executando.value = true
  erro.value = ''
  try {
    const atualizado = await moderarEvento(selecionado.value.evento.id, acao.value, justificativa)
    eventos.value = eventos.value.map((item) => item.evento.id === atualizado.evento.id ? atualizado : item)
    mensagem.value = acao.value === 'suspensao' ? 'Evento suspenso.'
      : acao.value === 'restauracao' ? 'Evento restaurado como rascunho.' : 'Evento excluído.'
    selecionado.value = null
    acao.value = null
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível moderar o evento.'
  } finally {
    executando.value = false
  }
}

onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Moderação"
      title="Eventos da plataforma"
      description="Consulte eventos de todos os organizadores e intervenha quando necessário."
    />
    <p v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</p>
    <p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <p v-if="carregando" role="status">Carregando eventos...</p>

    <section v-else class="card border-0 shadow-sm mb-4">
      <div class="card-body d-flex flex-wrap gap-3">
        <label class="flex-grow-1">
          <span class="form-label">Buscar evento ou organizador</span>
          <input v-model="busca" class="form-control" type="search" placeholder="Título, organizador ou ID" />
        </label>
        <label>
          <span class="form-label">Situação</span>
          <select v-model="estado" class="form-select">
            <option value="">Todas</option>
            <option value="RASCUNHO">Rascunho</option>
            <option value="PUBLICADO">Publicado</option>
            <option value="ENCERRADO">Encerrado</option>
            <option value="SUSPENSO">Suspenso</option>
            <option value="EXCLUIDO">Excluído</option>
          </select>
        </label>
      </div>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Evento</th>
              <th scope="col">Organizador</th>
              <th scope="col">Início</th>
              <th scope="col">Situação</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in resultados" :key="item.evento.id">
              <td class="ps-4 py-3">
                <strong class="d-block">{{ item.evento.titulo }}</strong>
                <small class="text-secondary">{{ item.evento.id }}</small>
              </td>
              <td>{{ item.organizadorNome }}</td>
              <td>{{ item.evento.dataInicio ? formatarData(item.evento.dataInicio) : 'A definir' }}</td>
              <td><span class="badge text-bg-primary">{{ item.evento.estado }}</span></td>
              <td class="text-end pe-4">
                <div class="d-inline-flex flex-wrap gap-1 justify-content-end">
                  <RouterLink class="btn btn-sm btn-outline-primary-custom" :to="`/admin/eventos/${item.evento.id}`">Detalhes</RouterLink>
                  <button v-if="item.evento.estado !== 'SUSPENSO' && item.evento.estado !== 'EXCLUIDO'"
                    class="btn btn-sm btn-outline-warning" type="button" @click="preparar(item, 'suspensao')">Suspender</button>
                  <button v-if="item.evento.estado === 'SUSPENSO'"
                    class="btn btn-sm btn-outline-success" type="button" @click="preparar(item, 'restauracao')">Restaurar</button>
                  <button v-if="item.evento.estado !== 'EXCLUIDO'"
                    class="btn btn-sm btn-outline-danger" type="button" @click="preparar(item, 'exclusao')">Excluir</button>
                </div>
              </td>
            </tr>
            <tr v-if="!resultados.length"><td colspan="5" class="text-center text-muted p-4">Nenhum evento encontrado.</td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <section v-if="selecionado && acao" class="card border-0 shadow-sm" aria-label="Confirmar moderação">
      <form class="card-body" @submit.prevent="confirmar">
        <h2 class="h5">{{ acao === 'exclusao' ? 'Excluir' : acao === 'suspensao' ? 'Suspender' : 'Restaurar' }}: {{ selecionado.evento.titulo }}</h2>
        <p class="text-muted">A ação será registrada na auditoria. A restauração volta o evento a rascunho.</p>
        <label for="motivo-moderacao" class="form-label">Motivo obrigatório</label>
        <textarea id="motivo-moderacao" v-model="motivo" class="form-control mb-3" required minlength="10" maxlength="500" rows="3" />
        <div class="d-flex gap-2">
          <button class="btn btn-primary-custom" type="submit" :disabled="executando">{{ executando ? 'Processando...' : 'Confirmar ação' }}</button>
          <button class="btn btn-outline-secondary" type="button" :disabled="executando" @click="selecionado = null; acao = null">Cancelar</button>
        </div>
      </form>
    </section>
  </div>
</template>
