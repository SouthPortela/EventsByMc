import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  codigoValido,
  linkPresenca,
  normalizarCodigo,
} from '../features/attendance/utils/codigoPresenca'
import {
  confirmarPresenca,
  criarAtividade,
  gerarChamada,
  inscrever,
  listarAtividades,
} from '../features/attendance/services/presencaService'
import { configurarHttpClient } from '../shared/services/httpClient'
import QRCode from 'qrcode'

afterEach(() => vi.unstubAllGlobals())
describe('presença e QR', () => {
  it('normaliza a digitação sem aceitar letras ambíguas ou código incompleto', () => {
    expect(normalizarCodigo('abcd-efgh-jkmn')).toBe('ABCDEFGHJKMN')
    expect(codigoValido('abcd efgh jkmn')).toBe(true)
    expect(codigoValido('IIIIIIIIIIII')).toBe(false)
    expect(codigoValido('123')).toBe(false)
  })
  it('coloca código no fragmento, nunca na query ou caminho', () => {
    const link = new URL(linkPresenca('https://exemplo.test/app/', 'ABCDEFGHJKMN'))
    expect(link.pathname).toBe('/app/presenca')
    expect(link.search).toBe('')
    expect(link.hash).toBe('#codigo=ABCDEFGHJKMN')
    expect(() => linkPresenca('javascript:alert(1)', 'ABCDEFGHJKMN')).toThrow()
    expect(() => linkPresenca('https://nome:senha@exemplo.test', 'ABCDEFGHJKMN')).toThrow()
  })
  it('gera uma matriz QR real com margem de correção', () => {
    const qr = QRCode.create(linkPresenca('https://exemplo.test/', 'ABCDEFGHJKMN'), {
      errorCorrectionLevel: 'M',
    })
    expect(qr.modules.size).toBeGreaterThan(20)
    expect(qr.modules.data.some((bit) => bit === 1)).toBe(true)
  })
  it('todas as chamadas de escrita usam JWT; confirmação envia código no corpo', async () => {
    const mock = vi
      .fn()
      .mockImplementation(async () => Response.json({ id: 'id', estado: 'ATIVA' }))
    vi.stubGlobal('fetch', mock)
    configurarHttpClient({ obterToken: () => 'jwt-teste', aoNaoAutorizado: vi.fn() })
    await inscrever('evento')
    await gerarChamada('atividade')
    await confirmarPresenca('ABCDEFGHJKMN', 'CODIGO')
    await criarAtividade('evento', {
      titulo: 'Palestra',
      descricao: '',
      local: 'Sala',
      dataInicio: '2026-10-10T09:00',
      dataFim: '2026-10-10T10:00',
    })
    await listarAtividades('evento')
    expect(mock.mock.calls.map((c) => c[0])).toEqual([
      '/api/eventos/evento/inscricoes',
      '/api/atividades/atividade/chamadas',
      '/api/presencas/confirmacoes',
      '/api/eventos/evento/atividades',
      '/api/eventos/evento/atividades',
    ])
    for (const chamada of mock.mock.calls)
      expect(chamada[1].headers.get('Authorization')).toBe('Bearer jwt-teste')
    expect(JSON.parse(mock.mock.calls[2]?.[1].body)).toEqual({
      codigo: 'ABCDEFGHJKMN',
      origem: 'CODIGO',
    })
  })
  it('propaga expiração e limite de tentativas sem inventar sucesso', async () => {
    configurarHttpClient({ obterToken: () => 'jwt-teste', aoNaoAutorizado: vi.fn() })
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(Response.json({ mensagem: 'Código expirado' }, { status: 400 })),
    )
    await expect(confirmarPresenca('ABCDEFGHJKMN', 'QR')).rejects.toMatchObject({ status: 400 })
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(Response.json({ mensagem: 'Aguarde' }, { status: 429 })),
    )
    await expect(confirmarPresenca('ABCDEFGHJKMN', 'QR')).rejects.toMatchObject({ status: 429 })
  })
})
