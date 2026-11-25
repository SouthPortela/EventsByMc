-- Versao logica 10. O nome V9a ordena apos V9 no init SQL alfabetico do Docker.
ALTER TABLE eventos DROP CONSTRAINT ck_eventos_estado;
ALTER TABLE eventos ADD CONSTRAINT ck_eventos_estado
    CHECK (estado IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'BLOQUEADO'));
ALTER TABLE eventos ADD COLUMN motivo_bloqueio VARCHAR(2000);
ALTER TABLE eventos ADD COLUMN estado_anterior_bloqueio VARCHAR(20);
ALTER TABLE eventos ADD CONSTRAINT ck_evento_bloqueio CHECK (
    (estado = 'BLOQUEADO' AND motivo_bloqueio IS NOT NULL AND length(trim(motivo_bloqueio)) > 0
     AND estado_anterior_bloqueio IS NOT NULL AND estado_anterior_bloqueio IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO'))
    OR (estado <> 'BLOQUEADO' AND motivo_bloqueio IS NULL AND estado_anterior_bloqueio IS NULL)
);

CREATE TABLE auditoria_administrativa (
    id UUID PRIMARY KEY,
    responsavel_id UUID NOT NULL REFERENCES usuarios(id),
    acao VARCHAR(60) NOT NULL,
    recurso_id UUID NOT NULL,
    detalhe VARCHAR(2000) NOT NULL,
    registrado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_auditoria_administrativa_data ON auditoria_administrativa(registrado_em DESC);
INSERT INTO versoes_schema(versao, descricao) VALUES (10, 'Administracao e bloqueio de eventos com motivo');
