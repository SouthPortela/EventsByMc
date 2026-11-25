'use strict'

const { test } = require('node:test')
const assert = require('node:assert/strict')
const fs = require('node:fs/promises')
const os = require('node:os')
const path = require('node:path')
const { validarUrl, configuracaoDoAmbiente } = require('../config.cjs')
const { APP_URL, confiavel, criarHandler } = require('../protocol.cjs')

test('configuracao permite HTTP em loopback ou IP privado e rejeita credenciais', () => {
  assert.equal(validarUrl('https://eventos.test/api', 'API'), 'https://eventos.test/api/')
  for (const valor of ['http://eventos.test/', 'https://nome:senha@eventos.test/',
    'https://eventos.test/?token=valor', 'file:///C:/index.html', 'https://eventos.test/#codigo']) {
    assert.throws(() => validarUrl(valor, 'API'))
  }
  for (const valor of ['http://127.0.0.1:8080/', 'http://localhost/api/', 'http://[::1]/api/',
    'http://192.168.1.2/', 'http://10.0.0.5/api/', 'http://172.16.0.1/api/']) {
    assert.equal(validarUrl(valor, 'API'), valor)
  }
  for (const valor of ['http://203.0.113.2/', 'http://localhost.evil/', 'http://127.0.0.1.evil/',
    'http://172.32.0.1/', 'http://192.169.1.2/']) {
    assert.throws(() => configuracaoDoAmbiente({ DESKTOP_API_URL: valor }))
  }
})

test('padroes do build e desenvolvimento usam o frontend Docker local como proxy da API', () => {
  assert.deepEqual(configuracaoDoAmbiente({}), {
    apiUrl: 'http://127.0.0.1/api/', publicAppUrl: 'http://127.0.0.1/',
  })
  assert.deepEqual(configuracaoDoAmbiente({ DESKTOP_API_URL: 'http://127.0.0.1:8081/api',
    DESKTOP_PUBLIC_APP_URL: 'http://127.0.0.1:8081' }), {
    apiUrl: 'http://127.0.0.1:8081/api/', publicAppUrl: 'http://127.0.0.1:8081/',
  })
})

test('navegacao permanece na origem do aplicativo', () => {
  assert.equal(confiavel(APP_URL + 'organizador'), true)
  for (const url of ['https://app/', 'eventsbymc://app.evil/', 'eventsbymc://usuario@app/',
    'file:///C:/Windows/', 'javascript:alert(1)']) assert.equal(confiavel(url), false)
})

test('protocolo serve assets e rotas Vue sem expor arquivos externos', async () => {
  const temporario = await fs.mkdtemp(path.join(os.tmpdir(), 'events-desktop-test-'))
  try {
    await fs.writeFile(path.join(temporario, 'index.html'), '<html>EventsByMc</html>')
    await fs.writeFile(path.join(temporario, 'app.js'), 'const ativo = true')
    const handler = criarHandler({ rendererDir: temporario, apiUrl: 'https://eventos.test/api/' })
    const html = await handler(new Request(APP_URL + 'organizador/frequencia', { headers: { Accept: 'text/html' } }))
    assert.equal(html.status, 200)
    assert.match(await html.text(), /EventsByMc/)
    assert.match(html.headers.get('Content-Security-Policy'), /script-src 'self'/)
    const js = await handler(new Request(APP_URL + 'app.js'))
    assert.match(js.headers.get('Content-Type'), /javascript/)
    assert.equal((await handler(new Request(APP_URL + 'ausente.js'))).status, 404)
    assert.equal((await handler(new Request(APP_URL + '%2e%2e%2fpackage.json'))).status, 403)
    assert.equal((await handler(new Request(APP_URL + '..%5cpackage.json'))).status, 403)
    assert.equal((await handler(new Request('eventsbymc://outro/index.html'))).status, 403)
    assert.equal((await handler(new Request(APP_URL, { method: 'POST' }))).status, 405)
  } finally {
    const destino = path.resolve(temporario)
    assert.ok(destino.startsWith(path.resolve(os.tmpdir()) + path.sep + 'events-desktop-test-'))
    await fs.rm(destino, { recursive: true, force: true })
  }
})

test('ponte preserva metodo, corpo e JWT sem encaminhar cookies ou permitir troca de destino', async () => {
  let chamada
  const handler = criarHandler({ rendererDir: '.', apiUrl: 'https://eventos.test/api/',
    fetchImpl: async (url, init) => {
      chamada = { url, init }
      return Response.json({ id: 'evento' }, { status: 201 })
    } })
  const response = await handler(new Request(APP_URL + 'api/eventos?pagina=2', {
    method: 'POST', headers: { Authorization: 'Bearer teste', 'Content-Type': 'application/json', Cookie: 'sessao=teste' },
    body: JSON.stringify({ titulo: 'Evento' }),
  }))
  assert.equal(response.status, 201)
  assert.equal(chamada.url.href, 'https://eventos.test/api/eventos?pagina=2')
  assert.equal(chamada.init.method, 'POST')
  assert.equal(chamada.init.headers.get('Authorization'), 'Bearer teste')
  assert.equal(chamada.init.headers.has('Cookie'), false)
  assert.equal(chamada.init.redirect, 'manual')
  assert.equal(Buffer.from(chamada.init.body).toString(), '{"titulo":"Evento"}')
  assert.equal((await handler(new Request(APP_URL + 'api//outro.test/'))).status, 400)
})

test('ponte preserva respostas 401, downloads e exclusoes sem conteudo', async () => {
  let upstream = Response.json({ mensagem: 'Token expirado' }, { status: 401 })
  const handler = criarHandler({ rendererDir: '.', apiUrl: 'https://eventos.test/', fetchImpl: async () => upstream })
  const negado = await handler(new Request(APP_URL + 'api/usuarios/me'))
  assert.equal(negado.status, 401)
  assert.equal((await negado.json()).mensagem, 'Token expirado')
  upstream = new Response('%PDF-teste', { headers: { 'Content-Type': 'application/pdf',
    'Content-Disposition': 'attachment; filename="relatorio.pdf"' } })
  const pdf = await handler(new Request(APP_URL + 'api/relatorios.pdf'))
  assert.equal(pdf.headers.get('Content-Type'), 'application/pdf')
  assert.match(pdf.headers.get('Content-Disposition'), /relatorio.pdf/)
  assert.equal(await pdf.text(), '%PDF-teste')
  upstream = new Response(null, { status: 204 })
  assert.equal((await handler(new Request(APP_URL + 'api/agenda', { method: 'DELETE' }))).status, 204)
})

test('redirecionamentos e falhas de rede retornam erro sem expor detalhes internos', async () => {
  for (const fetchImpl of [async () => Response.redirect('https://outro.test/'),
    async () => { throw new Error('detalhe confidencial') }]) {
    const handler = criarHandler({ rendererDir: '.', apiUrl: 'https://eventos.test/', fetchImpl })
    const response = await handler(new Request(APP_URL + 'api/eventos'))
    assert.equal(response.status, 502)
    assert.doesNotMatch(await response.text(), /confidencial/)
  }
})
