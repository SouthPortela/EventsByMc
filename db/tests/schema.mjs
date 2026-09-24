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
    assert.equal((await db.query('SELECT count(*)::int AS total FROM versoes_schema')).rows[0].total, 9)
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

test('V4 preserva eventos anteriores, permite classificação e rejeita categoria desconhecida', async () => {
  const db = new PGlite()
  try {
    await db.exec(await readFile(new URL('src/main/resources/db/migration/V1__cria_modelo_inicial.sql', raiz), 'utf8'))
    await db.exec(await lerEntrada(new URL('db/atualizar-v2.sql', raiz)))
    await db.exec(await lerEntrada(new URL('db/atualizar-v3.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Organizador','organizador@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id) VALUES ($1,'Simpósio',$2)", [evento, usuario])
    await db.exec(await lerEntrada(new URL('db/atualizar-v4.sql', raiz)))
    assert.equal((await db.query('SELECT categoria FROM eventos WHERE id=$1', [evento])).rows[0].categoria, 'OUTROS')
    await db.query('UPDATE eventos SET categoria=$1 WHERE id=$2', ['ACADEMICO', evento])
    assert.equal((await db.query('SELECT categoria FROM eventos WHERE id=$1', [evento])).rows[0].categoria, 'ACADEMICO')
    await assert.rejects(db.query('UPDATE eventos SET categoria=$1 WHERE id=$2', ['INVALIDA', evento]), { code: '23514' })
    assert.equal((await db.query('SELECT max(versao) AS versao FROM versoes_schema')).rows[0].versao, 4)
  } finally { await db.close() }
})

test('V5 vincula trilhas, espaços, pessoas e agenda ao mesmo evento', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), outroEvento = randomUUID()
    const atividade = randomUUID(), inscricao = randomUUID(), trilha = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-v5@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id,estado) VALUES ($1,'Evento A',$3,'PUBLICADO'),($2,'Evento B',$3,'PUBLICADO')", [evento, outroEvento, usuario])
    await db.query('INSERT INTO trilhas(id,evento_id,nome) VALUES ($1,$2,$3)', [trilha, evento, 'Tecnologia'])
    await db.query("INSERT INTO atividades(id,evento_id,titulo,inicio,fim,trilha_id) VALUES ($1,$2,'Palestra','2026-10-10 09:00','2026-10-10 10:00',$3)", [atividade, evento, trilha])
    await db.query('INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)', [inscricao, evento, usuario])
    await db.query('INSERT INTO agenda_atividades(inscricao_id,evento_id,usuario_id,atividade_id) VALUES ($1,$2,$3,$4)', [inscricao, evento, usuario, atividade])
    await assert.rejects(db.query('INSERT INTO agenda_atividades(inscricao_id,evento_id,usuario_id,atividade_id) VALUES ($1,$2,$3,$4)', [inscricao, evento, usuario, atividade]), { code: '23505' })
    await assert.rejects(db.query("UPDATE eventos SET frequencia_minima_percentual=101 WHERE id=$1", [evento]), { code: '23514' })
    await assert.rejects(db.query("UPDATE atividades SET evento_id=$1 WHERE id=$2", [outroEvento, atividade]), { code: '23503' })
    assert.equal((await db.query('SELECT max(versao) AS versao FROM versoes_schema')).rows[0].versao, 9)
  } finally { await db.close() }
})

test('V6 impede respostas duplicadas e vínculos de questões de outro questionário', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), outro = randomUUID()
    const questionario = randomUUID(), outroQuestionario = randomUUID()
    const questao = randomUUID(), avaliacao = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-v6@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id) VALUES ($1,'Evento A',$3),($2,'Evento B',$3)", [evento, outro, usuario])
    await db.query("INSERT INTO questionarios(id,evento_id,titulo) VALUES ($1,$3,'Avaliação A'),($2,$4,'Avaliação B')", [questionario, outroQuestionario, evento, outro])
    await db.query("INSERT INTO questoes(id,questionario_id,ordem,enunciado,tipo,opcoes) VALUES ($1,$2,1,'Gostou?','ESCOLHA_UNICA','[\"Sim\",\"Não\"]'::jsonb)", [questao, questionario])
    await assert.rejects(db.query("INSERT INTO questoes(id,questionario_id,ordem,enunciado,tipo,opcoes) VALUES ($1,$2,2,'Inválida','ESCOLHA_UNICA','[]'::jsonb)", [randomUUID(), questionario]), { code: '23514' })
    await db.query('INSERT INTO avaliacoes(id,questionario_id,usuario_id) VALUES ($1,$2,$3)', [avaliacao, outroQuestionario, usuario])
    await assert.rejects(db.query('INSERT INTO respostas_avaliacao(avaliacao_id,questionario_id,questao_id,valor) VALUES ($1,$2,$3,$4)', [avaliacao, outroQuestionario, questao, 'Sim']), { code: '23503' })
    await assert.rejects(db.query('INSERT INTO avaliacoes(id,questionario_id,usuario_id) VALUES ($1,$2,$3)', [randomUUID(), outroQuestionario, usuario]), { code: '23505' })
  } finally { await db.close() }
})

test('V7 garante emissão única e identidade do destinatário', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), pessoa = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-v7@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id) VALUES ($1,'Evento',$2)", [evento, usuario])
    await db.query("INSERT INTO pessoas_evento(id,evento_id,nome) VALUES ($1,$2,'Palestrante')", [pessoa, evento])
    await db.query("INSERT INTO certificados(id,evento_id,usuario_id,tipo) VALUES ($1,$2,$3,'PARTICIPANTE')", [randomUUID(), evento, usuario])
    await assert.rejects(db.query("INSERT INTO certificados(id,evento_id,usuario_id,tipo) VALUES ($1,$2,$3,'PARTICIPANTE')", [randomUUID(), evento, usuario]), { code: '23505' })
    await assert.rejects(db.query("INSERT INTO certificados(id,evento_id,pessoa_id,tipo) VALUES ($1,$2,$3,'PARTICIPANTE')", [randomUUID(), evento, pessoa]), { code: '23514' })
    await db.query("INSERT INTO certificados(id,evento_id,pessoa_id,tipo) VALUES ($1,$2,$3,'PALESTRANTE')", [randomUUID(), evento, pessoa])
    assert.equal((await db.query('SELECT count(*)::int AS total FROM certificados')).rows[0].total, 2)
  } finally { await db.close() }
})

test('frequência calcula presenças obrigatórias, percentual e mínimo configurado', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), atividadeA = randomUUID(), atividadeB = randomUUID()
    const inscricao = randomUUID(), chamada = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Participante','participante@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id,estado,frequencia_minima_percentual) VALUES ($1,'Evento',$2,'ENCERRADO',50)", [evento, usuario])
    await db.query("INSERT INTO atividades(id,evento_id,titulo) VALUES ($1,$3,'A'),($2,$3,'B')", [atividadeA, atividadeB, evento])
    await db.query('INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)', [inscricao, evento, usuario])
    await db.query("INSERT INTO chamadas_presenca(id,atividade_id,evento_id,criada_por,codigo_hash,criada_em,expira_em) VALUES ($1,$2,$3,$4,$5,'2026-10-10 12:00Z','2026-10-10 12:05Z')", [chamada, atividadeA, evento, usuario, 'a'.repeat(64)])
    await db.query("INSERT INTO presencas(id,atividade_id,evento_id,usuario_id,inscricao_id,chamada_id,registrada_em,origem) VALUES ($1,$2,$3,$4,$5,$6,'2026-10-10 12:01Z','QR')", [randomUUID(), atividadeA, evento, usuario, inscricao, chamada])
    const fonte = await readFile(new URL('src/main/java/br/com/eventsbymc/eventsapi/adapter/out/jdbc/JdbcRelatoriosRepository.java', raiz), 'utf8')
    const sql = fonte.match(/FREQUENCIA = """([\s\S]*?)"""/)[1].replace('?', '$1')
    const row = (await db.query(sql, [evento])).rows[0]
    assert.equal(Number(row.obrigatorias), 2)
    assert.equal(Number(row.confirmadas), 1)
    assert.equal(row.frequencia_minima_percentual, 50)
  } finally { await db.close() }
})

test('V8 calcula permanência por atividade e impede marcação duplicada', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), atividade = randomUUID(), inscricao = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-v8@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id,estado) VALUES ($1,'Evento',$2,'ENCERRADO')", [evento, usuario])
    await db.query("INSERT INTO atividades(id,evento_id,titulo,inicio,fim,politica_frequencia,permanencia_minima_percentual) VALUES ($1,$2,'Oficina','2026-10-10 09:00','2026-10-10 11:00','PERCENTUAL_PERMANENCIA',75)", [atividade, evento])
    await db.query('INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)', [inscricao, evento, usuario])
    const fonte = await readFile(new URL('src/main/java/br/com/eventsbymc/eventsapi/adapter/out/jdbc/JdbcRelatoriosRepository.java', raiz), 'utf8')
    const sql = fonte.match(/FREQUENCIA = """([\s\S]*?)"""/)[1].replace('?', '$1')
    const confirmadas = async () => Number((await db.query(sql, [evento])).rows[0].confirmadas)
    assert.equal(await confirmadas(), 0)
    await db.query("INSERT INTO registros_frequencia(id,atividade_id,evento_id,usuario_id,inscricao_id,marcacao,registrada_em,registrada_por) VALUES ($1,$2,$3,$4,$5,'ENTRADA','2026-10-10 09:00Z',$4)", [randomUUID(), atividade, evento, usuario, inscricao])
    await db.query("INSERT INTO registros_frequencia(id,atividade_id,evento_id,usuario_id,inscricao_id,marcacao,registrada_em,registrada_por) VALUES ($1,$2,$3,$4,$5,'SAIDA','2026-10-10 10:20Z',$4)", [randomUUID(), atividade, evento, usuario, inscricao])
    assert.equal(await confirmadas(), 0)
    await db.query("UPDATE registros_frequencia SET registrada_em='2026-10-10 10:31Z' WHERE atividade_id=$1 AND marcacao='SAIDA'", [atividade])
    assert.equal(await confirmadas(), 1)
    await assert.rejects(db.query("INSERT INTO registros_frequencia(id,atividade_id,evento_id,usuario_id,inscricao_id,marcacao,registrada_por) VALUES ($1,$2,$3,$4,$5,'ENTRADA',$4)", [randomUUID(), atividade, evento, usuario, inscricao]), { code: '23505' })
  } finally { await db.close() }
})

test('V9 aceita questionários distintos por atividade e preserva o geral do evento', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), outroEvento = randomUUID()
    const atividadeA = randomUUID(), atividadeB = randomUUID(), atividadeC = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-v9@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id) VALUES ($1,'Evento A',$3),($2,'Evento B',$3)", [evento, outroEvento, usuario])
    await db.query("INSERT INTO atividades(id,evento_id,titulo) VALUES ($1,$4,'A'),($2,$4,'B'),($3,$4,'C')", [atividadeA, atividadeB, atividadeC, evento])
    await db.query("INSERT INTO questionarios(id,evento_id,titulo) VALUES ($1,$2,'Geral')", [randomUUID(), evento])
    await db.query("INSERT INTO questionarios(id,evento_id,atividade_id,titulo) VALUES ($1,$2,$3,'A')", [randomUUID(), evento, atividadeA])
    await db.query("INSERT INTO questionarios(id,evento_id,atividade_id,titulo) VALUES ($1,$2,$3,'B')", [randomUUID(), evento, atividadeB])
    await assert.rejects(db.query("INSERT INTO questionarios(id,evento_id,atividade_id,titulo) VALUES ($1,$2,$3,'A duplicado')", [randomUUID(), evento, atividadeA]), { code: '23505' })
    await assert.rejects(db.query("INSERT INTO questionarios(id,evento_id,atividade_id,titulo) VALUES ($1,$2,$3,'Evento trocado')", [randomUUID(), outroEvento, atividadeC]), { code: '23503' })
    assert.equal((await db.query('SELECT count(*)::int AS total FROM questionarios')).rows[0].total, 3)
  } finally { await db.close() }
})

test('avaliação de atividade exige presença na própria atividade', async () => {
  const db = new PGlite()
  try {
    await db.exec(await lerEntrada(new URL('db/inicializar.sql', raiz)))
    const usuario = randomUUID(), evento = randomUUID(), atividadeA = randomUUID(), atividadeB = randomUUID()
    const inscricao = randomUUID(), chamada = randomUUID()
    await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Teste','teste-elegivel@example.test')", [usuario])
    await db.query("INSERT INTO eventos(id,titulo,organizador_id,estado) VALUES ($1,'Evento',$2,'PUBLICADO')", [evento, usuario])
    await db.query("INSERT INTO atividades(id,evento_id,titulo) VALUES ($1,$3,'A'),($2,$3,'B')", [atividadeA, atividadeB, evento])
    await db.query('INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)', [inscricao, evento, usuario])
    await db.query("INSERT INTO chamadas_presenca(id,atividade_id,evento_id,criada_por,codigo_hash,criada_em,expira_em) VALUES ($1,$2,$3,$4,$5,'2026-10-10 12:00Z','2026-10-10 12:05Z')", [chamada, atividadeA, evento, usuario, 'a'.repeat(64)])
    await db.query("INSERT INTO presencas(id,atividade_id,evento_id,usuario_id,inscricao_id,chamada_id,registrada_em,origem) VALUES ($1,$2,$3,$4,$5,$6,'2026-10-10 12:01Z','QR')", [randomUUID(), atividadeA, evento, usuario, inscricao, chamada])
    const fonte = await readFile(new URL('src/main/java/br/com/eventsbymc/eventsapi/adapter/out/jdbc/JdbcAvaliacaoRepository.java', raiz), 'utf8')
    const sql = fonte.match(/private static boolean elegivel\([\s\S]*?String sql = """([\s\S]*?)"""/)[1]
    const parametrizado = (() => { let i = 0; return sql.replace(/\?/g, () => `$${++i}`) })()
    const elegivel = async atividade => (await db.query(parametrizado, [evento, usuario, atividade, atividade, atividade, atividade])).rows.length > 0
    assert.equal(await elegivel(null), true)
    assert.equal(await elegivel(atividadeA), true)
    assert.equal(await elegivel(atividadeB), false)
  } finally { await db.close() }
})
