import { apiRequest } from '@/shared/services/httpClient'
import type { EventoDetalhe, EventoResumo } from '../types/evento'
import type { CategoriaEvento } from '../types/categoria'
import { formatarData } from '../utils/formatarData'

export interface DadosCriacaoEvento {
  titulo: string
  descricao: string
  local: string
  dataInicio: string
  dataFim: string
  categoria: CategoriaEvento
}

interface EventoResposta {
  id: string
  titulo: string
  descricao: string | null
  local: string | null
  dataInicio: string | null
  dataFim: string | null
  estado: 'RASCUNHO' | 'PUBLICADO' | 'ENCERRADO' | 'SUSPENSO' | 'EXCLUIDO'
  categoria: CategoriaEvento
  atividades: {
    id: string
    titulo: string
    local: string | null
    dataInicio: string | null
    dataFim: string | null
  }[]
}

function resumo(evento: EventoResposta): EventoResumo {
  return {
    id: evento.id,
    titulo: evento.titulo,
    local: evento.local ?? 'Local a divulgar',
    dataInicio: evento.dataInicio,
    dataFim: evento.dataFim,
    estado: evento.estado,
    categoria: evento.categoria,
  }
}

export async function alterarCategoriaEvento(
  id: string,
  categoria: CategoriaEvento,
): Promise<EventoResumo> {
  return resumo(
    await apiRequest<EventoResposta>(`/eventos/${encodeURIComponent(id)}/categoria`, {
      method: 'PATCH',
      autenticada: true,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ categoria }),
    }),
  )
}

export async function listarEventos(): Promise<EventoResumo[]> {
  return (await apiRequest<EventoResposta[]>('/eventos')).map(resumo)
}

export async function buscarEventoPorId(id: string): Promise<EventoDetalhe> {
  const evento = await apiRequest<EventoResposta>(`/eventos/${encodeURIComponent(id)}`)
  return {
    ...resumo(evento),
    descricao: evento.descricao ?? '',
    endereco: '',
    atividades: evento.atividades.map((atividade) => ({
      id: atividade.id,
      titulo: atividade.titulo,
      horario: formatarData(atividade.dataInicio),
      local: atividade.local ?? 'Local a divulgar',
    })),
  }
}

export async function listarMeusEventos(): Promise<EventoResumo[]> {
  return (await apiRequest<EventoResposta[]>('/usuarios/me/eventos', { autenticada: true })).map(
    resumo,
  )
}

export async function criarEvento(dados: DadosCriacaoEvento): Promise<EventoResumo> {
  return resumo(
    await apiRequest<EventoResposta>('/eventos', {
      method: 'POST',
      autenticada: true,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(dados),
    }),
  )
}

export async function alterarEstadoEvento(
  id: string,
  acao: 'publicacao' | 'encerramento',
): Promise<EventoResumo> {
  return resumo(
    await apiRequest<EventoResposta>(`/eventos/${encodeURIComponent(id)}/${acao}`, {
      method: 'POST',
      autenticada: true,
    }),
  )
}
