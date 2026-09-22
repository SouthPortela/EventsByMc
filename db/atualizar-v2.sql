-- Entrada para psql, somente quando V1 já foi aplicada e V2 ainda não.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path TO public;
\ir ../src/main/resources/db/migration/V2__inscricoes_chamadas_presencas.sql
COMMIT;
