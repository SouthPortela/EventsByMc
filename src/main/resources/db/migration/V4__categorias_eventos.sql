-- A categoria é dado do evento, não uma inferência feita a partir do banner ou do título.
-- Eventos antigos ficam em OUTROS até o organizador classificá-los no painel.
ALTER TABLE eventos ADD COLUMN categoria VARCHAR(30) NOT NULL DEFAULT 'OUTROS';
ALTER TABLE eventos ADD CONSTRAINT ck_eventos_categoria CHECK
    (categoria IN ('TECNOLOGIA', 'CURSOS_E_WORKSHOPS', 'NEGOCIOS_E_CARREIRAS', 'ACADEMICO', 'OUTROS'));

INSERT INTO versoes_schema(versao, descricao) VALUES
    (4, 'Categoria explicita dos eventos para filtros do catalogo');
