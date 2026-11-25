'use strict'

// Executado pelo Electron real, em janela oculta, sem acessar a API de producao.
const { app, BrowserWindow, protocol } = require('electron')
const assert = require('node:assert/strict')
const path = require('node:path')
const { APP_URL, SCHEME, criarHandler } = require('../protocol.cjs')

protocol.registerSchemesAsPrivileged([{ scheme: SCHEME,
  privileges: { standard: true, secure: true, supportFetchAPI: true, corsEnabled: true } }])
const limite = setTimeout(() => { console.error('Smoke desktop excedeu 20 segundos.'); app.exit(1) }, 20_000)

app.whenReady().then(async () => {
  protocol.handle(SCHEME, criarHandler({ rendererDir: path.resolve(__dirname, '../renderer'),
    apiUrl: 'https://api.test/', fetchImpl: async () => Response.json([]) }))
  const janela = new BrowserWindow({ show: false,
    webPreferences: { nodeIntegration: false, contextIsolation: true, sandbox: true } })
  await janela.loadURL(APP_URL + 'login')
  const resultado = await janela.webContents.executeJavaScript(`(async () => {
    const ate = Date.now() + 5000;
    while (!document.querySelector('form') && Date.now() < ate) await new Promise(r => setTimeout(r, 50));
    sessionStorage.setItem('smoke', 'ok');
    const api = await fetch('/api/eventos');
    return { formulario: !!document.querySelector('form'), texto: document.body.innerText,
      node: typeof window.require, processo: typeof window.process,
      storage: sessionStorage.getItem('smoke'), status: api.status, dados: await api.json(),
      css: [...document.styleSheets].some(s => s.href && s.cssRules.length > 0) };
  })()`)
  assert.equal(resultado.formulario, true)
  assert.match(resultado.texto, /Entrar/i)
  assert.equal(resultado.node, 'undefined')
  assert.equal(resultado.processo, 'undefined')
  assert.equal(resultado.storage, 'ok')
  assert.equal(resultado.css, true)
  assert.equal(resultado.status, 200)
  assert.deepEqual(resultado.dados, [])
  const recarregada = new Promise((resolve) => janela.webContents.once('did-finish-load', resolve))
  janela.webContents.reload()
  await recarregada
  assert.equal(new URL(janela.webContents.getURL()).pathname, '/login')
  console.log('Smoke desktop aprovado: Vue, rota direta, CSS, storage, sandbox e fetch /api com API simulada.')
  clearTimeout(limite)
  janela.destroy()
  app.exit(0)
}).catch(() => {
  console.error('Smoke desktop falhou ao carregar a interface ou validar o protocolo.')
  clearTimeout(limite)
  app.exit(1)
})
