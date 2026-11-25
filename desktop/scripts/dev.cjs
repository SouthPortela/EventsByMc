'use strict'

const path = require('node:path')
const { spawn } = require('node:child_process')
const waitOn = require('wait-on')
const { carregarAmbiente, configuracaoDoAmbiente } = require('../config.cjs')
const raiz = path.resolve(__dirname, '../..')
let vite
let electron
let encerrando = false

function encerrar(codigo = 0) {
  if (encerrando) return
  encerrando = true
  electron?.kill()
  vite?.kill()
  process.exitCode = codigo
}

async function iniciar() {
  carregarAmbiente(raiz)
  const config = configuracaoDoAmbiente(process.env)
  const env = { ...process.env,
    API_PROXY_TARGET: config.apiUrl, API_PROXY_KEEP_PREFIX: 'false',
    VITE_API_BASE_URL: '/api', VITE_PUBLIC_APP_URL: config.publicAppUrl }
  delete env.ELECTRON_RUN_AS_NODE
  vite = spawn(process.execPath, [path.join(raiz, 'frontend/node_modules/vite/bin/vite.js'),
    '--host', '127.0.0.1', '--port', '5173', '--strictPort'], {
    cwd: path.join(raiz, 'frontend'), env, stdio: 'inherit', windowsHide: true,
  })
  vite.on('error', () => encerrar(1))
  vite.on('exit', (code) => encerrar(code || 0))
  await waitOn({ resources: ['http-get://127.0.0.1:5173'], timeout: 30_000, interval: 250 })
  if (encerrando) return
  electron = spawn(require('electron'), [raiz, '--desktop-dev'], { cwd: raiz, env, stdio: 'inherit', windowsHide: true })
  electron.on('error', () => encerrar(1))
  electron.on('exit', (code) => encerrar(code || 0))
}

process.on('SIGINT', () => encerrar())
process.on('SIGTERM', () => encerrar())
void iniciar().catch(() => {
  console.error('Nao foi possivel iniciar o desktop. Confira .env.desktop, as dependencias e a disponibilidade da porta 5173.')
  encerrar(1)
})
