-- Programação estruturada, regras de inscrição e agenda pessoal.
ALTER TABLE eventos
    ADD COLUMN inscricoes_abertas BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN inscricoes_inicio TIMESTAMPTZ,
    ADD COLUMN inscricoes_fim TIMESTAMPTZ,
    ADD COLUMN limite_inscritos INTEGER,
    ADD COLUMN permitir_cancelamento BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN frequencia_minima_percentual INTEGER NOT NULL DEFAULT 75,
    ADD CONSTRAINT ck_eventos_janela_inscricao CHECK
        (inscricoes_inicio IS NULL OR inscricoes_fim IS NULL OR inscricoes_fim > inscricoes_inicio),
    ADD CONSTRAINT ck_eventos_limite_inscritos CHECK (limite_inscritos IS NULL OR limite_inscritos > 0),
    ADD CONSTRAINT ck_eventos_frequencia_minima CHECK
        (frequencia_minima_percentual BETWEEN 0 AND 100);

CREATE TABLE trilhas (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    nome VARCHAR(120) NOT NULL,
    CONSTRAINT uq_trilhas_evento_nome UNIQUE (evento_id, nome),
    CONSTRAINT uq_trilhas_id_evento UNIQUE (id, evento_id)
);

CREATE TABLE espacos (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER,
    CONSTRAINT uq_espacos_evento_nome UNIQUE (evento_id, nome),
    CONSTRAINT uq_espacos_id_evento UNIQUE (id, evento_id),
    CONSTRAINT ck_espacos_capacidade CHECK (capacidade IS NULL OR capacidade > 0)
);

ALTER TABLE atividades
    ADD COLUMN trilha_id UUID,
    ADD COLUMN espaco_id UUID,
    ADD COLUMN tipo VARCHAR(20) NOT NULL DEFAULT 'PALESTRA',
    ADD COLUMN presenca_obrigatoria BOOLEAN NOT NULL DEFAULT TRUE,
    ADD CONSTRAINT fk_atividades_trilha FOREIGN KEY (trilha_id, evento_id)
        REFERENCES trilhas(id, evento_id) ON DELETE RESTRICT,
    ADD CONSTRAINT fk_atividades_espaco FOREIGN KEY (espaco_id, evento_id)
        REFERENCES espacos(id, evento_id) ON DELETE RESTRICT,
    ADD CONSTRAINT ck_atividades_tipo CHECK
        (tipo IN ('PALESTRA', 'OFICINA', 'APRESENTACAO', 'MESA_REDONDA', 'OUTRO'));

CREATE TABLE pessoas_evento (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255),
    CONSTRAINT uq_pessoas_evento_id_evento UNIQUE (id, evento_id)
);

CREATE TABLE atividade_pessoas (
    atividade_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    pessoa_id UUID NOT NULL,
    papel VARCHAR(20) NOT NULL,
    PRIMARY KEY (atividade_id, pessoa_id, papel),
    FOREIGN KEY (atividade_id, evento_id) REFERENCES atividades(id, evento_id) ON DELETE RESTRICT,
    FOREIGN KEY (pessoa_id, evento_id) REFERENCES pessoas_evento(id, evento_id) ON DELETE RESTRICT,
    CONSTRAINT ck_atividade_pessoas_papel CHECK
        (papel IN ('PALESTRANTE', 'APRESENTADOR', 'RESPONSAVEL'))
);

CREATE TABLE agenda_atividades (
    inscricao_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    atividade_id UUID NOT NULL,
    adicionada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, atividade_id),
    FOREIGN KEY (inscricao_id, evento_id, usuario_id)
        REFERENCES inscricoes(id, evento_id, usuario_id) ON DELETE RESTRICT,
    FOREIGN KEY (atividade_id, evento_id)
        REFERENCES atividades(id, evento_id) ON DELETE RESTRICT
);
CREATE INDEX idx_agenda_usuario_data ON agenda_atividades(usuario_id, adicionada_em);

INSERT INTO versoes_schema(versao, descricao) VALUES
    (5, 'Programacao com trilhas, espacos e pessoas; regras de inscricao e agenda');
