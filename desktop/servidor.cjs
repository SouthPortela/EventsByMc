'use strict'

const fs = require('node:fs')
const path = require('node:path')
const { isIPv4 } = require('node:net')
const { validarUrl } = require('./config.cjs')

const ARQUIVO_SERVIDOR = 'servidor.ini'
const ERRO_SERVIDOR = 'Confira o IP ou nome do servidor, a porta e a opcao HTTPS na configuracao da instalacao.'

function configuracaoDoServidor({ host, porta, https }) {
  if (typeof host !== 'string' || typeof porta !== 'string' || !['0', '1'].includes(https)) {
    throw new Error(ERRO_SERVIDOR)
  }
  const nome = host.trim().toLowerCase()
  if (!nome || nome.length > 253 || !nome.split('.').every(parte =>
    /^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$/.test(parte)) ||
    (/^[\d.]+$/.test(nome) && !isIPv4(nome)) || nome === '0.0.0.0' ||
    !/^\d{1,5}$/.test(porta) || Number(porta) < 1 || Number(porta) > 65535) {
    throw new Error(ERRO_SERVIDOR)
  }
  const publicAppUrl = validarUrl(`${https === '1' ? 'https' : 'http'}://${nome}:${Number(porta)}/`, 'Servidor')
  return { publicAppUrl, apiUrl: new URL('api/', publicAppUrl).href }
}

function interpretarServidor(conteudo) {
  if (conteudo.length > 4096) throw new Error(ERRO_SERVIDOR)
  // WriteINIStr do Windows pode preservar UTF-16 em uma configuracao existente.
  const texto = (conteudo[0] === 0xff && conteudo[1] === 0xfe
    ? conteudo.toString('utf16le') : conteudo.toString('utf8')).replace(/^\uFEFF/, '')
  const valores = Object.create(null)
  let servidor = false
  for (const linhaOriginal of texto.split(/\r?\n/)) {
    const linha = linhaOriginal.trim()
    if (!linha || linha.startsWith(';') || linha.startsWith('#')) continue
    if (linha.startsWith('[')) {
      servidor = linha.toLowerCase() === '[servidor]'
      continue
    }
    if (!servidor) continue
    const indice = linha.indexOf('=')
    if (indice < 0) throw new Error(ERRO_SERVIDOR)
    const chave = linha.slice(0, indice).trim().toLowerCase()
    if (!['host', 'porta', 'https'].includes(chave) || Object.hasOwn(valores, chave)) {
      throw new Error(ERRO_SERVIDOR)
    }
    valores[chave] = linha.slice(indice + 1).trim()
  }
  return configuracaoDoServidor(valores)
}

function carregarServidor(diretorioInstalacao, configPadrao) {
  let conteudo
  try { conteudo = fs.readFileSync(path.join(diretorioInstalacao, ARQUIVO_SERVIDOR)) }
  catch (erro) {
    if (erro.code === 'ENOENT') return configPadrao
    throw new Error(ERRO_SERVIDOR)
  }
  return interpretarServidor(conteudo)
}

module.exports = { ARQUIVO_SERVIDOR, configuracaoDoServidor, interpretarServidor, carregarServidor }
