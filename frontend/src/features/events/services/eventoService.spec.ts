import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  buscarEventoPorId,
  listarEventos,
  listarMeusEventos,
  criarEvento,
  alterarEstadoEvento,
  alterarCategoriaEvento,
} from './eventoService'
import { ApiError, configurarHttpClient } from '@/shared/services/httpClient'

const id = 'b114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
const resposta = {
  id,
  titulo: 'Simpósio',
  descricao: 'Descrição do simpósio',
  local: 'Auditório',
  dataInicio: '2026-10-10T09:00:00',
  dataFim: '2026-10-10T18:00:00',
  estado: 'PUBLICADO',
  categoria: 'ACADEMICO',
  atividades: [],
}
afterEach(() => vi.unstubAllGlobals())
function servidor(dados: unknown, status = 200) {
  const fetchMock = vi.fn().mockImplementation(async () => Response.json(dados, { status }))
  vi.stubGlobal('fetch', fetchMock)
  configurarHttpClient({ obterToken: () => 'token-de-teste', aoNaoAutorizado: vi.fn() })
  return fetchMock
}
describe('eventoService REST', () => {
  it('lista a categoria persistida sem inventar vagas', async () => {
    const fetchMock = servidor([resposta])
    const eventos = await listarEventos()
    expect(fetchMock).toHaveBeenCalledWith('/api/eventos', expect.any(Object))
    expect(eventos[0]).toMatchObject({ id, titulo: 'Simpósio' })
    expect(eventos[0]).not.toHaveProperty('vagas')
    expect(eventos[0]?.categoria).toBe('ACADEMICO')
  })
  it('consulta os detalhes usando UUID', async () => {
    const fetchMock = servidor(resposta)
    expect(await buscarEventoPorId(id)).toMatchObject({ id, atividades: [] })
    expect(fetchMock).toHaveBeenCalledWith('/api/eventos/' + id, expect.any(Object))
  })
  it('propaga 404 sem recorrer aos mocks', async () => {
    servidor({ mensagem: 'Não encontrado' }, 404)
    await expect(buscarEventoPorId(id)).rejects.toBeInstanceOf(ApiError)
  })
  it('autentica a lista do organizador', async () => {
    const fetchMock = servidor([resposta])
    await listarMeusEventos()
    expect(fetchMock.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer token-de-teste')
    expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/usuarios/me/eventos')
  })
  it('envia somente o contrato da criação', async () => {
    const fetchMock = servidor({ ...resposta, estado: 'RASCUNHO' }, 201)
    const dados = {
      titulo: resposta.titulo,
      descricao: resposta.descricao,
      local: resposta.local,
      dataInicio: resposta.dataInicio,
      dataFim: resposta.dataFim,
      categoria: 'ACADEMICO' as const,
    }
    expect((await criarEvento(dados)).estado).toBe('RASCUNHO')
    expect(fetchMock.mock.calls[0]?.[1].method).toBe('POST')
    expect(JSON.parse(fetchMock.mock.calls[0]?.[1].body)).toEqual(dados)
  })
  it('altera a categoria pelo endpoint protegido', async () => {
    const fetchMock = servidor({ ...resposta, categoria: 'TECNOLOGIA' })
    expect((await alterarCategoriaEvento(id, 'TECNOLOGIA')).categoria).toBe('TECNOLOGIA')
    expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/eventos/' + id + '/categoria')
    expect(fetchMock.mock.calls[0]?.[1].method).toBe('PATCH')
    expect(JSON.parse(fetchMock.mock.calls[0]?.[1].body)).toEqual({ categoria: 'TECNOLOGIA' })
  })
  it('publica e encerra pela rota protegida', async () => {
    const fetchMock = servidor(resposta)
    await alterarEstadoEvento(id, 'publicacao')
    await alterarEstadoEvento(id, 'encerramento')
    expect(fetchMock.mock.calls.map((c) => c[0])).toEqual([
      '/api/eventos/' + id + '/publicacao',
      '/api/eventos/' + id + '/encerramento',
    ])
  })
})
