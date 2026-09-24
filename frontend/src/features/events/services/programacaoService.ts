import { apiRequest } from '@/shared/services/httpClient'

export interface PoliticaEvento {
  inscricoesAbertas: boolean
  inscricoesInicio: string | null
  inscricoesFim: string | null
  limiteInscritos: number | null
  permitirCancelamento: boolean
  frequenciaMinimaPercentual: number
}

export interface Trilha { id: string; nome: string }
export interface Espaco { id: string; nome: string; capacidade: number | null }
export interface PessoaEvento { id: string; nome: string; email: string | null }
export interface PapelAtividade { pessoaId: string; nome: string; papel: string }
export interface AtividadeProgramacao {
  id: string
  eventoId: string
  titulo: string
  descricao: string | null
  dataInicio: string
  dataFim: string
  local: string
  capacidade: number | null
  reservas: number
  trilhaId: string | null
  trilha: string | null
  espacoId: string | null
  espaco: string | null
  tipo: 'PALESTRA' | 'OFICINA' | 'APRESENTACAO' | 'MESA_REDONDA' | 'OUTRO'
  presencaObrigatoria: boolean
  politicaFrequencia: 'CHECKIN_UNICO' | 'VALIDACAO_MANUAL' | 'ENTRADA_SAIDA' | 'PERCENTUAL_PERMANENCIA'
  permanenciaMinimaPercentual: number
  pessoas: PapelAtividade[]
}
export interface FiltrosProgramacao {
  trilhaId: string
  espacoId: string
  tipo: string
  de: string
  ate: string
}
export interface ItemAgenda { atividade: AtividadeProgramacao; adicionadaEm: string }
export interface NovaAtividadeProgramacao {
  titulo: string
  descricao: string
  dataInicio: string
  dataFim: string
  local: string
  capacidade: number | null
  trilhaId: string | null
  espacoId: string | null
  tipo: AtividadeProgramacao['tipo']
  presencaObrigatoria: boolean
  politicaFrequencia: AtividadeProgramacao['politicaFrequencia']
  permanenciaMinimaPercentual: number
}

function recurso(eventoId: string, sufixo: string): string {
  return `/eventos/${encodeURIComponent(eventoId)}/${sufixo}`
}

function enviar<T>(caminho: string, method: 'POST' | 'PUT', dados: unknown): Promise<T> {
  return apiRequest<T>(caminho, {
    method,
    autenticada: true,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dados),
  })
}

export function listarProgramacao(
  eventoId: string,
  filtros?: Partial<FiltrosProgramacao>,
): Promise<AtividadeProgramacao[]> {
  const query = new URLSearchParams()
  if (filtros) {
    for (const [nome, valor] of Object.entries(filtros)) {
      if (valor) query.set(nome, valor)
    }
  }
  const sufixo = query.size ? `programacao?${query}` : 'programacao'
  return apiRequest(recurso(eventoId, sufixo))
}

export function listarProgramacaoGestao(eventoId: string): Promise<AtividadeProgramacao[]> {
  return apiRequest(recurso(eventoId, 'programacao/gestao'), { autenticada: true })
}

export function consultarPolitica(eventoId: string): Promise<PoliticaEvento> {
  return apiRequest(recurso(eventoId, 'politica'), { autenticada: true })
}
export function salvarPolitica(eventoId: string, dados: PoliticaEvento): Promise<PoliticaEvento> {
  return enviar(recurso(eventoId, 'politica'), 'PUT', dados)
}
export function listarTrilhas(eventoId: string): Promise<Trilha[]> {
  return apiRequest(recurso(eventoId, 'trilhas'), { autenticada: true })
}
export function criarTrilha(eventoId: string, nome: string): Promise<Trilha> {
  return enviar(recurso(eventoId, 'trilhas'), 'POST', { nome })
}
export function listarEspacos(eventoId: string): Promise<Espaco[]> {
  return apiRequest(recurso(eventoId, 'espacos'), { autenticada: true })
}
export function criarEspaco(eventoId: string, nome: string, capacidade: number | null): Promise<Espaco> {
  return enviar(recurso(eventoId, 'espacos'), 'POST', { nome, capacidade })
}
export function listarPessoas(eventoId: string): Promise<PessoaEvento[]> {
  return apiRequest(recurso(eventoId, 'pessoas'), { autenticada: true })
}
export function criarPessoa(eventoId: string, nome: string, email: string): Promise<PessoaEvento> {
  return enviar(recurso(eventoId, 'pessoas'), 'POST', { nome, email: email || null })
}
export function criarAtividadeProgramacao(
  eventoId: string,
  dados: NovaAtividadeProgramacao,
): Promise<AtividadeProgramacao> {
  return enviar(recurso(eventoId, 'programacao'), 'POST', dados)
}
export function vincularPessoa(
  eventoId: string,
  atividadeId: string,
  pessoaId: string,
  papel: string,
): Promise<void> {
  return enviar(recurso(eventoId, `atividades/${encodeURIComponent(atividadeId)}/pessoas`), 'POST', {
    pessoaId,
    papel,
  })
}
export function consultarAgenda(): Promise<ItemAgenda[]> {
  return apiRequest('/usuarios/me/agenda', { autenticada: true })
}
export function adicionarAgenda(atividadeId: string): Promise<ItemAgenda> {
  return apiRequest(`/atividades/${encodeURIComponent(atividadeId)}/agenda`, {
    method: 'POST',
    autenticada: true,
  })
}
export function removerAgenda(atividadeId: string): Promise<void> {
  return apiRequest(`/atividades/${encodeURIComponent(atividadeId)}/agenda`, {
    method: 'DELETE',
    autenticada: true,
  })
}
