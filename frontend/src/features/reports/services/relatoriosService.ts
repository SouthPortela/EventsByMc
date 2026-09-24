import { apiDownload, apiRequest } from '@/shared/services/httpClient'

export interface Inscrito {
  usuarioId: string
  nome: string
  email: string
  estado: 'ATIVA' | 'CANCELADA'
  inscritaEm: string
}

export interface Frequencia {
  usuarioId: string
  nome: string
  email: string
  atividadesObrigatorias: number
  presencasValidas: number
  percentual: number
  minimoExigido: number
  situacao: 'EM_ANDAMENTO' | 'APROVADO' | 'INSUFICIENTE' | 'CANCELADA'
  elegivelCertificado: boolean
}

const base = (eventoId: string): string => `/eventos/${encodeURIComponent(eventoId)}/relatorios`

export function listarInscritos(eventoId: string): Promise<Inscrito[]> {
  return apiRequest(`${base(eventoId)}/inscritos`, { autenticada: true })
}

export function listarFrequencias(eventoId: string): Promise<Frequencia[]> {
  return apiRequest(`${base(eventoId)}/frequencia`, { autenticada: true })
}

export function minhaFrequencia(eventoId: string): Promise<Frequencia> {
  return apiRequest(`/eventos/${encodeURIComponent(eventoId)}/frequencia/me`, { autenticada: true })
}

export function baixarInscritos(eventoId: string): Promise<void> {
  return apiDownload(`${base(eventoId)}/inscritos.csv`, 'inscritos.csv')
}

export function baixarFrequencia(eventoId: string): Promise<void> {
  return apiDownload(`${base(eventoId)}/frequencia.csv`, 'frequencia.csv')
}

export function baixarInscritosPdf(eventoId: string): Promise<void> {
  return apiDownload(`${base(eventoId)}/inscritos.pdf`, 'inscritos.pdf')
}

export function baixarFrequenciaPdf(eventoId: string): Promise<void> {
  return apiDownload(`${base(eventoId)}/frequencia.pdf`, 'frequencia.pdf')
}
