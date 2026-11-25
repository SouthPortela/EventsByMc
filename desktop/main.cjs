'use strict'

const { app, BrowserWindow, dialog, protocol, session } = require('electron')
const fs = require('node:fs')
const path = require('node:path')
const { validarUrl } = require('./config.cjs')
const { SCHEME, APP_URL, confiavel, criarHandler } = require('./protocol.cjs')

const desenvolvimento = !app.isPackaged && process.argv.includes('--desktop-dev')
const inicio = desenvolvimento ? 'http://127.0.0.1:5173/' : APP_URL
let janela

protocol.registerSchemesAsPrivileged([{ scheme: SCHEME,
  privileges: { standard: true, secure: true, supportFetchAPI: true, corsEnabled: true } }])

function criarJanela() {
  janela = new BrowserWindow({
    title: 'EventsByMc', width: 1280, height: 850, minWidth: 900, minHeight: 600,
    show: false, autoHideMenuBar: true,
    webPreferences: { nodeIntegration: false, contextIsolation: true, sandbox: true,
      webSecurity: true, webviewTag: false, devTools: desenvolvimento },
  })
  janela.once('ready-to-show', () => janela.show())
  janela.webContents.setWindowOpenHandler(() => ({ action: 'deny' }))
  for (const evento of ['will-navigate', 'will-redirect', 'will-frame-navigate']) {
    janela.webContents.on(evento, (event) => {
      if (!confiavel(event.url, inicio)) event.preventDefault()
    })
  }
  janela.webContents.on('will-attach-webview', (event) => event.preventDefault())
  janela.on('closed', () => { janela = undefined })
  void janela.loadURL(inicio).catch(() => {
    dialog.showErrorBox('EventsByMc', 'Nao foi possivel abrir a interface. Confira o build ou o servidor de desenvolvimento.')
    app.quit()
  })
}

if (!app.requestSingleInstanceLock()) {
  app.quit()
} else {
  app.on('second-instance', () => {
    if (janela?.isMinimized()) janela.restore()
    janela?.show()
    janela?.focus()
  })
  app.whenReady().then(() => {
    session.defaultSession.setPermissionRequestHandler((_webContents, _permission, callback) => callback(false))
    session.defaultSession.setPermissionCheckHandler(() => false)
    session.defaultSession.on('will-download', (event, item, webContents) => {
      if (webContents !== janela?.webContents || !confiavel(webContents.getURL(), inicio) ||
          !(confiavel(item.getURL(), inicio) || item.getURL().startsWith(`blob:${inicio.slice(0, -1)}/`))) {
        event.preventDefault()
        return
      }
      // O Chromium mostra o dialogo de salvamento; nenhum arquivo e aberto automaticamente.
      item.setSaveDialogOptions({ title: 'Salvar arquivo do EventsByMc' })
    })
    if (!desenvolvimento) {
      const config = JSON.parse(fs.readFileSync(path.join(__dirname, 'runtime-config.json'), 'utf8'))
      const apiUrl = validarUrl(config.apiUrl, 'DESKTOP_API_URL')
      protocol.handle(SCHEME, criarHandler({ rendererDir: path.join(__dirname, 'renderer'), apiUrl }))
    }
    criarJanela()
    app.on('activate', () => { if (BrowserWindow.getAllWindows().length === 0) criarJanela() })
  }).catch(() => {
    dialog.showErrorBox('Configuracao do EventsByMc', 'Execute npm run desktop:build com os enderecos configurados em .env.desktop.')
    app.quit()
  })
  app.on('window-all-closed', () => { if (process.platform !== 'darwin') app.quit() })
}
