<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import {
  validarCadastro,
  type DadosCadastro,
  type ErrosCadastro,
} from '@/features/auth/utils/validarCadastro'
import { cadastrar as cadastrarNaApi } from '@/features/auth/services/authService'
import { ApiError } from '@/shared/services/httpClient'

const router = useRouter()
const dados = reactive<DadosCadastro>({
  nome: '',
  email: '',
  senha: '',
  confirmacaoSenha: '',
  aceitouTermos: false,
})
const erros = ref<ErrosCadastro>({})
const mensagem = ref('')
const enviando = ref(false)

async function cadastrar(): Promise<void> {
  mensagem.value = ''
  erros.value = validarCadastro(dados)
  if (Object.keys(erros.value).length > 0) return

  enviando.value = true
  try {
    await cadastrarNaApi(dados.nome, dados.email, dados.senha)
    void router.push({ name: 'login', query: { cadastrado: '1' } })
  } catch (erro) {
    mensagem.value =
      erro instanceof ApiError ? erro.message : 'Não foi possível concluir o cadastro. Tente novamente.'
  } finally {
    enviando.value = false
  }
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-lg-7">
        <div class="text-center mb-4">
          <span class="badge rounded-pill text-bg-primary-subtle text-primary-custom mb-3"
            >Novo participante</span
          >
          <h1 class="h2 fw-bold">Crie sua conta</h1>
          <p class="text-muted">Uma conta para inscrições, agenda, frequência e avaliações.</p>
        </div>

        <div class="card border-0 shadow-sm">
          <div class="card-body p-4 p-lg-5">
            <div v-if="mensagem" class="alert alert-danger" role="status">{{ mensagem }}</div>

            <form novalidate @submit.prevent="cadastrar">
              <div class="mb-3">
                <label class="form-label fw-semibold" for="cadastro-nome">Nome completo</label>
                <input
                  id="cadastro-nome"
                  v-model="dados.nome"
                  class="form-control form-control-lg"
                  :class="{ 'is-invalid': erros.nome }"
                  autocomplete="name"
                />
                <div v-if="erros.nome" class="invalid-feedback">{{ erros.nome }}</div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-semibold" for="cadastro-email">E-mail</label>
                <input
                  id="cadastro-email"
                  v-model="dados.email"
                  class="form-control form-control-lg"
                  :class="{ 'is-invalid': erros.email }"
                  type="email"
                  autocomplete="email"
                />
                <div v-if="erros.email" class="invalid-feedback">{{ erros.email }}</div>
              </div>

              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold" for="cadastro-senha">Senha</label>
                  <input
                    id="cadastro-senha"
                    v-model="dados.senha"
                    class="form-control form-control-lg"
                    :class="{ 'is-invalid': erros.senha }"
                    type="password"
                    autocomplete="new-password"
                  />
                  <div v-if="erros.senha" class="invalid-feedback">{{ erros.senha }}</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold" for="cadastro-confirmacao"
                    >Confirmar senha</label
                  >
                  <input
                    id="cadastro-confirmacao"
                    v-model="dados.confirmacaoSenha"
                    class="form-control form-control-lg"
                    :class="{ 'is-invalid': erros.confirmacaoSenha }"
                    type="password"
                    autocomplete="new-password"
                  />
                  <div v-if="erros.confirmacaoSenha" class="invalid-feedback">
                    {{ erros.confirmacaoSenha }}
                  </div>
                </div>
              </div>

              <div class="form-check mt-4">
                <input
                  id="cadastro-termos"
                  v-model="dados.aceitouTermos"
                  class="form-check-input"
                  :class="{ 'is-invalid': erros.aceitouTermos }"
                  type="checkbox"
                />
                <label class="form-check-label" for="cadastro-termos">
                  Concordo com os termos de uso e com a finalidade dos dados informados.
                </label>
                <div v-if="erros.aceitouTermos" class="invalid-feedback">
                  {{ erros.aceitouTermos }}
                </div>
              </div>

              <button class="btn btn-primary-custom btn-lg w-100 mt-4" type="submit" :disabled="enviando">
                {{ enviando ? 'Criando conta…' : 'Criar conta' }}
              </button>
            </form>

            <p class="text-center text-muted mt-4 mb-0">
              Já possui conta? <RouterLink class="fw-semibold" to="/login">Entre agora</RouterLink>
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
