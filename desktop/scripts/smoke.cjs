'use strict'

const path = require('node:path')
const { spawn } = require('node:child_process')
const env = { ...process.env }
delete env.ELECTRON_RUN_AS_NODE
const processo = spawn(require('electron'), [path.resolve(__dirname, '../tests/smoke.cjs')],
  { env, stdio: 'inherit', windowsHide: true })
processo.on('error', () => { process.exitCode = 1 })
processo.on('exit', (code) => { process.exitCode = code ?? 1 })
