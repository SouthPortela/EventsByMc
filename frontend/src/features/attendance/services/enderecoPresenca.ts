const erroEndereco = 'Informe o endereço do site acessível pelo celular, com a porta utilizada pelo sistema.'

export function enderecoLoopback(host: string): boolean {
  return host === 'localhost' || host.endsWith('.localhost') || host === '[::1]' || host.startsWith('127.')
}

export function validarEnderecoPresenca(valor: string): string {
  let url: URL
  try { url = new URL(valor) } catch { throw new Error(erroEndereco) }
  if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password ||
      url.search || url.hash || enderecoLoopback(url.hostname) ||
      ['0.0.0.0', '[::]'].includes(url.hostname)) throw new Error(erroEndereco)
  url.pathname = url.pathname.replace(/\/*$/, '/')
  return url.href
}

function ipv4Privado(valor: unknown): valor is string {
  if (typeof valor !== 'string' || !/^(?:0|[1-9]\d{0,2})(?:\.(?:0|[1-9]\d{0,2})){3}$/.test(valor)) return false
  const partes = valor.split('.').map(Number)
  if (partes.some(p => p > 255)) return false
  return partes[0] === 10 || (partes[0] === 172 && partes[1]! >= 16 && partes[1]! <= 31) ||
    (partes[0] === 192 && partes[1] === 168)
}

export async function resolverEnderecoPresenca(
  opcoes: { configurado?: string; pagina: string; caminhoBase: string },
  buscar: typeof fetch = fetch,
): Promise<string> {
  const atual = new URL(opcoes.caminhoBase, opcoes.pagina)
  let base = new URL(opcoes.configurado || atual.href)
  let descoberta: unknown
  // O instalador pode mudar o servidor depois do build do Vue.
  if (atual.protocol === 'eventsbymc:') {
    descoberta = await consultarRede(atual, buscar)
    if (!descoberta || typeof descoberta !== 'object') throw new Error(erroEndereco)
    if (descoberta && typeof descoberta === 'object' && 'publicAppUrl' in descoberta &&
        typeof descoberta.publicAppUrl === 'string') base = new URL(descoberta.publicAppUrl)
  }
  // Um dominio publico configurado continua sendo o destino em producao.
  if (!enderecoLoopback(base.hostname)) return validarEnderecoPresenca(base.href)
  if (['http:', 'https:'].includes(atual.protocol) && !enderecoLoopback(atual.hostname)) {
    return validarEnderecoPresenca(atual.href)
  }
  const dados = atual.protocol === 'eventsbymc:' ? descoberta : await consultarRede(atual, buscar)
  if (dados && typeof dados === 'object' && 'endereco' in dados && ipv4Privado(dados.endereco)) {
    // Alterar apenas o host: manter protocolo, porta e caminho do site.
    base.hostname = dados.endereco
    return validarEnderecoPresenca(base.href)
  }
  throw new Error(erroEndereco)
}

async function consultarRede(atual: URL, buscar: typeof fetch): Promise<unknown> {
  try {
    const resposta = await buscar(new URL('presenca-rede.json', atual), {
      cache: 'no-store', credentials: 'omit', redirect: 'error', signal: AbortSignal.timeout(2500),
    })
    if (resposta.ok && resposta.headers.get('Content-Type')?.includes('application/json')) return await resposta.json()
  } catch { /* Sem descoberta disponivel: pedir o endereco, sem gerar QR de localhost. */ }
  return null
}
