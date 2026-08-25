<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import LogoMark from '@/components/brand/LogoMark.vue'
import AppIcon from '@/components/icons/AppIcon.vue'
import { PERFIL_LABEL } from '@/features/auth/types/perfil'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const busca = ref('')
const menuAberto = ref(false)

const painelInicial = computed(() => {
  if (auth.perfil === 'ADMINISTRADOR') return '/admin'
  if (auth.perfil === 'ORGANIZADOR') return '/organizador'
  return '/participante'
})

function pesquisar(): void {
  const termo = busca.value.trim()
  void router.push({ name: 'home', query: termo ? { busca: termo } : {} })
  menuAberto.value = false
}

function sair(): void {
  auth.sair()
  void router.push('/')
}

watch(
  () => route.query.busca,
  (termo) => {
    busca.value = typeof termo === 'string' ? termo : ''
  },
  { immediate: true },
)
</script>

<template>
  <header class="navbar-custom">
    <div class="container h-100">
      <div class="row h-100 align-items-center justify-content-between">
        <div class="col-auto d-flex align-items-center">
          <RouterLink class="d-flex align-items-center gap-3 text-decoration-none" to="/">
            <LogoMark />
            <span class="d-flex flex-column">
              <span class="navbar-brand-text mb-0 lh-1">
                Events<span class="text-secondary fw-medium">ByMc</span>
              </span>
              <span class="navbar-brand-subtitle d-none d-sm-block"
                >Plataforma de Gestão de Eventos</span
              >
            </span>
          </RouterLink>
        </div>

        <div class="col d-none d-lg-block px-4">
          <form
            class="input-group search-input-group w-100"
            role="search"
            @submit.prevent="pesquisar"
          >
            <span class="input-group-text border-end-0"><AppIcon name="search" :size="18" /></span>
            <input
              v-model="busca"
              type="search"
              class="form-control border-start-0 border-end-0 ps-0"
              placeholder="Busque por eventos, shows, cursos..."
            />
            <span class="input-group-text border-start-0 border-end-0">
              <AppIcon name="map-pin" :size="16" />
              <span class="small text-muted text-truncate ms-1">Qualquer local</span>
            </span>
            <button class="btn btn-search px-4" type="submit">Buscar</button>
          </form>
        </div>

        <div v-if="!auth.autenticado" class="col-auto d-none d-md-flex align-items-center gap-4">
          <RouterLink
            class="text-decoration-none nav-link-custom small"
            to="/login?redirect=/organizador"
            >Crie seu evento</RouterLink
          >
          <div class="d-flex align-items-center gap-3 border-start ps-3">
            <RouterLink class="text-decoration-none nav-link-custom small" to="/login"
              >Acesse sua conta</RouterLink
            >
            <RouterLink
              class="btn btn-outline-primary-custom btn-sm d-flex align-items-center gap-2 px-3 py-2"
              to="/cadastro"
            >
              <AppIcon name="user" :size="16" /> Cadastre-se
            </RouterLink>
          </div>
        </div>

        <div v-else class="col-auto d-none d-md-flex align-items-center gap-3">
          <RouterLink class="nav-link-custom text-decoration-none small" :to="painelInicial"
            >Meu painel</RouterLink
          >
          <div class="dropdown">
            <button
              class="btn btn-outline-primary-custom btn-sm dropdown-toggle d-flex align-items-center gap-2"
              type="button"
              data-bs-toggle="dropdown"
            >
              <span class="avatar-circle"><AppIcon name="user" :size="16" /></span>
              {{ PERFIL_LABEL[auth.perfil] }}
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0 mt-2">
              <li>
                <RouterLink class="dropdown-item" :to="painelInicial">Acessar painel</RouterLink>
              </li>
              <li><hr class="dropdown-divider" /></li>
              <li>
                <button class="dropdown-item text-danger" type="button" @click="sair">Sair</button>
              </li>
            </ul>
          </div>
        </div>

        <div class="col-auto d-md-none">
          <button
            class="btn btn-link text-muted p-0"
            type="button"
            aria-label="Abrir menu"
            @click="menuAberto = !menuAberto"
          >
            <AppIcon :name="menuAberto ? 'x' : 'menu'" :size="24" />
          </button>
        </div>
      </div>
    </div>

    <div
      v-show="menuAberto"
      class="d-md-none bg-white border-top shadow-sm position-absolute w-100"
    >
      <div class="p-3">
        <form class="input-group mb-3" role="search" @submit.prevent="pesquisar">
          <span class="input-group-text bg-light border-end-0"
            ><AppIcon name="search" :size="18"
          /></span>
          <input
            v-model="busca"
            type="search"
            class="form-control border-start-0 bg-light"
            placeholder="Buscar eventos..."
          />
        </form>
        <div class="d-flex flex-column gap-2">
          <template v-if="!auth.autenticado">
            <RouterLink
              class="text-dark text-decoration-none py-2 px-2 rounded fw-medium"
              to="/login?redirect=/organizador"
              >Crie seu evento</RouterLink
            >
            <RouterLink
              class="text-dark text-decoration-none py-2 px-2 rounded fw-medium"
              to="/login"
              >Acesse sua conta</RouterLink
            >
            <RouterLink
              class="text-primary-custom text-decoration-none py-2 px-2 rounded fw-bold"
              to="/cadastro"
              >Cadastre-se</RouterLink
            >
          </template>
          <template v-else>
            <RouterLink
              class="text-primary-custom text-decoration-none py-2 px-2 rounded fw-bold"
              :to="painelInicial"
              >Meu painel</RouterLink
            >
            <button
              class="btn btn-link text-danger text-start text-decoration-none px-2"
              type="button"
              @click="sair"
            >
              Sair
            </button>
          </template>
        </div>
      </div>
    </div>
  </header>
</template>
