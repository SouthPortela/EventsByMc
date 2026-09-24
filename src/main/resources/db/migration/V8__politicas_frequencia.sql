ALTER TABLE atividades
    ADD COLUMN politica_frequencia VARCHAR(24) NOT NULL DEFAULT 'CHECKIN_UNICO',
    ADD COLUMN permanencia_minima_percentual INTEGER NOT NULL DEFAULT 75,
    ADD CONSTRAINT ck_atividades_politica_frequencia CHECK
        (politica_frequencia IN ('CHECKIN_UNICO', 'VALIDACAO_MANUAL', 'ENTRADA_SAIDA', 'PERCENTUAL_PERMANENCIA')),
    ADD CONSTRAINT ck_atividades_permanencia_minima CHECK
        (permanencia_minima_percentual BETWEEN 1 AND 100);

CREATE TABLE registros_frequencia (
    id UUID PRIMARY KEY,
    atividade_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    inscricao_id UUID NOT NULL,
    marcacao VARCHAR(15) NOT NULL CHECK (marcacao IN ('CONFIRMACAO', 'ENTRADA', 'SAIDA')),
    registrada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    registrada_por UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    FOREIGN KEY (atividade_id, evento_id) REFERENCES atividades(id, evento_id) ON DELETE RESTRICT,
    FOREIGN KEY (inscricao_id, evento_id, usuario_id)
        REFERENCES inscricoes(id, evento_id, usuario_id) ON DELETE RESTRICT,
    CONSTRAINT uq_registros_frequencia UNIQUE (atividade_id, usuario_id, marcacao)
);
CREATE INDEX idx_registros_frequencia_evento ON registros_frequencia(evento_id, atividade_id);

INSERT INTO versoes_schema(versao, descricao) VALUES
    (8, 'Politicas de frequencia por atividade e marcacoes manuais auditaveis');
