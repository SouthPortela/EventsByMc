-- Entrada para psql quando V1 a V4 já estão aplicadas.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V5__programacao_inscricoes_agenda.sql
COMMIT;
