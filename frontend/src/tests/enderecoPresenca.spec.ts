import { describe, expect, it, vi } from 'vitest'
import { resolverEnderecoPresenca, validarEnderecoPresenca } from '../features/attendance/services/enderecoPresenca'
import { linkPresenca } from '../features/attendance/utils/codigoPresenca'

const local = { pagina: 'http://localhost:8081/organizador/frequencia', caminhoBase: '/' }
const resposta = (endereco: unknown) => Response.json({ endereco })

describe('endereço do QR para o celular', () => {
  it('troca localhost pelo IP do host e preserva porta, caminho e código no fragmento', async () => {
    const buscar = vi.fn<typeof fetch>().mockResolvedValue(resposta('192.168.1.4'))
    const base = await resolverEnderecoPresenca({ ...local, configurado: 'http://127.0.0.1:8081/eventos/' }, buscar)
    expect(base).toBe('http://192.168.1.4:8081/eventos/')
    expect(linkPresenca(base, 'ABCDEFGHJKMN')).toBe('http://192.168.1.4:8081/eventos/presenca#codigo=ABCDEFGHJKMN')
    expect(buscar.mock.calls[0]?.[0].toString()).toBe('http://localhost:8081/presenca-rede.json')
    expect(buscar.mock.calls[0]?.[1]).toMatchObject({ cache: 'no-store', credentials: 'omit', redirect: 'error' })
    expect(buscar.mock.calls[0]?.[1]).not.toHaveProperty('headers')
  })

  it('consulta novamente o IP e suporta DHCP sem reconstruir o aplicativo', async () => {
    const buscar = vi.fn<typeof fetch>().mockResolvedValueOnce(resposta('192.168.1.4')).mockResolvedValueOnce(resposta('10.0.0.8'))
    expect(await resolverEnderecoPresenca(local, buscar)).toBe('http://192.168.1.4:8081/')
    expect(await resolverEnderecoPresenca(local, buscar)).toBe('http://10.0.0.8:8081/')
  })

  it('no Electron usa a descoberta local mas aponta para o site HTTP, preservando sua porta', async () => {
    const buscar = vi.fn<typeof fetch>().mockResolvedValue(resposta('172.20.1.2'))
    expect(await resolverEnderecoPresenca({ configurado: 'http://127.0.0.1:8081/',
      pagina: 'eventsbymc://app/organizador/frequencia', caminhoBase: '/' }, buscar)).toBe('http://172.20.1.2:8081/')
    expect(buscar.mock.calls[0]?.[0].toString()).toBe('eventsbymc://app/presenca-rede.json')
  })

  it('preserva domínio público HTTPS configurado e endereço LAN já usado no navegador', async () => {
    const buscar = vi.fn<typeof fetch>()
    expect(await resolverEnderecoPresenca({ ...local, configurado: 'https://eventos.test/app/' }, buscar)).toBe('https://eventos.test/app/')
    expect(await resolverEnderecoPresenca({ ...local, pagina: 'http://192.168.1.9:8081/gestao',
      configurado: 'http://localhost/' }, buscar)).toBe('http://192.168.1.9:8081/')
    expect(buscar).not.toHaveBeenCalled()
  })

  it('no desktop o servidor escolhido no instalador prevalece sobre o build e o IP do cliente', async () => {
    const buscar = vi.fn<typeof fetch>().mockResolvedValue(Response.json({
      publicAppUrl: 'http://192.168.1.4:8081/', endereco: '192.168.1.99',
    }))
    expect(await resolverEnderecoPresenca({ configurado: 'https://antigo.test/',
      pagina: 'eventsbymc://app/organizador/frequencia', caminhoBase: '/' }, buscar)).toBe('http://192.168.1.4:8081/')
    expect(buscar).toHaveBeenCalledOnce()
  })

  it('instalacao em localhost continua descobrindo o IP local para o celular', async () => {
    const buscar = vi.fn<typeof fetch>().mockResolvedValue(Response.json({
      publicAppUrl: 'http://localhost:8081/', endereco: '192.168.1.4',
    }))
    expect(await resolverEnderecoPresenca({ configurado: 'https://antigo.test/',
      pagina: 'eventsbymc://app/organizador/frequencia', caminhoBase: '/' }, buscar)).toBe('http://192.168.1.4:8081/')
  })

  it('falha na leitura do servidor instalado impede QR com o dominio antigo do build', async () => {
    const buscar = vi.fn<typeof fetch>().mockRejectedValue(new Error('Sem configuracao'))
    await expect(resolverEnderecoPresenca({ configurado: 'https://antigo.test/',
      pagina: 'eventsbymc://app/organizador/frequencia', caminhoBase: '/' }, buscar)).rejects.toThrow('Informe o endereço')
  })

  it('não gera QR de loopback quando Docker não recebeu o endereço do host', async () => {
    for (const valor of [null, '127.0.0.1', '0.0.0.0', '999.168.1.4', '010.0.0.1', '192.168.1.4@outro.test', '203.0.113.2']) {
      await expect(resolverEnderecoPresenca(local, vi.fn<typeof fetch>().mockResolvedValue(resposta(valor)))).rejects.toThrow('Informe o endereço')
    }
    for (const obter of [() => new Response('<html>SPA</html>', { headers: { 'Content-Type': 'text/html' } }),
      () => new Response(null, { status: 404 }), () => { throw new Error('Sem rede') }]) {
      await expect(resolverEnderecoPresenca(local, vi.fn<typeof fetch>().mockImplementation(async () => obter()))).rejects.toThrow()
    }
  })

  it('aceita endereço manual da rede sem aceitar credenciais, query, fragmentos ou scripts', () => {
    expect(validarEnderecoPresenca('http://192.168.1.4:8081/app')).toBe('http://192.168.1.4:8081/app/')
    for (const valor of ['http://localhost/', 'http://127.0.0.2/', 'http://[::1]/', 'http://0.0.0.0/',
      'http://[::]/', 'file:///app/', 'javascript:alert(1)', 'http://nome:senha@192.168.1.4/',
      'https://site.test/?token=segredo', 'https://site.test/#codigo']) {
      expect(() => validarEnderecoPresenca(valor)).toThrow()
    }
  })
})
