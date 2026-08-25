<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import AppIcon, { type IconName } from '@/components/icons/AppIcon.vue'
import { PERFIL_LABEL } from '@/features/auth/types/perfil'
import { useAuthStore } from '@/stores/auth'

interface NavItem {
  label: string
  to: string
  icon: IconName
}

const auth = useAuthStore()
const itens = computed<NavItem[]>(() => {
  if (auth.perfil === 'ADMINISTRADOR')
    return [
      { label: 'Visão geral', to: '/admin', icon: 'dashboard' },
      { label: 'Usuários', to: '/admin/usuarios', icon: 'users' },
      { label: 'Eventos', to: '/admin/eventos', icon: 'calendar' },
      { label: 'Auditoria', to: '/admin/auditoria', icon: 'shield' },
    ]
  if (auth.perfil === 'ORGANIZADOR')
    return [
      { label: 'Visão geral', to: '/organizador', icon: 'dashboard' },
      { label: 'Criar evento', to: '/organizador/eventos/novo', icon: 'plus' },
      { label: 'Inscrições', to: '/organizador/inscricoes', icon: 'users' },
      { label: 'Frequência', to: '/organizador/frequencia', icon: 'check' },
      { label: 'Relatórios', to: '/organizador/relatorios', icon: 'report' },
    ]
  return [
    { label: 'Visão geral', to: '/participante', icon: 'dashboard' },
    { label: 'Minhas inscrições', to: '/participante/inscricoes', icon: 'ticket' },
    { label: 'Minha agenda', to: '/participante/agenda', icon: 'calendar' },
    { label: 'Minha conta', to: '/participante/conta', icon: 'user' },
  ]
})
</script>

<template>
  <div class="container-fluid">
    <div class="row">
      <aside class="col-lg-3 col-xl-2 dashboard-sidebar p-3 p-lg-4">
        <div class="d-flex align-items-center gap-3 pb-4 mb-3 border-bottom">
          <span class="avatar-circle"><AppIcon name="user" :size="18" /></span>
          <div>
            <span class="small text-muted d-block">Perfil atual</span
            ><strong class="text-primary-custom">{{ PERFIL_LABEL[auth.perfil] }}</strong>
          </div>
        </div>
        <nav
          class="d-flex flex-row flex-lg-column gap-2 overflow-auto"
          aria-label="Navegação do painel"
        >
          <RouterLink
            v-for="item in itens"
            :key="item.to"
            class="dashboard-nav-link flex-shrink-0"
            :to="item.to"
          >
            <AppIcon :name="item.icon" :size="18" /> {{ item.label }}
          </RouterLink>
        </nav>
      </aside>
      <div class="col-lg-9 col-xl-10 bg-light min-vh-100"><RouterView /></div>
    </div>
  </div>
</template>
