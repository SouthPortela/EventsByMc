'use strict'

const fs = require('node:fs')
const path = require('node:path')
const { spawnSync } = require('node:child_process')
const { carregarAmbiente, configuracaoDoAmbiente } = require('../config.cjs')
const raiz = path.resolve(__dirname, '../..')

try {
  carregarAmbiente(raiz)
  const config = configuracaoDoAmbiente(process.env)
  // O build desktop tem saida propria e nao sobrescreve frontend/dist (site).
  const npm = process.env.npm_execpath
  if (!npm) throw new Error('Execute este script usando npm run desktop:build.')
  const comandos = [
    ['--prefix', 'frontend', 'run', 'type-check'],
    ['--prefix', 'frontend', 'run', 'build-only', '--', '--mode', 'desktop',
      '--outDir', '../desktop/renderer', '--emptyOutDir'],
  ]
  for (const comando of comandos) {
    const resultado = spawnSync(process.execPath, [npm, ...comando], {
      cwd: raiz, stdio: 'inherit', env: { ...process.env,
        VITE_API_BASE_URL: '/api', VITE_PUBLIC_APP_URL: config.publicAppUrl },
    })
    if (resultado.error) throw new Error('Nao foi possivel iniciar o build do Vue.')
    if (resultado.status !== 0) process.exit(resultado.status || 1)
  }
  fs.writeFileSync(path.join(raiz, 'desktop/runtime-config.json'), JSON.stringify(config, null, 2) + '\n')
} catch (erro) {
  console.error(erro.message)
  process.exitCode = 1
}
