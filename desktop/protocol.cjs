'use strict'

const fs = require('node:fs/promises')
const path = require('node:path')
const { detectarEnderecoRede } = require('../frontend/tools/endereco-presenca.cjs')

const SCHEME = 'eventsbymc'
const APP_URL = `${SCHEME}://app/`
const CSP = "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data: blob: https://images.unsplash.com; connect-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none'"
const MIME = {
  '.html': 'text/html; charset=utf-8', '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8', '.json': 'application/json',
  '.svg': 'image/svg+xml', '.png': 'image/png', '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg', '.ico': 'image/x-icon', '.woff2': 'font/woff2',
}

function confiavel(valor, base = APP_URL) {
  try {
    const url = new URL(valor)
    const origem = new URL(base)
    return url.protocol === origem.protocol && url.host === origem.host && !url.username && !url.password
  } catch {
    return false
  }
}

function resposta(corpo, status, headers = {}) {
  return new Response(corpo, { status, headers: {
    'Content-Security-Policy': CSP,
    'X-Content-Type-Options': 'nosniff',
    'Cache-Control': 'no-store',
    ...headers,
  } })
}

async function encaminharApi(request, url, apiUrl, fetchImpl) {
  // Somente um servidor configurado; redirecionamentos nao recebem o JWT.
  const relativo = url.pathname.slice('/api/'.length)
  if (relativo.includes('\\') || relativo.includes('%') || relativo.startsWith('/')) {
    return resposta(null, 400)
  }
  const destino = new URL(relativo + url.search, apiUrl)
  if (!destino.href.startsWith(apiUrl)) return resposta(null, 403)
  const headers = new Headers()
  for (const nome of ['Accept', 'Content-Type', 'Authorization']) {
    if (request.headers.has(nome)) headers.set(nome, request.headers.get(nome))
  }
  const leitura = ['GET', 'HEAD'].includes(request.method)
  const body = leitura ? undefined : await request.arrayBuffer()
  if (body && body.byteLength > 1_048_576) return resposta(null, 413)
  try {
    const upstream = await fetchImpl(destino, {
      method: request.method, headers, body, redirect: 'manual',
      signal: AbortSignal.any([request.signal, AbortSignal.timeout(30_000)]),
    })
    if (upstream.status >= 300 && upstream.status < 400) {
      await upstream.body?.cancel()
      return resposta(JSON.stringify({ mensagem: 'A API redirecionou a requisicao. Confira DESKTOP_API_URL.' }), 502,
        { 'Content-Type': 'application/json' })
    }
    const respostaHeaders = {}
    for (const nome of ['Content-Type', 'Content-Disposition']) {
      if (upstream.headers.has(nome)) respostaHeaders[nome] = upstream.headers.get(nome)
    }
    return resposta(leitura && request.method === 'HEAD' ? null : upstream.body,
      upstream.status, respostaHeaders)
  } catch {
    return resposta(JSON.stringify({ mensagem: 'Nao foi possivel conectar a API. Confira sua conexao e tente novamente.' }),
      502, { 'Content-Type': 'application/json' })
  }
}

function criarHandler({ rendererDir, apiUrl, publicAppUrl, fetchImpl = fetch, detectarEndereco = detectarEnderecoRede }) {
  const raiz = path.resolve(rendererDir)
  return async (request) => {
    if (!confiavel(request.url)) return resposta(null, 403)
    const url = new URL(request.url)
    if (url.pathname.startsWith('/api/')) return encaminharApi(request, url, apiUrl, fetchImpl)
    if (!['GET', 'HEAD'].includes(request.method)) return resposta(null, 405)
    if (url.pathname === '/presenca-rede.json') {
      const host = publicAppUrl ? new URL(publicAppUrl).hostname : 'localhost'
      const servidorLocal = ['localhost', '[::1]'].includes(host) || host.startsWith('127.')
      const endereco = servidorLocal ? await detectarEndereco().catch(() => null) : null
      return resposta(request.method === 'HEAD' ? null : JSON.stringify({ endereco, ...(publicAppUrl ? { publicAppUrl } : {}) }), 200,
        { 'Content-Type': 'application/json' })
    }
    let pathname
    try { pathname = decodeURIComponent(url.pathname) } catch { return resposta(null, 400) }
    if (pathname.includes('\\') || pathname.includes('\0') || pathname.includes(':')) return resposta(null, 403)
    const arquivo = path.resolve(raiz, '.' + pathname)
    if (arquivo !== raiz && !arquivo.startsWith(raiz + path.sep)) return resposta(null, 403)
    try {
      const conteudo = await fs.readFile(arquivo === raiz ? path.join(raiz, 'index.html') : arquivo)
      return resposta(request.method === 'HEAD' ? null : conteudo, 200,
        { 'Content-Type': MIME[path.extname(arquivo)] || (arquivo === raiz ? MIME['.html'] : 'application/octet-stream') })
    } catch {
      // Mantem createWebHistory: somente navegacoes HTML recebem o fallback da SPA.
      if (!path.extname(pathname) && request.headers.get('Accept')?.includes('text/html')) {
        try {
          const conteudo = await fs.readFile(path.join(raiz, 'index.html'))
          return resposta(request.method === 'HEAD' ? null : conteudo, 200, { 'Content-Type': MIME['.html'] })
        } catch { /* Build ausente: retornar 404. */ }
      }
      return resposta(null, 404)
    }
  }
}

module.exports = { SCHEME, APP_URL, confiavel, criarHandler }
