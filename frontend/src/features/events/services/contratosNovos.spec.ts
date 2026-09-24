import { afterEach, describe, expect, it, vi } from 'vitest'
import { configurarHttpClient } from '@/shared/services/httpClient'
import { listarProgramacao, adicionarAgenda } from './programacaoService'
import { registrarFrequencia } from '@/features/attendance/services/frequenciaService'
import { responderQuestionarioAtividade } from '@/features/evaluations/services/avaliacaoService'
import { listarFrequencias } from '@/features/reports/services/relatoriosService'
import { publicarMensagem } from './interacaoService'

const evento = 'b114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
const atividade = 'a114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
afterEach(() => vi.unstubAllGlobals())
function servidor(resposta: unknown, status = 200) {
  const fetchMock = vi.fn().mockImplementation(async () => Response.json(resposta, { status }))
  vi.stubGlobal('fetch', fetchMock)
  configurarHttpClient({ obterToken: () => 'jwt-falso', aoNaoAutorizado: vi.fn() })
  return fetchMock
}

describe('contratos REST dos requisitos funcionais', () => {
  it('combina filtros de programação sem enviar campos vazios', async () => {
    const fetchMock = servidor([])
    await listarProgramacao(evento, { trilhaId: 'trilha', tipo: 'OFICINA', espacoId: '', de: '', ate: '' })
    expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/eventos/${evento}/programacao?trilhaId=trilha&tipo=OFICINA`)
  })
  it('seleção da agenda envia JWT e não altera a atividade', async () => {
    const fetchMock = servidor({ atividade: { id: atividade }, adicionadaEm: '2026-10-10T12:00:00Z' }, 201)
    await adicionarAgenda(atividade)
    expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/atividades/${atividade}/agenda`)
    expect(fetchMock.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer jwt-falso')
  })
  it('marcação manual envia participante e tipo para a atividade', async () => {
    const fetchMock = servidor({ id: 'registro' }, 201)
    await registrarFrequencia(atividade, evento, 'ENTRADA')
    expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/atividades/${atividade}/frequencia`)
    expect(JSON.parse(fetchMock.mock.calls[0]?.[1].body)).toEqual({ usuarioId: evento, marcacao: 'ENTRADA' })
  })
  it('avaliação fica vinculada à atividade e não ao evento inteiro', async () => {
    const fetchMock = servidor({ id: 'avaliacao' }, 201)
    await responderQuestionarioAtividade(atividade, [{ questaoId: evento, valor: '5' }])
    expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/atividades/${atividade}/avaliacoes`)
  })
  it('relatório e comunidade usam rotas autenticadas separadas', async () => {
    const fetchMock = servidor([])
    await listarFrequencias(evento)
    expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/eventos/${evento}/relatorios/frequencia`)
    await publicarMensagem(evento, 'Olá')
    expect(fetchMock.mock.calls[1]?.[0]).toBe(`/api/eventos/${evento}/mensagens`)
    expect(JSON.parse(fetchMock.mock.calls[1]?.[1].body)).toEqual({ mensagem: 'Olá' })
  })
})
