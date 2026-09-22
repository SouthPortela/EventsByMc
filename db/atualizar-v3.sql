-- Entrada para psql quando V1 e V2 já estão aplicadas.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V3__perfis_de_contas_autenticadas.sql
COMMIT;
