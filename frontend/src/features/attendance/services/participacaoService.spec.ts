import { afterEach, describe, expect, it, vi } from 'vitest'
import { configurarHttpClient } from '@/shared/services/httpClient'
import { consultarParticipacao } from './participacaoService'

afterEach(() => vi.unstubAllGlobals())

describe('consulta da participação', () => {
  it('usa o token e a rota da própria conta', async () => {
    configurarHttpClient({ obterToken: () => 'token-teste', aoNaoAutorizado: () => {} })
    const resposta = { inscricoes: [], atividades: [], totalPresencas: 0 }
    const mock = vi.fn().mockResolvedValue(Response.json(resposta))
    vi.stubGlobal('fetch', mock)

    expect(await consultarParticipacao()).toEqual(resposta)
    expect(mock.mock.calls[0]?.[0]).toBe('/api/usuarios/me/participacao')
    expect(mock.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer token-teste')
  })
})
