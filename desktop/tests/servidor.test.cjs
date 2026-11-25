'use strict'

const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')
const { configuracaoDoServidor, interpretarServidor, carregarServidor } = require('../servidor.cjs')
const { criarHandler, APP_URL } = require('../protocol.cjs')

test('opcoes do instalador definem API e site no mesmo servidor com localhost como padrao', () => {
  assert.deepEqual(configuracaoDoServidor({ host: 'localhost', porta: '80', https: '0' }), {
    apiUrl: 'http://localhost/api/', publicAppUrl: 'http://localhost/',
  })
  assert.deepEqual(configuracaoDoServidor({ host: '192.168.1.4', porta: '8081', https: '0' }), {
    apiUrl: 'http://192.168.1.4:8081/api/', publicAppUrl: 'http://192.168.1.4:8081/',
  })
  assert.deepEqual(configuracaoDoServidor({ host: 'eventos.exemplo.test', porta: '443', https: '1' }), {
    apiUrl: 'https://eventos.exemplo.test/api/', publicAppUrl: 'https://eventos.exemplo.test/',
  })
})

test('le INI do Windows em ASCII, UTF-8 BOM e UTF-16 LE sem depender de bibliotecas', () => {
  const ini = '; gerado pelo instalador\r\n[servidor]\r\nhost=192.168.1.4\r\nporta=8081\r\nhttps=0\r\n'
  for (const buffer of [Buffer.from(ini), Buffer.from('\uFEFF' + ini), Buffer.from('\uFEFF' + ini, 'utf16le')]) {
    assert.equal(interpretarServidor(buffer).apiUrl, 'http://192.168.1.4:8081/api/')
  }
})

test('configuracao invalida nao muda silenciosamente para localhost nem permite URLs e credenciais no host', () => {
  for (const host of ['http://192.168.1.4', '192.168.1.4/api/', 'nome:senha@192.168.1.4',
    '192.168.1.4?token=valor', '192.168.1.4#codigo', '999.168.1.4', '-servidor',
    'servidor..local', '0.0.0.0', 'localhost\r\nporta=80']) {
    assert.throws(() => configuracaoDoServidor({ host, porta: '80', https: '0' }))
  }
  for (const porta of ['0', '65536', '-1', '80/api', '8e1', '']) {
    assert.throws(() => configuracaoDoServidor({ host: 'localhost', porta, https: '0' }))
  }
  assert.throws(() => configuracaoDoServidor({ host: '203.0.113.2', porta: '80', https: '0' }))
  for (const ini of ['[servidor]\nhost=localhost', '[servidor]\nhost=localhost\nporta=80\nhttps=0\nhost=outro',
    '[outra]\nhost=localhost\nporta=80\nhttps=0', '[servidor]\nhost=localhost\nporta=80\nhttps=sim']) {
    assert.throws(() => interpretarServidor(Buffer.from(ini)))
  }
})

test('arquivo da instalacao prevalece sobre build e permanece configuravel sem recompilar', async () => {
  const diretorio = await fs.mkdtemp(path.join(os.tmpdir(), 'events-servidor-test-'))
  const padrao = { apiUrl: 'https://antigo.test/api/', publicAppUrl: 'https://antigo.test/' }
  const arquivo = path.join(diretorio, 'servidor.ini')
  try {
    assert.deepEqual(carregarServidor(diretorio, padrao), padrao)
    await fs.writeFile(arquivo, '[servidor]\nhost=192.168.1.4\nporta=8081\nhttps=0\n')
    assert.equal(carregarServidor(diretorio, padrao).apiUrl, 'http://192.168.1.4:8081/api/')
    await fs.writeFile(arquivo, '[servidor]\nhost=10.0.0.9\nporta=80\nhttps=0\n')
    assert.equal(carregarServidor(diretorio, padrao).apiUrl, 'http://10.0.0.9/api/')
    await fs.writeFile(arquivo, 'configuracao invalida')
    assert.throws(() => carregarServidor(diretorio, padrao))
  } finally {
    const destino = path.resolve(diretorio)
    assert.ok(destino.startsWith(path.resolve(os.tmpdir()) + path.sep + 'events-servidor-test-'))
    await fs.rm(destino, { recursive: true, force: true })
  }
})

test('requisicoes e QR usam o servidor escolhido sem descobrir o IP do computador cliente', async () => {
  const config = configuracaoDoServidor({ host: '192.168.1.4', porta: '8081', https: '0' })
  let destino
  const handler = criarHandler({ rendererDir: '.', ...config,
    detectarEndereco: () => assert.fail('Nao deve usar a interface do cliente para servidor remoto'),
    fetchImpl: async (url, init) => {
      destino = url.href
      assert.equal(init.headers.get('Authorization'), 'Bearer teste')
      return Response.json([])
    },
  })
  assert.deepEqual(await (await handler(new Request(APP_URL + 'presenca-rede.json'))).json(), {
    endereco: null, publicAppUrl: 'http://192.168.1.4:8081/',
  })
  await handler(new Request(APP_URL + 'api/eventos', { headers: { Authorization: 'Bearer teste' } }))
  assert.equal(destino, 'http://192.168.1.4:8081/api/eventos')
})
