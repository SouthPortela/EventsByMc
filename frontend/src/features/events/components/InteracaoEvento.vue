<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listarMensagens, publicarMensagem } from '../services/interacaoService'
import type { MensagemEvento } from '../services/interacaoService'
import { ApiError } from '@/shared/services/httpClient'
import { useAuthStore } from '@/stores/auth'

const props = defineProps<{ eventoId: string }>()
const auth = useAuthStore()
const mensagens = ref<MensagemEvento[]>([])
const texto = ref('')
const erro = ref('')
const carregando = ref(false)
const enviando = ref(false)

async function carregar(): Promise<void> {
  if (!auth.autenticado) return
  carregando.value = true
  erro.value = ''
  try { mensagens.value = await listarMensagens(props.eventoId) }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar mensagens.' }
  finally { carregando.value = false }
}

async function enviar(): Promise<void> {
  const mensagem = texto.value.trim()
  if (!mensagem || enviando.value) return
  enviando.value = true
  erro.value = ''
  try {
    const criada = await publicarMensagem(props.eventoId, mensagem)
    mensagens.value = [criada, ...mensagens.value].slice(0, 50)
    texto.value = ''
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível enviar a mensagem.' }
  finally { enviando.value = false }
}
onMounted(carregar)
</script>

<template>
  <section class="card border-0 shadow-sm mb-4">
    <div class="card-body p-4">
      <h2 class="h4 fw-bold">Comunidade do evento</h2>
      <p class="text-muted small">Converse com outros participantes inscritos.</p>
      <p v-if="!auth.autenticado" class="text-muted">Entre na sua conta e inscreva-se para participar.</p>
      <template v-else>
        <form @submit.prevent="enviar">
          <label class="form-label" for="nova-mensagem">Sua mensagem</label>
          <textarea id="nova-mensagem" v-model="texto" class="form-control" rows="3" maxlength="2000" />
          <button class="btn btn-primary-custom mt-2" type="submit" :disabled="enviando || !texto.trim()">Publicar</button>
        </form>
        <p v-if="erro" class="alert alert-warning mt-3" role="alert">{{ erro }}</p>
        <p v-if="carregando" role="status">Carregando mensagens...</p>
        <div v-for="item in mensagens" :key="item.id" class="border-top py-3 mt-3">
          <div class="d-flex justify-content-between"><strong>{{ item.autor }}</strong><small class="text-muted">{{ new Date(item.criadaEm).toLocaleString('pt-BR') }}</small></div>
          <p class="mb-0 text-break" style="white-space: pre-wrap">{{ item.mensagem }}</p>
        </div>
        <p v-if="!carregando && !mensagens.length && !erro" class="text-muted mt-3">Nenhuma mensagem ainda.</p>
      </template>
    </div>
  </section>
</template>
