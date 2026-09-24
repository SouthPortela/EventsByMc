<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { adicionarQuestao, adicionarQuestaoAtividade, consultarQuestionarioGestao, consultarQuestionarioAtividadeGestao, consultarResultados, consultarResultadosAtividade, criarQuestionario, criarQuestionarioAtividade, publicarQuestionario, publicarQuestionarioAtividade } from '@/features/evaluations/services/avaliacaoService'
import type { NovaQuestao, Questionario, Resultados } from '@/features/evaluations/services/avaliacaoService'
import { ApiError } from '@/shared/services/httpClient'

const route = useRoute()
const eventoId = String(route.params.id ?? '')
const atividadeId = String(route.params.atividadeId ?? '')
const questionario = ref<Questionario | null>(null)
const resultados = ref<Resultados | null>(null)
const titulo = ref('Avaliação do evento')
const opcoesTexto = ref('')
const questao = reactive<NovaQuestao>({ enunciado: '', tipo: 'TEXTO', opcoes: null, escalaMinima: null, escalaMaxima: null, obrigatoria: true })
const erro = ref('')
const mensagem = ref('')
const ocupado = ref(false)

async function carregar(): Promise<void> {
  try {
    questionario.value = atividadeId ? await consultarQuestionarioAtividadeGestao(atividadeId) : await consultarQuestionarioGestao(eventoId)
    if (questionario.value.ativo) resultados.value = atividadeId ? await consultarResultadosAtividade(atividadeId) : await consultarResultados(eventoId)
  } catch (e) { if (!(e instanceof ApiError && e.status === 404)) erro.value = e instanceof ApiError ? e.message : 'Falha ao carregar questionário.' }
}

async function executar(acao: () => Promise<unknown>, sucesso: string): Promise<void> {
  if (ocupado.value) return
  ocupado.value = true
  erro.value = ''
  mensagem.value = ''
  try { await acao(); mensagem.value = sucesso; await carregar() }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível salvar.' }
  finally { ocupado.value = false }
}

function criar(): Promise<void> {
  return executar(() => atividadeId ? criarQuestionarioAtividade(atividadeId, titulo.value) : criarQuestionario(eventoId, titulo.value), 'Questionário criado.')
}
function adicionar(): Promise<void> {
  const nova: NovaQuestao = {
    enunciado: questao.enunciado,
    tipo: questao.tipo,
    opcoes: questao.tipo === 'ESCOLHA_UNICA' ? opcoesTexto.value.split('\n').map(o => o.trim()).filter(Boolean) : null,
    escalaMinima: questao.tipo === 'ESCALA' ? questao.escalaMinima : null,
    escalaMaxima: questao.tipo === 'ESCALA' ? questao.escalaMaxima : null,
    obrigatoria: questao.obrigatoria,
  }
  return executar(async () => { await (atividadeId ? adicionarQuestaoAtividade(atividadeId, nova) : adicionarQuestao(eventoId, nova)); questao.enunciado = ''; opcoesTexto.value = '' }, 'Questão adicionada.')
}
function publicar(): Promise<void> {
  return executar(() => atividadeId ? publicarQuestionarioAtividade(atividadeId) : publicarQuestionario(eventoId), 'Questionário publicado.')
}
onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <h1 class="h2 fw-bold">{{ atividadeId ? 'Avaliação da atividade' : 'Avaliação do evento' }}</h1>
    <p class="text-muted">Monte as perguntas antes de publicar. Após a publicação, as questões ficam imutáveis para preservar as respostas.</p>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <form v-if="!questionario" class="card card-body mb-4" @submit.prevent="criar">
      <label class="form-label" for="titulo-questionario">Título do questionário</label>
      <input id="titulo-questionario" v-model="titulo" class="form-control" required maxlength="160" />
      <button class="btn btn-primary-custom mt-3 align-self-start" :disabled="ocupado">Criar questionário</button>
    </form>
    <template v-else>
      <h2 class="h4">{{ questionario.titulo }}</h2>
      <p>{{ questionario.ativo ? 'Publicado' : 'Rascunho' }}</p>
      <ol class="list-group list-group-numbered mb-4">
        <li v-for="item in questionario.questoes" :key="item.id" class="list-group-item">{{ item.enunciado }} · {{ item.tipo }}</li>
      </ol>
      <form v-if="!questionario.ativo" class="card card-body mb-4" @submit.prevent="adicionar">
        <h3 class="h5">Nova questão</h3>
        <label class="form-label" for="enunciado">Enunciado</label>
        <input id="enunciado" v-model="questao.enunciado" class="form-control mb-3" required maxlength="500" />
        <label class="form-label" for="tipo-questao">Tipo</label>
        <select id="tipo-questao" v-model="questao.tipo" class="form-select mb-3"><option value="TEXTO">Texto</option><option value="ESCOLHA_UNICA">Escolha única</option><option value="ESCALA">Escala numérica</option></select>
        <template v-if="questao.tipo === 'ESCOLHA_UNICA'"><label class="form-label" for="opcoes">Opções, uma por linha</label><textarea id="opcoes" v-model="opcoesTexto" class="form-control mb-3" rows="4" required /></template>
        <div v-if="questao.tipo === 'ESCALA'" class="row mb-3"><div class="col"><label class="form-label" for="minima">Mínima</label><input id="minima" v-model.number="questao.escalaMinima" type="number" min="0" max="9" class="form-control" required /></div><div class="col"><label class="form-label" for="maxima">Máxima</label><input id="maxima" v-model.number="questao.escalaMaxima" type="number" min="1" max="10" class="form-control" required /></div></div>
        <label class="form-check mb-3"><input v-model="questao.obrigatoria" type="checkbox" class="form-check-input" /><span class="form-check-label">Obrigatória</span></label>
        <button class="btn btn-outline-primary align-self-start" :disabled="ocupado">Adicionar questão</button>
      </form>
      <button v-if="!questionario.ativo" class="btn btn-primary-custom mb-4" :disabled="ocupado || !questionario.questoes.length" @click="publicar">Publicar questionário</button>
      <section v-if="resultados" class="card card-body">
        <h3 class="h5">Resultados · {{ resultados.totalRespostas }} resposta(s)</h3>
        <div v-for="item in resultados.questoes" :key="item.questaoId" class="border-top py-3">
          <strong>{{ item.enunciado }}</strong>
          <ul v-if="item.tipo !== 'TEXTO'"><li v-for="(total, valor) in item.distribuicao" :key="valor">{{ valor }}: {{ total }}</li></ul>
          <ul v-else><li v-for="(texto, indice) in item.textos" :key="indice" class="text-break">{{ texto }}</li></ul>
        </div>
      </section>
    </template>
  </div>
</template>
