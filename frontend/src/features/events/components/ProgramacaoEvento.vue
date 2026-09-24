<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import {
  adicionarAgenda, consultarAgenda, listarProgramacao, removerAgenda,
  type AtividadeProgramacao, type FiltrosProgramacao,
} from '../services/programacaoService'
import { formatarData } from '../utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'
import { useAuthStore } from '@/stores/auth'
import AvaliacaoEvento from '@/features/evaluations/components/AvaliacaoEvento.vue'

const props = defineProps<{ eventoId: string }>()
const route = useRoute()
const auth = useAuthStore()
const todas = ref<AtividadeProgramacao[]>([])
const atividades = ref<AtividadeProgramacao[]>([])
const selecionadas = ref<string[]>([])
const carregando = ref(true)
const ocupada = ref('')
const avaliacoesAbertas = ref<string[]>([])
const erro = ref('')
const filtros = reactive<FiltrosProgramacao>({ trilhaId: '', espacoId: '', tipo: '', de: '', ate: '' })
const trilhas = computed(() => [...new Map(todas.value.filter(a => a.trilhaId).map(a => [a.trilhaId, { id: a.trilhaId, nome: a.trilha }])).values()])
const espacos = computed(() => [...new Map(todas.value.filter(a => a.espacoId).map(a => [a.espacoId, { id: a.espacoId, nome: a.espaco }])).values()])

async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    todas.value = await listarProgramacao(props.eventoId)
    atividades.value = todas.value
    if (auth.autenticado) {
      selecionadas.value = (await consultarAgenda()).map(item => item.atividade.id)
    }
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a programação.'
  } finally {
    carregando.value = false
  }
}

async function aplicarFiltros(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    atividades.value = await listarProgramacao(props.eventoId, filtros)
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível filtrar a programação.'
  } finally {
    carregando.value = false
  }
}

async function alternarAgenda(atividade: AtividadeProgramacao): Promise<void> {
  if (ocupada.value) return
  ocupada.value = atividade.id
  erro.value = ''
  try {
    if (selecionadas.value.includes(atividade.id)) {
      await removerAgenda(atividade.id)
      selecionadas.value = selecionadas.value.filter(id => id !== atividade.id)
    } else {
      await adicionarAgenda(atividade.id)
      selecionadas.value = [...selecionadas.value, atividade.id]
    }
  } catch (e) {
    erro.value = e instanceof ApiError ? e.message : 'Não foi possível alterar sua agenda.'
  } finally {
    ocupada.value = ''
  }
}
function alternarAvaliacao(evento: Event, id: string): void {
  const aberto = (evento.target as HTMLDetailsElement).open
  avaliacoesAbertas.value = aberto
    ? [...avaliacoesAbertas.value, id]
    : avaliacoesAbertas.value.filter((atual) => atual !== id)
}
onMounted(carregar)
</script>

<template>
  <section class="mb-5">
    <p class="text-primary-custom fw-semibold small text-uppercase mb-1">Cronograma</p>
    <h2 class="h3 fw-bold mb-3">Programação do evento</h2>
    <form class="card border-0 shadow-sm mb-4" @submit.prevent="aplicarFiltros">
      <div class="card-body row g-3 align-items-end">
        <div class="col-md-4">
          <label for="programacao-trilha" class="form-label">Trilha</label>
          <select id="programacao-trilha" v-model="filtros.trilhaId" class="form-select">
            <option value="">Todas</option>
            <option v-for="trilha in trilhas" :key="trilha.id!" :value="trilha.id!">{{ trilha.nome }}</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="programacao-espaco" class="form-label">Espaço</label>
          <select id="programacao-espaco" v-model="filtros.espacoId" class="form-select">
            <option value="">Todos</option>
            <option v-for="espaco in espacos" :key="espaco.id!" :value="espaco.id!">{{ espaco.nome }}</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="programacao-tipo" class="form-label">Tipo</label>
          <select id="programacao-tipo" v-model="filtros.tipo" class="form-select">
            <option value="">Todos</option>
            <option value="PALESTRA">Palestra</option>
            <option value="OFICINA">Oficina</option>
            <option value="APRESENTACAO">Apresentação</option>
            <option value="MESA_REDONDA">Mesa-redonda</option>
            <option value="OUTRO">Outro</option>
          </select>
        </div>
        <div class="col-md-5">
          <label for="programacao-de" class="form-label">A partir de</label>
          <input id="programacao-de" v-model="filtros.de" class="form-control" type="datetime-local" />
        </div>
        <div class="col-md-5">
          <label for="programacao-ate" class="form-label">Antes de</label>
          <input id="programacao-ate" v-model="filtros.ate" class="form-control" type="datetime-local" />
        </div>
        <div class="col-md-2 d-grid"><button class="btn btn-primary-custom" type="submit">Filtrar</button></div>
      </div>
    </form>
    <div v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</div>
    <p v-if="carregando" role="status">Carregando programação...</p>
    <p v-else-if="!atividades.length" class="text-muted">Nenhuma atividade corresponde aos filtros.</p>
    <div v-else class="vstack gap-3">
      <article v-for="atividade in atividades" :key="atividade.id" class="card border-0 shadow-sm">
        <div class="card-body p-4">
          <div class="d-flex flex-wrap justify-content-between gap-2 mb-2">
            <span class="small fw-bold text-primary-custom">{{ formatarData(atividade.dataInicio) }}</span>
            <span class="badge text-bg-primary-subtle">{{ atividade.tipo.replaceAll('_', ' ') }}</span>
          </div>
          <h3 class="h5 fw-bold">{{ atividade.titulo }}</h3>
          <p v-if="atividade.descricao" class="text-secondary">{{ atividade.descricao }}</p>
          <p class="small text-secondary mb-1">{{ atividade.espaco ?? atividade.local }}<span v-if="atividade.trilha"> · {{ atividade.trilha }}</span></p>
          <p v-if="atividade.capacidade !== null" class="small text-secondary mb-2">{{ Math.max(0, atividade.capacidade - atividade.reservas) }} vaga(s) na atividade</p>
          <p v-for="pessoa in atividade.pessoas" :key="`${pessoa.pessoaId}-${pessoa.papel}`" class="small mb-1">{{ pessoa.nome }} · {{ pessoa.papel.toLocaleLowerCase('pt-BR') }}</p>
          <div class="mt-3">
            <RouterLink v-if="!auth.autenticado" class="btn btn-sm btn-outline-primary" :to="{ name: 'login', query: { redirect: route.fullPath } }">Entre para montar sua agenda</RouterLink>
            <button v-else class="btn btn-sm btn-outline-primary" type="button" :disabled="!!ocupada" @click="alternarAgenda(atividade)">
              {{ selecionadas.includes(atividade.id) ? 'Remover da agenda' : 'Adicionar à agenda' }}
            </button>
          </div>
          <details v-if="auth.autenticado" class="mt-3" @toggle="alternarAvaliacao($event, atividade.id)">
            <summary class="text-primary-custom fw-semibold">Avaliar esta atividade</summary>
            <AvaliacaoEvento v-if="avaliacoesAbertas.includes(atividade.id)" :atividade-id="atividade.id" />
          </details>
        </div>
      </article>
    </div>
  </section>
</template>
