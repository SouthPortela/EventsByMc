'use strict'

const { test } = require('node:test')
const assert = require('node:assert/strict')
const { escolherEndereco } = require('../../frontend/tools/endereco-presenca.cjs')
const { configurarRede } = require('../scripts/configurar-rede.cjs')
const { APP_URL, criarHandler } = require('../protocol.cjs')

const adaptador = address => [{ address, family: 'IPv4', internal: false }]

test('usa a interface da rota e ignora VPN, Docker e interfaces internas', () => {
  assert.equal(escolherEndereco({
    'Radmin VPN': adaptador('26.63.164.40'),
    'vEthernet (WSL)': adaptador('172.31.112.1'),
    'Ethernet 2': adaptador('192.168.1.4'),
    'Ethernet 3': adaptador('192.168.56.1'),
    Loopback: [{ address: '127.0.0.1', family: 'IPv4', internal: true }],
  }, '192.168.1.4'), '192.168.1.4')
  assert.equal(escolherEndereco({ Ethernet: adaptador('10.20.30.40') }, null), '10.20.30.40')
  assert.equal(escolherEndereco({ Ethernet: adaptador('172.20.1.2') }, null), '172.20.1.2')
})

test('nao adivinha entre redes e nao retorna endereco publico ou sem rede', () => {
  assert.equal(escolherEndereco({ Ethernet: adaptador('192.168.1.4'), WiFi: adaptador('10.0.0.5') }, null), null)
  assert.equal(escolherEndereco({ VPN: adaptador('10.0.0.1'), Docker: adaptador('172.17.0.1') }, '10.0.0.1'), null)
  assert.equal(escolherEndereco({ Ethernet: adaptador('203.0.113.2') }, null), null)
  assert.equal(escolherEndereco({}, null), null)
})

test('protocolo consulta novamente o host apos troca DHCP sem enviar JWT para descoberta', async () => {
  let endereco = '192.168.1.4'
  const handler = criarHandler({ rendererDir: '.', apiUrl: 'http://127.0.0.1/api/',
    detectarEndereco: async () => endereco,
    fetchImpl: () => { throw new Error('Nao deve chamar a API') },
  })
  const obter = () => handler(new Request(APP_URL + 'presenca-rede.json'))
  const primeiro = await obter()
  assert.equal(primeiro.headers.get('Cache-Control'), 'no-store')
  assert.deepEqual(await primeiro.json(), { endereco: '192.168.1.4' })
  endereco = '192.168.1.7'
  assert.deepEqual(await (await obter()).json(), { endereco: '192.168.1.7' })
  assert.equal((await handler(new Request(APP_URL + 'presenca-rede.json', { method: 'POST' }))).status, 405)
  assert.equal((await handler(new Request('eventsbymc://outro/presenca-rede.json'))).status, 403)
  const semRede = criarHandler({ rendererDir: '.', apiUrl: 'http://127.0.0.1/api/',
    detectarEndereco: async () => { throw new Error('Sem rede') } })
  assert.deepEqual(await (await semRede(new Request(APP_URL + 'presenca-rede.json'))).json(), { endereco: null })
})

test('comando Docker transfere somente o IP detectado por stdin sem alterar compose ou banco', async () => {
  let chamada
  const endereco = await configurarRede({ detectar: async () => '192.168.1.4',
    executar: (...args) => { chamada = args } })
  assert.equal(endereco, '192.168.1.4')
  assert.equal(chamada[0], 'docker')
  assert.deepEqual(chamada[1], ['compose', 'exec', '-T', 'frontend', 'sh', '-c',
    'cat > /usr/share/nginx/html/presenca-rede.json'])
  assert.deepEqual(JSON.parse(chamada[2].input), { endereco: '192.168.1.4' })
  assert.equal(chamada[2].windowsHide, true)
  await assert.rejects(configurarRede({ detectar: async () => null,
    executar: () => assert.fail('Sem rede, nao deve executar Docker') }))
  await assert.rejects(configurarRede({ detectar: async () => '192.168.1.4',
    executar: () => { throw new Error('Docker indisponivel') } }))
})
