import { apiRequest } from '@/shared/services/httpClient'

export interface Questao {
  id: string
  ordem: number
  enunciado: string
  tipo: 'TEXTO' | 'ESCOLHA_UNICA' | 'ESCALA'
  opcoes: string[] | null
  escalaMinima: number | null
  escalaMaxima: number | null
  obrigatoria: boolean
}
export interface Questionario {
  id: string
  eventoId: string
  atividadeId: string | null
  titulo: string
  ativo: boolean
  questoes: Questao[]
}
export interface NovaQuestao {
  enunciado: string
  tipo: Questao['tipo']
  opcoes: string[] | null
  escalaMinima: number | null
  escalaMaxima: number | null
  obrigatoria: boolean
}
export interface Resultados {
  totalRespostas: number
  questoes: { questaoId: string; enunciado: string; tipo: Questao['tipo']; distribuicao: Record<string, number>; textos: string[] }[]
}
const base = (id: string): string => `/eventos/${encodeURIComponent(id)}`
const post = <T>(url: string, corpo?: unknown): Promise<T> => apiRequest(url, {
  method: 'POST', autenticada: true,
  ...(corpo === undefined ? {} : { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(corpo) }),
})

export const consultarQuestionario = (id: string): Promise<Questionario> =>
  apiRequest(`${base(id)}/questionario`, { autenticada: true })
export const consultarQuestionarioGestao = (id: string): Promise<Questionario> =>
  apiRequest(`${base(id)}/questionario/gestao`, { autenticada: true })
export const criarQuestionario = (id: string, titulo: string): Promise<Questionario> =>
  post(`${base(id)}/questionario`, { titulo })
export const adicionarQuestao = (id: string, questao: NovaQuestao): Promise<Questao> =>
  post(`${base(id)}/questionario/questoes`, questao)
export const publicarQuestionario = (id: string): Promise<Questionario> =>
  post(`${base(id)}/questionario/publicacao`)
export const responderQuestionario = (id: string, respostas: { questaoId: string; valor: string }[]): Promise<{ id: string }> =>
  post(`${base(id)}/avaliacoes`, { respostas })
export const jaRespondeu = (id: string): Promise<{ respondeu: boolean }> =>
  apiRequest(`${base(id)}/avaliacoes/me`, { autenticada: true })
export const consultarResultados = (id: string): Promise<Resultados> =>
  apiRequest(`${base(id)}/avaliacoes/resultados`, { autenticada: true })

const atividade = (id: string): string => `/atividades/${encodeURIComponent(id)}`
export const consultarQuestionarioAtividade = (id: string): Promise<Questionario> =>
  apiRequest(`${atividade(id)}/questionario`, { autenticada: true })
export const consultarQuestionarioAtividadeGestao = (id: string): Promise<Questionario> =>
  apiRequest(`${atividade(id)}/questionario/gestao`, { autenticada: true })
export const criarQuestionarioAtividade = (id: string, titulo: string): Promise<Questionario> =>
  post(`${atividade(id)}/questionario`, { titulo })
export const adicionarQuestaoAtividade = (id: string, questao: NovaQuestao): Promise<Questao> =>
  post(`${atividade(id)}/questionario/questoes`, questao)
export const publicarQuestionarioAtividade = (id: string): Promise<Questionario> =>
  post(`${atividade(id)}/questionario/publicacao`)
export const responderQuestionarioAtividade = (id: string, respostas: { questaoId: string; valor: string }[]): Promise<{ id: string }> =>
  post(`${atividade(id)}/avaliacoes`, { respostas })
export const jaRespondeuAtividade = (id: string): Promise<{ respondeu: boolean }> =>
  apiRequest(`${atividade(id)}/avaliacoes/me`, { autenticada: true })
export const consultarResultadosAtividade = (id: string): Promise<Resultados> =>
  apiRequest(`${atividade(id)}/avaliacoes/resultados`, { autenticada: true })
