-- Entrada para psql quando V1, V2 e V3 já estão aplicadas.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V4__categorias_eventos.sql
COMMIT;
