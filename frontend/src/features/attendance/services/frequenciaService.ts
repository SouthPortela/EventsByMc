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

export interface AjusteFrequencia {
  id: string; atividadeId: string; usuarioId: string; confirmada: boolean
  motivo: string; registradaEm: string; responsavelId: string
}
export interface NovoAjusteFrequencia { id: string; usuarioId: string; confirmada: boolean; motivo: string }
export function listarAjustes(atividadeId: string): Promise<AjusteFrequencia[]> {
  return apiRequest('/atividades/' + encodeURIComponent(atividadeId) + '/frequencia/ajustes', { autenticada: true })
}
export function ajustarFrequencia(atividadeId: string, dados: NovoAjusteFrequencia): Promise<AjusteFrequencia> {
  return apiRequest('/atividades/' + encodeURIComponent(atividadeId) + '/frequencia/ajustes', {
    method: 'POST', autenticada: true, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dados),
  })
}
