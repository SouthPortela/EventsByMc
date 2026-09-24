\set ON_ERROR_STOP on
BEGIN;
\ir ../src/main/resources/db/migration/V7__certificados.sql
COMMIT;
