import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
import { configurarHttpClient } from '@/shared/services/httpClient'
import { atualizarMinhaConta } from '@/features/events/services/contaService'
import { editarEvento, consultarEventoGestao, consultarHistoricoEvento } from '@/features/events/services/eventoService'
import { ajustarFrequencia, listarAjustes } from '@/features/attendance/services/frequenciaService'
import { listarRelatorioAtividades } from '@/features/reports/services/relatoriosService'
const id = 'b114d7b7-a7b6-4209-b6c8-24c4fbb27b00'
beforeEach(() => configurarHttpClient({ obterToken: () => 'jwt-teste', aoNaoAutorizado: vi.fn() }))
afterEach(() => vi.unstubAllGlobals())
describe('edição e retificação autenticadas', () => {
  it('edita a própria conta sem enviar perfis nem credenciais', async () => {
    const fetch = vi.fn().mockResolvedValue(Response.json({ nome: 'Nome', email: 'novo@example.test', perfis: ['PARTICIPANTE'] }))
    vi.stubGlobal('fetch', fetch)
    await atualizarMinhaConta('Nome', 'novo@example.test')
    expect(fetch.mock.calls[0]?.[0]).toBe('/api/usuarios/me')
    expect(fetch.mock.calls[0]?.[1].headers.get('Authorization')).toBe('Bearer jwt-teste')
    expect(JSON.parse(fetch.mock.calls[0]?.[1].body)).toEqual({ nome: 'Nome', email: 'novo@example.test' })
  })
  it('carrega rascunhos por rota privada e envia edição por PUT', async () => {
    const fetch = vi.fn().mockImplementation(async () => Response.json({ id, titulo: 'Evento', categoria: 'OUTROS', atividades: [] }))
    vi.stubGlobal('fetch', fetch)
    await consultarEventoGestao(id)
    await editarEvento(id, { titulo: 'Evento', descricao: 'Descrição suficientemente longa', local: 'Sala', dataInicio: '2026-11-25T09:00', dataFim: '2026-11-25T10:00', categoria: 'OUTROS' })
    expect(fetch.mock.calls.map(c => c[0])).toEqual(['/api/eventos/' + id + '/gestao', '/api/eventos/' + id])
    expect(fetch.mock.calls[1]?.[1].method).toBe('PUT')
  })
  it('reutiliza o identificador do ajuste em uma repetição da solicitação', async () => {
    const fetch = vi.fn().mockImplementation(async () => Response.json({ id }))
    vi.stubGlobal('fetch', fetch)
    const ajuste = { id, usuarioId: id, confirmada: false, motivo: 'Correção justificada' }
    await ajustarFrequencia(id, ajuste)
    await ajustarFrequencia(id, ajuste)
    expect(JSON.parse(fetch.mock.calls[0]?.[1].body)).toEqual(ajuste)
    expect(fetch.mock.calls[0]?.[1].body).toBe(fetch.mock.calls[1]?.[1].body)
    expect(fetch.mock.calls[1]?.[1].headers.get('Authorization')).toBe('Bearer jwt-teste')
  })
  it('consulta relatórios e históricos por rotas autenticadas', async () => {
    const fetch = vi.fn().mockImplementation(async () => Response.json([]))
    vi.stubGlobal('fetch', fetch)
    await listarRelatorioAtividades(id); await listarAjustes(id); await consultarHistoricoEvento(id)
    for (const c of fetch.mock.calls) expect(c[1].headers.get('Authorization')).toBe('Bearer jwt-teste')
    expect(fetch.mock.calls.map(c => c[0])).toEqual(['/api/eventos/' + id + '/relatorios/atividades', '/api/atividades/' + id + '/frequencia/ajustes', '/api/eventos/' + id + '/historico'])
  })
  it('propaga bloqueio de histórico e duplicidade de e-mail sem simular sucesso', async () => {
    vi.stubGlobal('fetch', vi.fn().mockImplementation(async () => Response.json({ mensagem: 'Operação recusada' }, { status: 409 })))
    await expect(atualizarMinhaConta('Nome', 'usado@example.test')).rejects.toMatchObject({ status: 409 })
    await expect(ajustarFrequencia(id, { id, usuarioId: id, confirmada: true, motivo: 'Justificativa' })).rejects.toMatchObject({ status: 409 })
  })
})
