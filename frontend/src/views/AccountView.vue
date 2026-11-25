<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { consultarMinhaConta, atualizarMinhaConta, type MinhaConta } from '@/features/events/services/contaService'
import { PERFIL_LABEL } from '@/features/auth/types/perfil'
import { ApiError } from '@/shared/services/httpClient'
import { useAuthStore } from '@/stores/auth'
const auth = useAuthStore()
const conta = ref<MinhaConta | null>(null)
const dados = reactive({ nome: '', email: '' })
const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')
const mensagem = ref('')
async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try { conta.value = await consultarMinhaConta(); Object.assign(dados, conta.value) }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar sua conta.' }
  finally { carregando.value = false }
}
async function salvar(): Promise<void> {
  if (salvando.value) return
  erro.value = ''; mensagem.value = ''; salvando.value = true
  try {
    conta.value = await atualizarMinhaConta(dados.nome.trim(), dados.email.trim())
    Object.assign(dados, conta.value)
    auth.atualizarDadosConta(conta.value)
    mensagem.value = 'Cadastro atualizado. Use o e-mail atualizado no próximo login.'
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível atualizar o cadastro.' }
  finally { salvando.value = false }
}
onMounted(carregar)
</script>
<template>
  <div class="container py-5">
    <h1 class="h2 fw-bold">Minha conta</h1>
    <p v-if="carregando" role="status">Carregando...</p>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }} <button v-if="!conta" class="btn btn-outline-primary" @click="carregar">Tentar novamente</button></p>
    <p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <form v-if="conta" class="card card-body shadow-sm" @submit.prevent="salvar">
      <fieldset :disabled="salvando || carregando">
        <label for="conta-nome" class="form-label">Nome</label>
        <input id="conta-nome" v-model="dados.nome" class="form-control mb-3" required maxlength="150" autocomplete="name" />
        <label for="conta-email" class="form-label">E-mail</label>
        <input id="conta-email" v-model="dados.email" class="form-control mb-3" required type="email" maxlength="255" autocomplete="email" />
        <p>Perfis: <span v-for="perfil in conta.perfis" :key="perfil" class="badge text-bg-primary me-2">{{ PERFIL_LABEL[perfil] }}</span></p>
        <button class="btn btn-primary-custom" type="submit">{{ salvando ? 'Salvando...' : 'Salvar alterações' }}</button>
      </fieldset>
    </form>
  </div>
</template>
