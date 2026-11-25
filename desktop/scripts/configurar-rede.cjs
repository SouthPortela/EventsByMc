'use strict'

const { execFileSync } = require('node:child_process')
const path = require('node:path')
const { detectarEnderecoRede } = require('../../frontend/tools/endereco-presenca.cjs')

async function configurarRede({ detectar = detectarEnderecoRede, executar = execFileSync } = {}) {
  const endereco = await detectar()
  if (!endereco) throw new Error('Nao foi possivel identificar uma unica rede local. Informe o endereco na tela de chamada.')
  // O host consulta suas interfaces; o container nunca tenta adivinhar o IP do host.
  // Nenhuma interpolacao de dados em comandos de shell e nenhum segredo no arquivo.
  executar('docker', ['compose', 'exec', '-T', 'frontend', 'sh', '-c',
    'cat > /usr/share/nginx/html/presenca-rede.json'], {
    cwd: path.resolve(__dirname, '../..'), windowsHide: true, timeout: 15_000,
    input: JSON.stringify({ endereco }) + '\n', stdio: ['pipe', 'pipe', 'pipe'],
  })
  return endereco
}

if (require.main === module) {
  configurarRede().then(endereco => {
    console.log(`Endereco de rede para os QR Codes atualizado: ${endereco}.`)
  }).catch(() => {
    console.error('Nao foi possivel configurar o QR para a rede. Verifique a conexao local e inicie o frontend com docker compose up -d --build. Voce tambem pode informar o endereco na tela de chamada.')
    process.exitCode = 1
  })
}

module.exports = { configurarRede }
