export interface EventoResumo {
  id: number
  titulo: string
  local: string
  vagas: number
  dataInicio: string
  categoria: string
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
  id: number
  titulo: string
  horario: string
  local: string
}

export interface PessoaDestaque {
  id: number
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
