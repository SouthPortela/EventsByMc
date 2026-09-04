import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { maiorPerfil, type PerfilUsuario } from '@/features/auth/types/perfil'
import { login as loginNaApi, type LoginRespostaAPI } from '@/features/auth/services/authService'

const STORAGE_KEY = 'events-by-mc-auth-session'

interface SessaoPersistida {
  token: string
  usuarioId: string
  nome: string
  email: string
  perfis: PerfilUsuario[]
}

function carregarSessao(): SessaoPersistida | null {
  if (typeof window === 'undefined') return null
  const bruto = window.sessionStorage.getItem(STORAGE_KEY)
  if (!bruto) return null
  try {
    return JSON.parse(bruto) as SessaoPersistida
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const sessaoInicial = carregarSessao()

  const token = ref<string | null>(sessaoInicial?.token ?? null)
  const usuarioId = ref<string | null>(sessaoInicial?.usuarioId ?? null)
  const nome = ref<string | null>(sessaoInicial?.nome ?? null)
  const email = ref<string | null>(sessaoInicial?.email ?? null)
  const perfis = ref<PerfilUsuario[]>(sessaoInicial?.perfis ?? [])

  const perfil = computed<PerfilUsuario>(() => maiorPerfil(perfis.value))
  const autenticado = computed(() => token.value !== null)

  function persistir(): void {
    if (typeof window === 'undefined' || token.value === null) return
    window.sessionStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        token: token.value,
        usuarioId: usuarioId.value ?? '',
        nome: nome.value ?? '',
        email: email.value ?? '',
        perfis: perfis.value,
      }),
    )
  }

  async function entrarComApi(emailInformado: string, senha: string): Promise<void> {
    const resposta: LoginRespostaAPI = await loginNaApi(emailInformado, senha)
    token.value = resposta.token
    usuarioId.value = resposta.usuarioID
    nome.value = resposta.nome
    email.value = resposta.email
    perfis.value = resposta.perfis
    persistir()
  }

  function sair(): void {
    token.value = null
    usuarioId.value = null
    nome.value = null
    email.value = null
    perfis.value = []
    window.sessionStorage.removeItem(STORAGE_KEY)
  }

  return { token, usuarioId, nome, email, perfis, perfil, autenticado, entrarComApi, sair }
})
