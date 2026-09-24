import { apiRequest } from '@/shared/services/httpClient'

export interface RegistroFrequencia {
  id: string
  atividadeId: string
  usuarioId: string
  marcacao: 'CONFIRMACAO' | 'ENTRADA' | 'SAIDA'
  registradaEm: string
  registradaPor: string
}
export function listarRegistros(atividadeId: string): Promise<RegistroFrequencia[]> {
  return apiRequest(`/atividades/${encodeURIComponent(atividadeId)}/frequencia`, { autenticada: true })
}
export function registrarFrequencia(atividadeId: string, usuarioId: string,
  marcacao: RegistroFrequencia['marcacao']): Promise<RegistroFrequencia> {
  return apiRequest(`/atividades/${encodeURIComponent(atividadeId)}/frequencia`, {
    method: 'POST', autenticada: true,
    headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ usuarioId, marcacao }),
  })
}
