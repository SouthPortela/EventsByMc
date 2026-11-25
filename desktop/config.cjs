'use strict'

const fs = require('node:fs')
const path = require('node:path')
const { isIPv4 } = require('node:net')

function httpLocal(host) {
  if (['localhost', '127.0.0.1', '[::1]'].includes(host)) return true
  if (!isIPv4(host)) return false
  const [a, b] = host.split('.').map(Number)
  return a === 127 || a === 10 || (a === 172 && b >= 16 && b <= 31) || (a === 192 && b === 168)
}

function validarUrl(valor, nome) {
  let url
  try {
    url = new URL(valor)
  } catch {
    throw new Error(`Configure ${nome} com uma URL absoluta.`)
  }
  const local = httpLocal(url.hostname)
  if ((url.protocol !== 'https:' && !(local && url.protocol === 'http:')) ||
      url.username || url.password || url.search || url.hash) {
    throw new Error(`${nome} exige HTTPS, sem credenciais, query ou fragmento. HTTP permitido em localhost ou IP privado da rede.`)
  }
  url.pathname = url.pathname.replace(/\/*$/, '/')
  return url.href
}

function carregarAmbiente(raiz) {
  const arquivo = path.join(raiz, '.env.desktop')
  if (fs.existsSync(arquivo)) process.loadEnvFile(arquivo)
}

function configuracaoDoAmbiente(env) {
  return {
    apiUrl: validarUrl(env.DESKTOP_API_URL || 'http://127.0.0.1/api/', 'DESKTOP_API_URL'),
    publicAppUrl: validarUrl(env.DESKTOP_PUBLIC_APP_URL || 'http://127.0.0.1/', 'DESKTOP_PUBLIC_APP_URL'),
  }
}

module.exports = { validarUrl, carregarAmbiente, configuracaoDoAmbiente }
