import { afterEach, describe, expect, it, vi } from 'vitest'
import { configurarHttpClient } from '@/shared/services/httpClient'
import {
  consultarEventoAdministrativo, listarEventosAdministrativos, listarModeracoes,
  listarUsuariosAdministrativos, moderarEvento,
} from './administracaoService'

const id = 'b114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
afterEach(() => vi.unstubAllGlobals())

function servidor(resposta: unknown) {
  const requisicoes = vi.fn().mockImplementation(async () => Response.json(resposta))
  vi.stubGlobal('fetch', requisicoes)
  configurarHttpClient({ obterToken: () => 'jwt-admin', aoNaoAutorizado: vi.fn() })
  return requisicoes
}

describe('administração REST', () => {
  it('consulta dados globais somente com token', async () => {
    const requisicoes = servidor([])
    await listarEventosAdministrativos()
    await listarUsuariosAdministrativos()
    await listarModeracoes()
    expect(requisicoes.mock.calls.map((item) => item[0])).toEqual([
      '/api/admin/eventos', '/api/admin/usuarios', '/api/admin/moderacoes',
    ])
    expect(requisicoes.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer jwt-admin')
  })

  it('consulta evento por UUID e exige motivo para moderar', async () => {
    const requisicoes = servidor({ evento: { id } })
    await consultarEventoAdministrativo(id)
    await moderarEvento(id, 'suspensao', 'Conteúdo sob análise.')
    await moderarEvento(id, 'restauracao', 'Análise concluída.')
    await moderarEvento(id, 'exclusao', 'Conteúdo impróprio.')
    expect(requisicoes.mock.calls.map((item) => item[0])).toEqual([
      `/api/admin/eventos/${id}`,
      `/api/admin/eventos/${id}/suspensao`,
      `/api/admin/eventos/${id}/restauracao`,
      `/api/admin/eventos/${id}`,
    ])
    expect(requisicoes.mock.calls[1]?.[1].method).toBe('POST')
    expect(requisicoes.mock.calls[3]?.[1].method).toBe('DELETE')
    expect(JSON.parse(requisicoes.mock.calls[3]?.[1].body)).toEqual({ motivo: 'Conteúdo impróprio.' })
  })
})
