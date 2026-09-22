import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { apiRequest, configurarHttpClient } from '../shared/services/httpClient'
import { consultarMinhaConta } from '../features/events/services/contaService'
import { cadastrar } from '../features/auth/services/authService'

const sair = vi.fn()
let token: string | null
beforeEach(() => {
  token = 'token-teste'
  sair.mockClear()
  configurarHttpClient({ obterToken: () => token, aoNaoAutorizado: sair })
})
afterEach(() => vi.unstubAllGlobals())
function responder(resposta: Response) {
  const mock = vi.fn().mockResolvedValue(resposta)
  vi.stubGlobal('fetch', mock)
  return mock
}
describe('cliente HTTP autenticado', () => {
  it('não envia token nas consultas públicas', async () => {
    const mock = responder(Response.json([]))
    await apiRequest('/eventos')
    expect(mock.mock.calls[0]?.[1].headers.has('Authorization')).toBe(false)
  })
  it('consulta a conta com Bearer', async () => {
    const conta = {
      usuarioId: 'id',
      nome: 'Teste',
      email: 'teste@example.test',
      perfis: ['PARTICIPANTE'],
    }
    const mock = responder(Response.json(conta))
    expect(await consultarMinhaConta()).toEqual(conta)
    expect(mock.mock.calls[0]?.[0]).toBe('/api/usuarios/me')
    expect(mock.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer token-teste')
  })
  it('bloqueia antes do envio quando não há token', async () => {
    token = null
    const mock = responder(Response.json({}))
    await expect(apiRequest('/usuarios/me', { autenticada: true })).rejects.toMatchObject({
      status: 401,
    })
    expect(mock).not.toHaveBeenCalled()
    expect(sair).toHaveBeenCalledOnce()
  })
  it('encerra sessão em 401 protegido, mas não em 403', async () => {
    responder(Response.json({}, { status: 401 }))
    await expect(apiRequest('/usuarios/me', { autenticada: true })).rejects.toMatchObject({
      status: 401,
    })
    expect(sair).toHaveBeenCalledOnce()
    sair.mockClear()
    responder(Response.json({}, { status: 403 }))
    await expect(apiRequest('/eventos', { autenticada: true })).rejects.toMatchObject({
      status: 403,
    })
    expect(sair).not.toHaveBeenCalled()
  })
  it('falha de login não encerra outra sessão', async () => {
    responder(Response.json({}, { status: 401 }))
    await expect(apiRequest('/auth/login', { method: 'POST' })).rejects.toMatchObject({
      status: 401,
    })
    expect(sair).not.toHaveBeenCalled()
  })
  it('401 antigo não encerra uma sessão renovada', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockImplementation(async () => {
        token = 'novo-token'
        return Response.json({}, { status: 401 })
      }),
    )
    await expect(apiRequest('/usuarios/me', { autenticada: true })).rejects.toMatchObject({
      status: 401,
    })
    expect(sair).not.toHaveBeenCalled()
  })
  it('preserva cadastro text/plain e aceita resposta vazia', async () => {
    responder(new Response('Cadastro realizado', { headers: { 'Content-Type': 'text/plain' } }))
    expect(await apiRequest('/usuarios', { method: 'POST' })).toBe('Cadastro realizado')
    responder(new Response(null, { status: 204 }))
    expect(await apiRequest('/teste')).toBeUndefined()
  })
  it('envia o perfil de organizador escolhido no cadastro', async () => {
    const mock = responder(
      new Response('Cadastro realizado', { headers: { 'Content-Type': 'text/plain' } }),
    )
    await cadastrar('Maria Silva', 'maria@example.test', 'segura123', 'ORGANIZADOR')
    expect(mock.mock.calls[0]?.[0]).toBe('/api/usuarios')
    expect(JSON.parse(mock.mock.calls[0]?.[1].body)).toEqual({
      nome: 'Maria Silva',
      email: 'maria@example.test',
      senha: 'segura123',
      perfil: 'ORGANIZADOR',
    })
    expect(mock.mock.calls[0]?.[1].headers.has('Authorization')).toBe(false)
  })
  it('não trata página HTML como dados da API', async () => {
    responder(new Response('<html></html>', { headers: { 'Content-Type': 'text/html' } }))
    await expect(apiRequest('/eventos')).rejects.toThrow('Resposta inesperada')
  })
})
