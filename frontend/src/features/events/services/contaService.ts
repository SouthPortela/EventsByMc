import { apiRequest } from '@/shared/services/httpClient'
import type { PerfilUsuario } from '@/features/auth/types/perfil'

export interface MinhaConta {
  usuarioId: string
  nome: string
  email: string
  perfis: PerfilUsuario[]
}

export function consultarMinhaConta(): Promise<MinhaConta> {
  return apiRequest<MinhaConta>('/usuarios/me', {
    method: 'GET',
    autenticada: true,
  })
}

export function atualizarMinhaConta(nome: string, email: string): Promise<MinhaConta> {
  return apiRequest('/usuarios/me', { method: 'PUT', autenticada: true,
    headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nome, email }) })
}
