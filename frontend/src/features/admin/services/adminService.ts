import { apiDownload, apiRequest } from '@/shared/services/httpClient'
import type { PerfilUsuario } from '@/features/auth/types/perfil'
import type { EventoResumo } from '@/features/events/types/evento'

export type PerfilConta = Exclude<PerfilUsuario, 'VISITANTE'>
export interface ContaAdmin { id: string; nome: string; email: string; perfis: PerfilConta[] }
export interface EventoAdmin { evento: EventoResumo; organizadorId: string; organizadorNome: string }
export interface ResumoAdmin {
  usuarios: number; organizadores: number; eventosPublicados: number; eventosBloqueados: number; registrosPresenca: number
}
export interface RegistroAdmin {
  id: string; responsavelId: string; acao: string; recursoId: string; detalhe: string; registradoEm: string
}
export const consultarResumo = (): Promise<ResumoAdmin> => apiRequest('/admin/resumo', { autenticada: true })
export const listarContas = (): Promise<ContaAdmin[]> => apiRequest('/admin/usuarios', { autenticada: true })
export const listarEventosAdmin = (): Promise<EventoAdmin[]> => apiRequest('/admin/eventos', { autenticada: true })
export const listarAuditoria = (): Promise<RegistroAdmin[]> => apiRequest('/admin/auditoria', { autenticada: true })
export const exportarAuditoria = (): Promise<void> => apiDownload('/admin/auditoria.csv', 'auditoria.csv')
export function alterarPerfis(id: string, perfis: PerfilConta[]): Promise<ContaAdmin> {
  return apiRequest(`/admin/usuarios/${encodeURIComponent(id)}/perfis`, {
    method: 'PUT', autenticada: true, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ perfis }),
  })
}
export function bloquearEvento(id: string, motivo: string): Promise<EventoAdmin> {
  return apiRequest(`/admin/eventos/${encodeURIComponent(id)}/bloqueio`, {
    method: 'POST', autenticada: true, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ motivo: motivo.trim() }),
  })
}
export function desbloquearEvento(id: string): Promise<EventoAdmin> {
  return apiRequest(`/admin/eventos/${encodeURIComponent(id)}/bloqueio`, { method: 'DELETE', autenticada: true })
}
