import { test } from 'node:test'
import assert from 'node:assert/strict'
import { readFile, readdir } from 'node:fs/promises'
import { randomUUID } from 'node:crypto'
import { PGlite } from '../../target/sql-validation/node_modules/@electric-sql/pglite/dist/index.js'
const raiz = new URL('../../', import.meta.url)
async function fixture() {
  const db = new PGlite()
  const dir = new URL('src/main/resources/db/migration/', raiz)
  for (const f of (await readdir(dir)).filter(f => f.endsWith('.sql')).sort()) await db.exec(await readFile(new URL(f, dir), 'utf8'))
  const u = randomUUID(), e = randomUUID(), a = randomUUID(), i = randomUUID()
  await db.query("INSERT INTO usuarios(id,nome,email) VALUES ($1,'Participante','teste@example.test')", [u])
  await db.query("INSERT INTO eventos(id,titulo,organizador_id,inicio,fim,estado) VALUES ($1,'Evento original',$2,'2026-11-25 09:00','2026-11-25 18:00','PUBLICADO')", [e,u])
  await db.query("INSERT INTO atividades(id,evento_id,titulo,inicio,fim,local) VALUES ($1,$2,'Atividade','2026-11-25 09:00','2026-11-25 10:00','Sala 1')", [a,e])
  await db.query('INSERT INTO inscricoes(id,evento_id,usuario_id) VALUES ($1,$2,$3)', [i,e,u])
  return { db,u,e,a,i }
}
async function ajustar(f, confirmada, motivo) {
  const id = randomUUID()
  await f.db.query('INSERT INTO ajustes_frequencia(id,atividade_id,evento_id,usuario_id,inscricao_id,confirmada,motivo,responsavel_id) VALUES ($1,$2,$3,$4,$5,$6,$7,$4)', [id,f.a,f.e,f.u,f.i,confirmada,motivo])
  return id
}
test('V11 bloqueia sobreposição no mesmo espaço e permite locais distintos e horários adjacentes', async () => {
  const f = await fixture()
  try {
    const sql = "INSERT INTO atividades(id,evento_id,titulo,inicio,fim,local) VALUES ($1,$2,'Outra',$3,$4,$5)"
    await assert.rejects(f.db.query(sql,[randomUUID(),f.e,'2026-11-25 09:30','2026-11-25 10:30','  SALA  1 ']), { code: '23514' })
    await f.db.query(sql,[randomUUID(),f.e,'2026-11-25 09:30','2026-11-25 10:30','Sala 2'])
    await f.db.query(sql,[randomUUID(),f.e,'2026-11-25 10:00','2026-11-25 11:00','Sala 1'])
    assert.equal((await f.db.query('SELECT count(*)::int AS n FROM atividades')).rows[0].n,3)
  } finally { await f.db.close() }
})
test('V11 mantém presença QR original, aplica a última retificação e impede apagar o histórico', async () => {
  const f = await fixture()
  try {
    const chamada = randomUUID()
    await f.db.query("INSERT INTO chamadas_presenca(id,atividade_id,evento_id,criada_por,codigo_hash,criada_em,expira_em) VALUES ($1,$2,$3,$4,$5,'2026-11-25 12:00Z','2026-11-25 12:05Z')",[chamada,f.a,f.e,f.u,'a'.repeat(64)])
    await f.db.query("INSERT INTO presencas(id,atividade_id,evento_id,usuario_id,inscricao_id,chamada_id,registrada_em,origem) VALUES ($1,$2,$3,$4,$5,$6,'2026-11-25 12:01Z','QR')",[randomUUID(),f.a,f.e,f.u,f.i,chamada])
    assert.equal((await f.db.query('SELECT confirmada FROM frequencias_efetivas')).rows[0].confirmada,true)
    const id = await ajustar(f,false,'Presença registrada por engano')
    assert.equal((await f.db.query('SELECT confirmada FROM frequencias_efetivas')).rows[0].confirmada,false)
    await assert.rejects(f.db.query('DELETE FROM ajustes_frequencia WHERE id=$1',[id]),{ code:'23514' })
    await assert.rejects(f.db.query('UPDATE ajustes_frequencia SET confirmada=true WHERE id=$1',[id]),{ code:'23514' })
    await ajustar(f,true,'Revisão posterior comprovou participação')
    assert.equal((await f.db.query('SELECT confirmada FROM frequencias_efetivas')).rows[0].confirmada,true)
    assert.equal((await f.db.query('SELECT count(*)::int AS n FROM presencas')).rows[0].n,1)
    assert.equal((await f.db.query('SELECT count(*)::int AS n FROM ajustes_frequencia')).rows[0].n,2)
  } finally { await f.db.close() }
})
test('V11 audita edição e preserva dados, programação e frequência mínima após encerramento', async () => {
  const f = await fixture()
  try {
    await f.db.exec('BEGIN')
    await f.db.query("SELECT set_config('events.responsavel_id',$1,true)",[f.u])
    await f.db.query("UPDATE eventos SET titulo='Título atualizado',frequencia_minima_percentual=80 WHERE id=$1",[f.e])
    await f.db.exec('COMMIT')
    const h=(await f.db.query('SELECT * FROM historico_eventos WHERE evento_id=$1',[f.e])).rows[0]
    assert.equal(h.responsavel_id,f.u);assert.equal(h.antes.titulo,'Evento original');assert.equal(h.depois.frequencia_minima_percentual,80)
    await assert.rejects(f.db.query("UPDATE eventos SET fim='2026-11-25 09:30' WHERE id=$1",[f.e]),{code:'23514'})
    await f.db.query("UPDATE eventos SET estado='ENCERRADO' WHERE id=$1",[f.e])
    await assert.rejects(f.db.query('UPDATE eventos SET frequencia_minima_percentual=50 WHERE id=$1',[f.e]),{code:'23514'})
    await assert.rejects(f.db.query("UPDATE atividades SET presenca_obrigatoria=false WHERE id=$1",[f.a]),{code:'23514'})
    await assert.rejects(f.db.query('DELETE FROM historico_eventos WHERE evento_id=$1',[f.e]),{code:'23514'})
    await ajustar(f,true,'Conferência manual após encerramento')
    assert.equal((await f.db.query('SELECT confirmada FROM frequencias_efetivas')).rows[0].confirmada,true)
  } finally { await f.db.close() }
})
test('V11 protege vínculo do ajuste e relatório separa reserva, inscrição e confirmação', async () => {
  const f = await fixture()
  try {
    await assert.rejects(f.db.query('INSERT INTO ajustes_frequencia(id,atividade_id,evento_id,usuario_id,inscricao_id,confirmada,motivo,responsavel_id) VALUES ($1,$2,$3,$4,$5,true,$6,$4)',[randomUUID(),f.a,f.e,f.u,randomUUID(),'Justificativa válida']),{code:'23503'})
    await ajustar(f,true,'Conferência do organizador')
    const fonte=await readFile(new URL('src/main/java/br/com/eventsbymc/eventsapi/adapter/out/jdbc/JdbcRelatoriosRepository.java',raiz),'utf8')
    const sql=fonte.match(/public List<DadosRelatorios.Atividade> porAtividade[\s\S]*?String sql = """([\s\S]*?)"""/)[1].replace('?', '$1')
    const row=(await f.db.query(sql,[f.e])).rows[0]
    assert.equal(row.confirmada,true);assert.equal(row.reservada,false);assert.equal(row.estado,'ATIVA')
    await f.db.query('INSERT INTO agenda_atividades(inscricao_id,evento_id,usuario_id,atividade_id) VALUES ($1,$2,$3,$4)',[f.i,f.e,f.u,f.a])
    assert.equal((await f.db.query(sql,[f.e])).rows[0].reservada,true)
  } finally { await f.db.close() }
})
