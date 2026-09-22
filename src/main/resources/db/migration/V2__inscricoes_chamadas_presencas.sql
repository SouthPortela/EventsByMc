-- Aplicar uma única vez, depois de V1, dentro de uma transação.
-- Não remove tabelas nem dados. Inconsistências existentes devem ser corrigidas antes.
ALTER TABLE eventos ADD CONSTRAINT ck_eventos_periodo_completo
    CHECK ((inicio IS NULL AND fim IS NULL) OR (inicio IS NOT NULL AND fim IS NOT NULL AND fim > inicio));
ALTER TABLE atividades ADD CONSTRAINT ck_atividades_periodo_completo
    CHECK ((inicio IS NULL AND fim IS NULL) OR (inicio IS NOT NULL AND fim IS NOT NULL AND fim > inicio));
ALTER TABLE atividades ADD CONSTRAINT uq_atividades_id_evento UNIQUE (id, evento_id);
CREATE INDEX idx_eventos_publicados_inicio ON eventos(inicio, id) WHERE estado = 'PUBLICADO';

CREATE TABLE inscricoes (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    estado VARCHAR(15) NOT NULL DEFAULT 'ATIVA' CHECK (estado IN ('ATIVA', 'CANCELADA')),
    criada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelada_em TIMESTAMPTZ,
    CONSTRAINT uq_inscricao_evento_usuario UNIQUE (evento_id, usuario_id),
    CONSTRAINT uq_inscricao_vinculo UNIQUE (id, evento_id, usuario_id),
    CONSTRAINT ck_inscricao_cancelamento CHECK (
        (estado = 'ATIVA' AND cancelada_em IS NULL) OR
        (estado = 'CANCELADA' AND cancelada_em IS NOT NULL AND cancelada_em >= criada_em)
    )
);
CREATE INDEX idx_inscricoes_usuario ON inscricoes(usuario_id, criada_em DESC);

CREATE TABLE chamadas_presenca (
    id UUID PRIMARY KEY,
    atividade_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    criada_por UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    codigo_hash CHAR(64) NOT NULL UNIQUE CHECK (codigo_hash ~ '^[0-9a-f]{64}$'),
    criada_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    revogada_em TIMESTAMPTZ,
    FOREIGN KEY (atividade_id, evento_id) REFERENCES atividades(id, evento_id) ON DELETE RESTRICT,
    CONSTRAINT uq_chamada_vinculo UNIQUE (id, atividade_id, evento_id),
    CONSTRAINT ck_chamada_validade CHECK (expira_em = criada_em + INTERVAL '5 minutes'),
    CONSTRAINT ck_chamada_revogacao CHECK (revogada_em IS NULL OR revogada_em >= criada_em)
);
-- A geração de outra chamada revoga a anterior na mesma transação.
CREATE UNIQUE INDEX uq_chamada_aberta_atividade ON chamadas_presenca(atividade_id)
    WHERE revogada_em IS NULL;
CREATE INDEX idx_chamadas_evento ON chamadas_presenca(evento_id);
CREATE INDEX idx_chamadas_criador ON chamadas_presenca(criada_por);

CREATE TABLE presencas (
    id UUID PRIMARY KEY,
    atividade_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    inscricao_id UUID NOT NULL,
    chamada_id UUID NOT NULL,
    registrada_em TIMESTAMPTZ NOT NULL,
    origem VARCHAR(10) NOT NULL CHECK (origem IN ('QR', 'CODIGO')),
    CONSTRAINT uq_presenca_atividade_usuario UNIQUE (atividade_id, usuario_id),
    FOREIGN KEY (inscricao_id, evento_id, usuario_id)
        REFERENCES inscricoes(id, evento_id, usuario_id) ON DELETE RESTRICT,
    FOREIGN KEY (chamada_id, atividade_id, evento_id)
        REFERENCES chamadas_presenca(id, atividade_id, evento_id) ON DELETE RESTRICT
);
CREATE INDEX idx_presencas_usuario ON presencas(usuario_id, registrada_em DESC);
CREATE INDEX idx_presencas_inscricao ON presencas(inscricao_id);
CREATE INDEX idx_presencas_chamada ON presencas(chamada_id);
CREATE INDEX idx_presencas_evento ON presencas(evento_id);

-- Limite por conta, persistente e compartilhado entre instâncias da API.
CREATE TABLE limites_presenca (
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    operacao VARCHAR(10) NOT NULL CHECK (operacao IN ('GERAR', 'CONFIRMAR')),
    janela_em TIMESTAMPTZ NOT NULL,
    tentativas INTEGER NOT NULL CHECK (tentativas > 0),
    PRIMARY KEY (usuario_id, operacao)
);

CREATE TABLE versoes_schema (
    versao INTEGER PRIMARY KEY,
    descricao VARCHAR(200) NOT NULL,
    aplicada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO versoes_schema(versao, descricao) VALUES
    (1, 'Modelo inicial: usuarios, perfis, eventos e atividades'),
    (2, 'Inscricoes, chamadas temporarias e presencas por atividade');

COMMENT ON TABLE chamadas_presenca IS 'Código compartilhado da atividade. Apenas o hash é persistido; não é token de login.';
COMMENT ON TABLE presencas IS 'Uma confirmação por participante e atividade; QR e digitação usam as mesmas validações.';
