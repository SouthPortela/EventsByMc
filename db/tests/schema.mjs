// Executa apenas em PostgreSQL embarcado e efêmero; não lê credenciais ou DB_URL.
// Preparação: npm install --prefix target/sql-validation --no-save --package-lock=false @electric-sql/pglite
// Execução, na raiz: node --test db/tests/schema.mjs
import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile } from 'node:fs/promises'
import { randomUUID } from 'node:crypto'
import { PGlite } from '../../target/sql-validation/node_modules/@electric-sql/pglite/dist/index.js'

const raiz = new URL('../../', import.meta.url)
async function lerEntrada(url) {
  const linhas = (await readFile(url, 'utf8')).split(/\r?\n/)
  const saida = []
  for (const linha of linhas) {
    if (linha.startsWith('\\set ')) continue
    if (linha.startsWith('\\ir ')) saida.push(await lerEntrada(new URL(linha.slice(4).trim(), url)))
    else saida.push(linha)
  }
  return saida.join('\n')
}

test('inicialização completa, constraints e consultas do adaptador de presença', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    assert.equal((await db.query('SELECT count(*)::int AS total FROM versoes_schema')).rows[0].total, 3)
    const usuario = randomUUID(), outro = randomUUID(), evento = randomUUID(), evento2 = randomUUID()
    const atividade = randomUUID(), atividade2 = randomUUID(), inscricao = randomUUID(), chamada = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email,senha_hash) VALUES ($1,'Teste','teste@example.test','hash-teste'),($2,'Outro','outro@example.test','hash-teste')", [usuario, outro])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id,inicio,fim,local,estado) VALUES ($1,'Evento A',$3,'2026-10-10 09:00','2026-10-10 18:00','Sala','PUBLICADO'),($2,'Evento B',$3,'2026-10-10 09:00','2026-10-10 18:00','Sala','PUBLICADO')", [evento, evento2, usuario])
    await db.query("INSERT INTO atividades(id,evento_id,titulo,inicio,fim) VALUES ($1,$3,'Palestra A','2026-10-10 09:00','2026-10-10 10:00'),($2,$4,'Palestra B','2026-10-10 09:00','2026-10-10 10:00')", [atividade, atividade2, evento, evento2])
    await db.query("INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)", [inscricao, evento, usuario])
    await assert.rejects(db.query("INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)", [randomUUID(), evento, usuario]), { code: '23505' })
    await assert.rejects(db.query("UPDATE eventos SET fim = NULL WHERE id = $1", [evento]), { code: '23514' })
    await assert.rejects(db.query("UPDATE inscricoes SET estado = 'CANCELADA' WHERE id = $1", [inscricao]), { code: '23514' })
    const sqlChamada = "INSERT INTO chamadas_presenca(id,atividade_id,evento_id,criada_por,codigo_hash,criada_em,expira_em) VALUES ($1,$2,$3,$4,$5,'2026-10-10 12:00Z','2026-10-10 12:05Z')"
    await db.query(sqlChamada, [chamada, atividade, evento, usuario, 'a'.repeat(64)])
    await assert.rejects(db.query(sqlChamada, [randomUUID(), atividade, evento, usuario, 'b'.repeat(64)]), { code: '23505' })
    await assert.rejects(db.query("UPDATE chamadas_presenca SET expira_em = criada_em + interval '6 minutes' WHERE id = $1", [chamada]), { code: '23514' })
    const sqlPresenca = "INSERT INTO presencas(id,atividade_id,evento_id,usuario_id,inscricao_id,chamada_id,registrada_em,origem) VALUES ($1,$2,$3,$4,$5,$6,'2026-10-10 12:01Z','CODIGO')"
    await db.query(sqlPresenca, [randomUUID(), atividade, evento, usuario, inscricao, chamada])
    await assert.rejects(db.query(sqlPresenca, [randomUUID(), atividade, evento, usuario, inscricao, chamada]), { code: '23505' })
    await assert.rejects(db.query(sqlPresenca, [randomUUID(), atividade2, evento2, outro, inscricao, chamada]), { code: '23503' })
    await assert.rejects(db.query("DELETE FROM eventos WHERE id=$1", [evento]), e => ['23001', '23503'].includes(e.code))
    await db.query("UPDATE chamadas_presenca SET revogada_em='2026-10-10 12:02Z' WHERE id=$1", [chamada])
    await db.query(sqlChamada, [randomUUID(), atividade, evento, usuario, 'b'.repeat(64)])
    assert.equal((await db.query('SELECT count(*)::int AS total FROM presencas')).rows[0].total, 1)

    // EXPLAIN verifica sintaxe/tipos das consultas reais, sem executar INSERT/UPDATE.
    const fonte = await readFile(new URL('src/main/java/br/com/eventsbymc/eventsapi/adapter/out/jdbc/JdbcPresencaRepository.java', raiz), 'utf8')
    const sqls = [...fonte.matchAll(/prepareStatement\("([^"]+)"\)/g)].map(m => m[1])
    sqls.push(...[...fonte.matchAll(/String\s+\w+\s*=\s*"""([\s\S]*?)"""/g)].map(m => m[1]))
    assert.ok(sqls.length >= 20)
    const sqlInscricoes = sqls.find(sql => sql.includes('FROM inscricoes i JOIN eventos e'))
    const sqlAgenda = sqls.find(sql => sql.includes('JOIN atividades a ON a.evento_id = i.evento_id'))
    assert.ok(sqlInscricoes)
    assert.ok(sqlAgenda)
    assert.equal((await db.query(sqlInscricoes.replace('?', '$1'), [usuario])).rows.length, 1)
    assert.equal((await db.query(sqlInscricoes.replace('?', '$1'), [outro])).rows.length, 0)
    const agenda = (await db.query(sqlAgenda.replace('?', '$1'), [usuario])).rows
    assert.equal(agenda.length, 1)
    assert.equal(agenda[0].id, atividade)
    assert.ok(agenda[0].registrada_em)
    assert.equal((await db.query(sqlAgenda.replace('?', '$1'), [outro])).rows.length, 0)
    for (const sql of sqls) {
      let indice = 0
      const parametrizado = sql.replace(/\?/g, () => '$' + ++indice)
      await db.query('EXPLAIN ' + parametrizado, Array(indice).fill(null))
    }
    const limiteSql = sqls.find(sql => sql.includes('INSERT INTO limites_presenca'))
    let parametro = 0
    const limiteParametrizado = limiteSql.replace(/\?/g, () => '$' + ++parametro)
    for (let i = 1; i <= 11; i++) {
      const resposta = await db.query(limiteParametrizado, [usuario, 'CONFIRMAR'])
      assert.equal(resposta.rows[0].tentativas, i)
    }
    await db.query("UPDATE limites_presenca SET janela_em=CURRENT_TIMESTAMP-interval '2 minutes' WHERE usuario_id=$1", [usuario])
    assert.equal((await db.query(limiteParametrizado, [usuario, 'CONFIRMAR'])).rows[0].tentativas, 1)
  } finally { await db.close() }
})

test('atualização V1 → V2 preserva registros e não reaplica silenciosamente', async () => {
  const db = new PGlite()
  try {
    await db.exec(await readFile(new URL('src/main/resources/db/migration/V1__cria_modelo_inicial.sql', raiz), 'utf8'))
    const id = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Existente','existente@example.test')", [id])
    await db.exec(await lerEntrada(new URL('db/atualizar-v2.sql', raiz)))
    assert.equal((await db.query('SELECT id FROM usuarios WHERE id=$1', [id])).rows[0].id, id)
    await assert.rejects(db.exec(await lerEntrada(new URL('db/atualizar-v2.sql', raiz))))
    await db.exec('ROLLBACK')
    assert.equal((await db.query('SELECT count(*)::int AS total FROM versoes_schema')).rows[0].total, 2)
  } finally { await db.close() }
})

test('V3 transforma visitantes persistidos em participantes e preserva organizadores/admins', async () => {
  const db = new PGlite()
  try {
    await db.exec(await readFile(new URL('src/main/resources/db/migration/V1__cria_modelo_inicial.sql', raiz), 'utf8'))
    const participante = randomUUID(), organizador = randomUUID(), admin = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'A','a@example.test'),($2,'B','b@example.test'),($3,'C','c@example.test')", [participante, organizador, admin])
    await db.query("INSERT INTO usuario_perfis(usuario_id,perfil) VALUES ($1,'VISITANTE'),($2,'VISITANTE'),($2,'ORGANIZADOR'),($3,'ADMINISTRADOR')", [participante, organizador, admin])
    await db.exec(await lerEntrada(new URL('db/atualizar-v2.sql', raiz)))
    await db.exec(await lerEntrada(new URL('db/atualizar-v3.sql', raiz)))
    assert.deepEqual((await db.query('SELECT usuario_id,perfil FROM usuario_perfis ORDER BY perfil')).rows,
      [{ usuario_id: admin, perfil: 'ADMINISTRADOR' }, { usuario_id: organizador, perfil: 'ORGANIZADOR' }, { usuario_id: participante, perfil: 'PARTICIPANTE' }])
    await assert.rejects(db.query("INSERT INTO usuario_perfis(usuario_id,perfil) VALUES ($1,'VISITANTE')", [participante]), { code: '23514' })
    assert.equal((await db.query('SELECT max(versao) AS versao FROM versoes_schema')).rows[0].versao, 3)
  } finally { await db.close() }
})
