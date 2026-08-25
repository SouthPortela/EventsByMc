<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import type { PerfilUsuario } from '@/features/auth/types/perfil'
import { validarLogin, type ErrosLogin } from '@/features/auth/utils/validarLogin'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const senha = ref('')
const redirecionamentoInicial = typeof route.query.redirect === 'string' ? route.query.redirect : ''
const perfilInicial: Exclude<PerfilUsuario, 'VISITANTE'> = redirecionamentoInicial.startsWith(
  '/admin',
)
  ? 'ADMINISTRADOR'
  : redirecionamentoInicial.startsWith('/organizador')
    ? 'ORGANIZADOR'
    : 'PARTICIPANTE'
const perfilDemonstracao = ref<Exclude<PerfilUsuario, 'VISITANTE'>>(perfilInicial)
const erros = ref<ErrosLogin>({})
const mensagem = ref('')

function destinoPadrao(): string {
  if (perfilDemonstracao.value === 'ADMINISTRADOR') return '/admin'
  if (perfilDemonstracao.value === 'ORGANIZADOR') return '/organizador'
  return '/participante'
}

function entrar(): void {
  mensagem.value = ''
  erros.value = validarLogin(email.value, senha.value)

  if (Object.keys(erros.value).length === 0) {
    const redirecionamento = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const destinoSeguro =
      redirecionamento.startsWith('/') && !redirecionamento.startsWith('//')
        ? redirecionamento
        : destinoPadrao()

    auth.entrarComo(perfilDemonstracao.value)
    void router.push(destinoSeguro)
  }
}
</script>

<template>
  <div class="container py-5">
    <div class="row justify-content-center">
      <div class="col-lg-10">
        <div class="card border-0 shadow-sm overflow-hidden">
          <div class="row g-0">
            <div
              class="col-lg-5 bg-primary text-white p-4 p-lg-5 d-flex flex-column justify-content-center"
            >
              <span class="badge bg-white text-primary align-self-start mb-4">Área segura</span>
              <h1 class="display-6 fw-bold">Bem-vindo de volta.</h1>
              <p class="lead opacity-75 mb-0">
                Acompanhe inscrições, agenda, frequência e avaliações em uma experiência integrada.
              </p>
            </div>
            <div class="col-lg-7">
              <div class="card-body p-4 p-lg-5">
                <p class="text-primary fw-semibold small text-uppercase mb-2">
                  Acesso à plataforma
                </p>
                <h2 class="h3 fw-bold mb-2">Entre na sua conta</h2>
                <p class="text-muted mb-4">Use o e-mail informado no cadastro.</p>

                <div class="alert alert-primary border-0 small" role="note">
                  Enquanto a API de autenticação não está conectada, escolha abaixo qual perfil
                  deseja simular. No produto real, o backend devolverá o perfil autorizado.
                </div>

                <div v-if="mensagem" class="alert alert-info" role="status">{{ mensagem }}</div>

                <form novalidate @submit.prevent="entrar">
                  <div class="mb-3">
                    <label class="form-label fw-semibold" for="email">E-mail</label>
                    <input
                      id="email"
                      v-model="email"
                      class="form-control form-control-lg"
                      :class="{ 'is-invalid': erros.email }"
                      type="email"
                      autocomplete="email"
                      placeholder="voce@exemplo.com"
                    />
                    <div v-if="erros.email" class="invalid-feedback">{{ erros.email }}</div>
                  </div>

                  <div class="mb-4">
                    <label class="form-label fw-semibold" for="senha">Senha</label>
                    <input
                      id="senha"
                      v-model="senha"
                      class="form-control form-control-lg"
                      :class="{ 'is-invalid': erros.senha }"
                      type="password"
                      autocomplete="current-password"
                    />
                    <div v-if="erros.senha" class="invalid-feedback">{{ erros.senha }}</div>
                  </div>

                  <div class="mb-4">
                    <label class="form-label fw-semibold" for="perfil-demonstracao"
                      >Perfil de demonstração</label
                    >
                    <select
                      id="perfil-demonstracao"
                      v-model="perfilDemonstracao"
                      class="form-select form-select-lg"
                    >
                      <option value="PARTICIPANTE">Participante</option>
                      <option value="ORGANIZADOR">Organizador</option>
                      <option value="ADMINISTRADOR">Administrador</option>
                    </select>
                  </div>

                  <button class="btn btn-primary-custom btn-lg w-100" type="submit">Entrar</button>
                </form>

                <p class="text-center text-muted mt-4 mb-0">
                  Ainda não possui conta?
                  <RouterLink class="fw-semibold" to="/cadastro">Cadastre-se</RouterLink>
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
