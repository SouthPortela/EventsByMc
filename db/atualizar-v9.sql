\set ON_ERROR_STOP on
BEGIN;
\ir ../src/main/resources/db/migration/V9__questionarios_por_atividade.sql
COMMIT;
