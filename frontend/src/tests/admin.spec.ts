import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { configurarHttpClient } from '@/shared/services/httpClient'
import { alterarPerfis, bloquearEvento, desbloquearEvento, listarEventosAdmin, listarContas, consultarResumo, listarAuditoria } from '@/features/admin/services/adminService'
import { listarEventosParaGestao, listarMeusEventos } from '@/features/events/services/eventoService'
import { useAuthStore } from '@/stores/auth'

const id = 'b114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
beforeEach(() => {
  setActivePinia(createPinia())
  configurarHttpClient({ obterToken: () => 'jwt-teste', aoNaoAutorizado: vi.fn() })
})
afterEach(() => vi.unstubAllGlobals())
describe('administração integrada', () => {
  it('autentica todas as consultas administrativas sem retornar mocks', async () => {
    const mock = vi.fn().mockImplementation(async () => Response.json([]))
    vi.stubGlobal('fetch', mock)
    await listarEventosAdmin(); await listarContas(); await consultarResumo(); await listarAuditoria()
    expect(mock.mock.calls.map(c => c[0])).toEqual(['/api/admin/eventos', '/api/admin/usuarios', '/api/admin/resumo', '/api/admin/auditoria'])
    for (const chamada of mock.mock.calls) expect(chamada[1].headers.get('Authorization')).toBe('Bearer jwt-teste')
  })
  it('usa UUID, envia o motivo no corpo e mantém perfis múltiplos', async () => {
    const mock = vi.fn().mockImplementation(async () => Response.json({}))
    vi.stubGlobal('fetch', mock)
    await bloquearEvento(id, '  Corrigir o local  ')
    await desbloquearEvento(id)
    await alterarPerfis(id, ['PARTICIPANTE', 'ORGANIZADOR'])
    expect(mock.mock.calls.map(c => c[0])).toEqual([`/api/admin/eventos/${id}/bloqueio`, `/api/admin/eventos/${id}/bloqueio`, `/api/admin/usuarios/${id}/perfis`])
    expect(JSON.parse(mock.mock.calls[0]?.[1].body)).toEqual({ motivo: 'Corrigir o local' })
    expect(mock.mock.calls[1]?.[1].method).toBe('DELETE')
    expect(JSON.parse(mock.mock.calls[2]?.[1].body)).toEqual({ perfis: ['PARTICIPANTE', 'ORGANIZADOR'] })
  })
  it('administrador seleciona eventos globais e organizador continua vendo os próprios', async () => {
    const evento = { id, titulo: 'Evento bloqueado', estado: 'BLOQUEADO', motivoBloqueio: 'Corrigir local', atividades: [] }
    const mock = vi.fn().mockResolvedValueOnce(Response.json([{ evento }])).mockResolvedValueOnce(Response.json([evento]))
    vi.stubGlobal('fetch', mock)
    useAuthStore().perfis = ['ADMINISTRADOR']
    expect((await listarEventosParaGestao())[0]?.motivoBloqueio).toBe('Corrigir local')
    expect((await listarMeusEventos())[0]?.estado).toBe('BLOQUEADO')
    expect(mock.mock.calls.map(c => c[0])).toEqual(['/api/admin/eventos', '/api/usuarios/me/eventos'])
  })
  it('propaga acesso negado em vez de simular dados', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(Response.json({ mensagem: 'Acesso negado' }, { status: 403 })))
    await expect(listarContas()).rejects.toMatchObject({ status: 403 })
  })
})
