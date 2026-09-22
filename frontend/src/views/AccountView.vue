<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { consultarMinhaConta, type MinhaConta } from '@/features/events/services/contaService'
import { PERFIL_LABEL } from '@/features/auth/types/perfil'
import { ApiError } from '@/shared/services/httpClient'

const conta = ref<MinhaConta | null>(null)
const carregando = ref(false)
const erro = ref('')

async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    conta.value = await consultarMinhaConta()
  } catch (e) {
    erro.value =
      e instanceof ApiError ? e.message : 'Não foi possível carregar sua conta. Tente novamente.'
  } finally {
    carregando.value = false
  }
}
onMounted(carregar)
</script>

<template>
  <div class="container py-5">
    <h1 class="h2 fw-bold">Minha conta</h1>
    <p class="text-muted">Dados cadastrados na plataforma.</p>
    <p v-if="carregando" role="status">Carregando sua conta...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button class="btn btn-outline-primary ms-3" @click="carregar">Tentar novamente</button>
    </div>
    <section v-else-if="conta" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        <dl class="row mb-0">
          <dt class="col-sm-3">Nome</dt>
          <dd class="col-sm-9">{{ conta.nome }}</dd>
          <dt class="col-sm-3">E-mail</dt>
          <dd class="col-sm-9">{{ conta.email }}</dd>
          <dt class="col-sm-3">Perfis</dt>
          <dd class="col-sm-9">
            <span v-for="perfil in conta.perfis" :key="perfil" class="badge text-bg-primary me-2">
              {{ PERFIL_LABEL[perfil] }}
            </span>
          </dd>
        </dl>
        <p class="small text-muted mt-4 mb-0">
          A edição dos dados e o envio de foto ainda não estão disponíveis.
        </p>
      </div>
    </section>
  </div>
</template>
