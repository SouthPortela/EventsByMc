CREATE TABLE certificados (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id) ON DELETE RESTRICT,
    usuario_id UUID REFERENCES usuarios(id) ON DELETE RESTRICT,
    pessoa_id UUID,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PARTICIPANTE', 'PALESTRANTE', 'APRESENTADOR')),
    emitido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_em TIMESTAMPTZ,
    FOREIGN KEY (pessoa_id, evento_id) REFERENCES pessoas_evento(id, evento_id) ON DELETE RESTRICT,
    CONSTRAINT ck_certificados_destinatario CHECK
        ((tipo = 'PARTICIPANTE' AND usuario_id IS NOT NULL AND pessoa_id IS NULL)
         OR (tipo <> 'PARTICIPANTE' AND usuario_id IS NULL AND pessoa_id IS NOT NULL))
);
CREATE UNIQUE INDEX uq_certificado_participante ON certificados(evento_id, usuario_id) WHERE usuario_id IS NOT NULL;
CREATE UNIQUE INDEX uq_certificado_pessoa ON certificados(evento_id, pessoa_id, tipo) WHERE pessoa_id IS NOT NULL;

INSERT INTO versoes_schema(versao, descricao) VALUES
    (7, 'Certificados de participantes e declaracoes de palestrantes/apresentadores');
