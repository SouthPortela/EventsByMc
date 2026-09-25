-- Entrada para psql, somente em banco vazio. Não cria usuário/senha nem apaga dados.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V1__cria_modelo_inicial.sql
\ir ../src/main/resources/db/migration/V2__inscricoes_chamadas_presencas.sql
\ir ../src/main/resources/db/migration/V3__perfis_de_contas_autenticadas.sql
\ir ../src/main/resources/db/migration/V4__categorias_eventos.sql
\ir ../src/main/resources/db/migration/V5__programacao_inscricoes_agenda.sql
\ir ../src/main/resources/db/migration/V6__avaliacoes_e_interacao.sql
\ir ../src/main/resources/db/migration/V7__certificados.sql
\ir ../src/main/resources/db/migration/V8__politicas_frequencia.sql
\ir ../src/main/resources/db/migration/V9__questionarios_por_atividade.sql
\ir ../src/main/resources/db/migration/V9z__V10_moderacao_eventos.sql
COMMIT;
