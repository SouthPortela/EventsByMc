-- V10. O prefixo V9z mantém a ordem alfabética do initdb do Docker após V9.
-- Moderação preserva inscrições, presenças e certificados vinculados ao evento.
ALTER TABLE eventos DROP CONSTRAINT ck_eventos_estado;
ALTER TABLE eventos ADD CONSTRAINT ck_eventos_estado
    CHECK (estado IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'SUSPENSO', 'EXCLUIDO'));

CREATE TABLE moderacoes_evento (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    administrador_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    estado_anterior VARCHAR(20) NOT NULL,
    estado_novo VARCHAR(20) NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_moderacoes_estados CHECK (
        estado_anterior IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'SUSPENSO')
        AND estado_novo IN ('RASCUNHO', 'SUSPENSO', 'EXCLUIDO')
    ),
    CONSTRAINT ck_moderacoes_motivo CHECK (char_length(btrim(motivo)) BETWEEN 10 AND 500)
);
CREATE INDEX idx_moderacoes_evento_data ON moderacoes_evento(evento_id, criado_em DESC);

INSERT INTO versoes_schema(versao, descricao)
VALUES (10, 'Moderacao, suspensao e exclusao logica de eventos');
