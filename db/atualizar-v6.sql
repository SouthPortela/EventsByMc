\set ON_ERROR_STOP on
BEGIN;
\ir ../src/main/resources/db/migration/V6__avaliacoes_e_interacao.sql
COMMIT;
