\set ON_ERROR_STOP on
BEGIN;
\ir ../src/main/resources/db/migration/V9a__administracao_e_bloqueio_eventos.sql
COMMIT;
