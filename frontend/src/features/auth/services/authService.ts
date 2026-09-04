import { apiRequest } from '@/shared/services/httpClient'
import type { PerfilUsuario } from '../types/perfil'

// Espelha br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.LoginRespostaDTO
export interface LoginRespostaAPI {
  usuarioID: string
  nome: string
  email: string
  perfis: PerfilUsuario[]
  token: string
  tipoToken: string
}

export async function login(email: string, senha: string): Promise<LoginRespostaAPI> {
  return apiRequest<LoginRespostaAPI>('/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, senha }),
  })
}

export async function cadastrar(nome: string, email: string, senha: string): Promise<void> {
  await apiRequest<string>('/usuarios', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nome, email, senha }),
  })
}
