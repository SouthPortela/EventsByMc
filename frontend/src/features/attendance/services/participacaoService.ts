import { apiRequest } from '@/shared/services/httpClient'

export interface InscricaoResumo {
  id: string
  eventoId: string
  eventoTitulo: string
  eventoInicio: string | null
  eventoFim: string | null
  eventoLocal: string | null
  eventoEstado: 'RASCUNHO' | 'PUBLICADO' | 'ENCERRADO' | 'SUSPENSO' | 'EXCLUIDO'
  estado: 'ATIVA' | 'CANCELADA'
  criadaEm: string
}

export interface AtividadeAgenda {
  id: string
  eventoId: string
  eventoTitulo: string
  titulo: string
  dataInicio: string | null
  dataFim: string | null
  local: string | null
  presencaRegistradaEm: string | null
}

export interface Participacao {
  inscricoes: InscricaoResumo[]
  atividades: AtividadeAgenda[]
  totalPresencas: number
}

export function consultarParticipacao(): Promise<Participacao> {
  return apiRequest<Participacao>('/usuarios/me/participacao', { autenticada: true })
}
