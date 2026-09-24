CREATE TABLE questionarios (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL UNIQUE REFERENCES eventos(id) ON DELETE RESTRICT,
    titulo VARCHAR(160) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_questionarios_id_evento UNIQUE (id, evento_id)
);

CREATE TABLE questoes (
    id UUID PRIMARY KEY,
    questionario_id UUID NOT NULL REFERENCES questionarios(id) ON DELETE RESTRICT,
    ordem INTEGER NOT NULL CHECK (ordem > 0),
    enunciado VARCHAR(500) NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('TEXTO', 'ESCOLHA_UNICA', 'ESCALA')),
    opcoes JSONB,
    escala_minima INTEGER,
    escala_maxima INTEGER,
    obrigatoria BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_questoes_ordem UNIQUE (questionario_id, ordem),
    CONSTRAINT uq_questoes_id_questionario UNIQUE (id, questionario_id),
    CONSTRAINT ck_questoes_escala CHECK
        ((tipo = 'ESCALA' AND escala_minima IS NOT NULL AND escala_maxima IS NOT NULL
            AND escala_maxima > escala_minima AND escala_maxima <= 10 AND escala_minima >= 0)
         OR (tipo <> 'ESCALA' AND escala_minima IS NULL AND escala_maxima IS NULL)),
    CONSTRAINT ck_questoes_opcoes CHECK
        ((tipo = 'ESCOLHA_UNICA' AND opcoes IS NOT NULL AND jsonb_typeof(opcoes) = 'array'
            AND jsonb_array_length(opcoes) BETWEEN 2 AND 20)
         OR (tipo <> 'ESCOLHA_UNICA' AND opcoes IS NULL))
);

CREATE TABLE avaliacoes (
    id UUID PRIMARY KEY,
    questionario_id UUID NOT NULL REFERENCES questionarios(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    enviada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_avaliacoes_usuario UNIQUE (questionario_id, usuario_id),
    CONSTRAINT uq_avaliacoes_id_questionario UNIQUE (id, questionario_id)
);

CREATE TABLE respostas_avaliacao (
    avaliacao_id UUID NOT NULL,
    questionario_id UUID NOT NULL,
    questao_id UUID NOT NULL,
    valor TEXT NOT NULL,
    PRIMARY KEY (avaliacao_id, questao_id),
    FOREIGN KEY (avaliacao_id, questionario_id)
        REFERENCES avaliacoes(id, questionario_id) ON DELETE RESTRICT,
    FOREIGN KEY (questao_id, questionario_id)
        REFERENCES questoes(id, questionario_id) ON DELETE RESTRICT
);

CREATE TABLE mensagens_evento (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE RESTRICT,
    mensagem VARCHAR(2000) NOT NULL,
    criada_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_mensagens_evento_data ON mensagens_evento(evento_id, criada_em DESC);

INSERT INTO versoes_schema(versao, descricao) VALUES
    (6, 'Questionarios configuraveis, respostas unicas e interacao entre participantes');
