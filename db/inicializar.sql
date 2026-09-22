-- Entrada para psql, somente em banco vazio. Não cria usuário/senha nem apaga dados.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V1__cria_modelo_inicial.sql
\ir ../src/main/resources/db/migration/V2__inscricoes_chamadas_presencas.sql
COMMIT;
