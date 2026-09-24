<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { consultarQuestionario, consultarQuestionarioAtividade, jaRespondeu, jaRespondeuAtividade, responderQuestionario, responderQuestionarioAtividade } from '../services/avaliacaoService'
import type { Questionario } from '../services/avaliacaoService'
import { ApiError } from '@/shared/services/httpClient'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ eventoId?: string; atividadeId?: string }>()
const auth = useAuthStore()
const questionario = ref<Questionario | null>(null)
const respostas = reactive<Record<string, string>>({})
const respondido = ref(false)
const erro = ref('')
const carregando = ref(false)
const enviando = ref(false)

async function carregar(): Promise<void> {
  if (!auth.autenticado) return
  const id = props.atividadeId ?? props.eventoId
  if (!id) return
  carregando.value = true
  try {
    questionario.value = props.atividadeId ? await consultarQuestionarioAtividade(id) : await consultarQuestionario(id)
    respondido.value = (props.atividadeId ? await jaRespondeuAtividade(id) : await jaRespondeu(id)).respondeu
  } catch (e) {
    if (!(e instanceof ApiError && e.status === 404)) erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar a avaliação.'
  } finally { carregando.value = false }
}
async function enviar(): Promise<void> {
  if (!questionario.value || enviando.value) return
  const id = props.atividadeId ?? props.eventoId
  if (!id) return
  enviando.value = true
  erro.value = ''
  try {
    const respostasValidas = questionario.value.questoes
      .filter((questao) => respostas[questao.id]?.trim())
      .map((questao) => ({ questaoId: questao.id, valor: respostas[questao.id]!.trim() }))
    if (props.atividadeId) await responderQuestionarioAtividade(id, respostasValidas)
    else await responderQuestionario(id, respostasValidas)
    respondido.value = true
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível enviar a avaliação.' }
  finally { enviando.value = false }
}
onMounted(carregar)
</script>

<template>
  <section v-if="auth.autenticado && (questionario || erro || carregando)" class="card border-0 shadow-sm mb-4">
    <div class="card-body p-4">
      <p v-if="carregando" role="status">Carregando avaliação...</p>
      <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
      <template v-if="questionario">
        <h2 class="h4 fw-bold">{{ questionario.titulo }}</h2>
        <p class="text-muted small">É necessário ter inscrição ativa e presença confirmada para responder.</p>
        <p v-if="respondido" class="alert alert-success" role="status">Sua avaliação já foi registrada. Obrigado!</p>
        <form v-else @submit.prevent="enviar">
          <div v-for="questao in questionario.questoes" :key="questao.id" class="mb-3">
            <label class="form-label" :for="`questao-${questao.id}`">{{ questao.ordem }}. {{ questao.enunciado }}</label>
            <textarea v-if="questao.tipo === 'TEXTO'" :id="`questao-${questao.id}`" v-model="respostas[questao.id]" class="form-control" maxlength="2000" :required="questao.obrigatoria" />
            <select v-else :id="`questao-${questao.id}`" v-model="respostas[questao.id]" class="form-select" :required="questao.obrigatoria">
              <option value="">Selecione</option>
              <option v-for="opcao in questao.tipo === 'ESCOLHA_UNICA' ? questao.opcoes ?? [] : Array.from({ length: (questao.escalaMaxima ?? 0) - (questao.escalaMinima ?? 0) + 1 }, (_, n) => String(n + (questao.escalaMinima ?? 0)))" :key="opcao" :value="opcao">{{ opcao }}</option>
            </select>
          </div>
          <button class="btn btn-primary-custom" type="submit" :disabled="enviando">Enviar avaliação</button>
        </form>
      </template>
    </div>
  </section>
</template>
