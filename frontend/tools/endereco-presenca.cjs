'use strict'
/* eslint-disable @typescript-eslint/no-require-imports -- Modulo CommonJS compartilhado com o processo principal do Electron. */

const os = require('node:os')
const dgram = require('node:dgram')
const { isIPv4 } = require('node:net')

function privado(endereco) {
  if (!isIPv4(endereco)) return false
  const [a, b] = endereco.split('.').map(Number)
  return a === 10 || (a === 172 && b >= 16 && b <= 31) || (a === 192 && b === 168)
}

function escolherEndereco(interfaces, enderecoRota) {
  const candidatos = [...new Set(Object.entries(interfaces).flatMap(([nome, enderecos]) => {
    if (/docker|vethernet|virtual|vmware|vbox|wsl|vpn|radmin|loopback|\btun\b|\btap\b/i.test(nome)) return []
    return (enderecos || []).filter(item => !item.internal && item.family === 'IPv4' && privado(item.address))
      .map(item => item.address)
  }))]
  if (candidatos.includes(enderecoRota)) return enderecoRota
  // Havendo mais de uma rede sem rota identificada, nao escolher uma ao acaso.
  return candidatos.length === 1 ? candidatos[0] : null
}

function enderecoDaRota() {
  return new Promise(resolve => {
    const socket = dgram.createSocket('udp4')
    const timer = setTimeout(() => concluir(null), 1500)
    let encerrado = false
    function concluir(endereco) {
      if (encerrado) return
      encerrado = true
      clearTimeout(timer)
      try { socket.close() } catch { /* Socket pode falhar antes de receber uma porta. */ }
      resolve(endereco)
    }
    socket.once('error', () => concluir(null))
    // UDP connect apenas consulta a rota do SO. Nenhum pacote e enviado.
    // O destino pertence ao bloco reservado para documentacao (TEST-NET-1).
    socket.connect(9, '192.0.2.1', () => {
      if (!encerrado) concluir(socket.address().address)
    })
  })
}

async function detectarEnderecoRede() {
  return escolherEndereco(os.networkInterfaces(), await enderecoDaRota())
}

module.exports = { escolherEndereco, detectarEnderecoRede }
