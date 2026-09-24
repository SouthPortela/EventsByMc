ALTER TABLE questionarios DROP CONSTRAINT questionarios_evento_id_key;
ALTER TABLE questionarios
    ADD COLUMN atividade_id UUID,
    ADD CONSTRAINT fk_questionarios_atividade
        FOREIGN KEY (atividade_id, evento_id) REFERENCES atividades(id, evento_id) ON DELETE RESTRICT;
CREATE UNIQUE INDEX uq_questionario_evento_geral ON questionarios(evento_id) WHERE atividade_id IS NULL;
CREATE UNIQUE INDEX uq_questionario_atividade ON questionarios(atividade_id) WHERE atividade_id IS NOT NULL;

INSERT INTO versoes_schema(versao, descricao) VALUES
    (9, 'Questionarios por atividade sem perder questionarios gerais existentes');
