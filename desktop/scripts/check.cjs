'use strict'

const fs = require('node:fs')
const path = require('node:path')
const { spawnSync } = require('node:child_process')

function verificar(diretorio) {
  for (const item of fs.readdirSync(diretorio, { withFileTypes: true })) {
    const arquivo = path.join(diretorio, item.name)
    if (item.isDirectory() && !['renderer', 'node_modules'].includes(item.name)) verificar(arquivo)
    if (item.isFile() && item.name.endsWith('.cjs')) {
      const resultado = spawnSync(process.execPath, ['--check', arquivo], { stdio: 'inherit' })
      if (resultado.error || resultado.status !== 0) process.exitCode = 1
    }
  }
}
verificar(path.resolve(__dirname, '..'))
