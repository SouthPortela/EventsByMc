import type { CategoriaEvento } from './categoria'

export interface EventoResumo {
  id: string
  titulo: string
  local: string
  vagas?: number
  dataInicio: string | null
  dataFim?: string | null
  estado?: 'RASCUNHO' | 'PUBLICADO' | 'ENCERRADO' | 'SUSPENSO' | 'EXCLUIDO'
  categoria: CategoriaEvento
  modalidade?: 'PRESENCIAL' | 'ONLINE' | 'HIBRIDO'
  banner?: MidiaReferencia
  preco?: string
  gratuito?: boolean
  destaque?: boolean
}

export interface MidiaReferencia {
  id: string
  url: string
  textoAlternativo: string
}

export interface AtividadeResumo {
  id: string
  titulo: string
  horario: string
  local: string
}

export interface PessoaDestaque {
  id: string
  nome: string
  papel: string
  foto?: MidiaReferencia
}

export interface EventoDetalhe extends EventoResumo {
  descricao: string
  endereco: string
  atividades: AtividadeResumo[]
  pessoas?: PessoaDestaque[]
}
