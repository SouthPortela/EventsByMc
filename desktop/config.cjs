'use strict'

const fs = require('node:fs')
const path = require('node:path')

function validarUrl(valor, nome) {
  let url
  try {
    url = new URL(valor)
  } catch {
    throw new Error(`Configure ${nome} com uma URL absoluta.`)
  }
  const local = ['localhost', '127.0.0.1', '[::1]'].includes(url.hostname)
  if ((url.protocol !== 'https:' && !(local && url.protocol === 'http:')) ||
      url.username || url.password || url.search || url.hash) {
    throw new Error(`${nome} exige HTTPS, sem credenciais, query ou fragmento. HTTP permitido somente em loopback.`)
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
