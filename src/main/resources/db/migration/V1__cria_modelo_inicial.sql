CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE usuario_perfis (
    usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    perfil VARCHAR(30) NOT NULL,
    PRIMARY KEY (usuario_id, perfil),
    CONSTRAINT ck_usuario_perfis_perfil CHECK (perfil IN ('VISITANTE', 'PARTICIPANTE', 'ORGANIZADOR', 'ADMINISTRADOR'))
);

CREATE TABLE eventos (
    id UUID PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    organizador_id UUID NOT NULL REFERENCES usuarios(id),
    inicio TIMESTAMP,
    fim TIMESTAMP,
    local VARCHAR(200),
    estado VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_eventos_periodo CHECK (inicio IS NULL OR fim > inicio),
    CONSTRAINT ck_eventos_estado CHECK (estado IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO'))
);

CREATE INDEX idx_eventos_organizador_id ON eventos(organizador_id);

CREATE TABLE atividades (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    inicio TIMESTAMP,
    fim TIMESTAMP,
    local VARCHAR(200),
    capacidade INTEGER,
    CONSTRAINT ck_atividades_periodo CHECK (inicio IS NULL OR fim > inicio),
    CONSTRAINT ck_atividades_capacidade CHECK (capacidade IS NULL OR capacidade > 0)
);

CREATE INDEX idx_atividades_evento_id ON atividades(evento_id);
