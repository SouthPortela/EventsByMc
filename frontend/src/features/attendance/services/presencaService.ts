import { apiRequest } from '@/shared/services/httpClient'
export interface AtividadePresenca {
  id: string
  titulo: string
  dataInicio: string | null
  dataFim: string | null
  local: string | null
}
export interface NovaAtividade {
  titulo: string
  descricao: string
  dataInicio: string
  dataFim: string
  local: string
}
export interface ChamadaGerada {
  id: string
  atividade: string
  codigo: string
  expiraEm: string
}
export interface Confirmacao {
  id: string
  atividade: string
  registradaEm: string
  jaRegistrada: boolean
}
function post<T>(path: string, dados?: unknown): Promise<T> {
  return apiRequest<T>(path, {
    method: 'POST',
    autenticada: true,
    ...(dados === undefined
      ? {}
      : { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(dados) }),
  })
}
export function inscrever(eventoId: string): Promise<{ id: string; estado: string }> {
  return post(`/eventos/${encodeURIComponent(eventoId)}/inscricoes`)
}
export function listarAtividades(eventoId: string): Promise<AtividadePresenca[]> {
  return apiRequest(`/eventos/${encodeURIComponent(eventoId)}/atividades`, { autenticada: true })
}
export function criarAtividade(eventoId: string, dados: NovaAtividade): Promise<AtividadePresenca> {
  return post(`/eventos/${encodeURIComponent(eventoId)}/atividades`, dados)
}
export function gerarChamada(atividadeId: string): Promise<ChamadaGerada> {
  return post(`/atividades/${encodeURIComponent(atividadeId)}/chamadas`)
}
export function confirmarPresenca(codigo: string, origem: 'QR' | 'CODIGO'): Promise<Confirmacao> {
  return post('/presencas/confirmacoes', { codigo, origem })
}
