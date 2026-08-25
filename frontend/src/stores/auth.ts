import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { PERFIS, type PerfilUsuario } from '@/features/auth/types/perfil'

const STORAGE_KEY = 'events-by-mc-demo-profile'

function carregarPerfil(): PerfilUsuario {
  if (typeof window === 'undefined') return 'VISITANTE'
  const salvo = window.sessionStorage.getItem(STORAGE_KEY)
  return PERFIS.includes(salvo as PerfilUsuario) ? (salvo as PerfilUsuario) : 'VISITANTE'
}

export const useAuthStore = defineStore('auth', () => {
  const perfil = ref<PerfilUsuario>(carregarPerfil())
  const autenticado = computed(() => perfil.value !== 'VISITANTE')

  function entrarComo(novoPerfil: PerfilUsuario): void {
    perfil.value = novoPerfil
    window.sessionStorage.setItem(STORAGE_KEY, novoPerfil)
  }

  function sair(): void {
    perfil.value = 'VISITANTE'
    window.sessionStorage.removeItem(STORAGE_KEY)
  }

  return { perfil, autenticado, entrarComo, sair }
})
