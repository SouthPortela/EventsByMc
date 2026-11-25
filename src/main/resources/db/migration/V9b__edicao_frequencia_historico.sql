-- Versão lógica 11. Preserva registros originais e consolidação de eventos encerrados.
CREATE TABLE ajustes_frequencia (
    id UUID PRIMARY KEY,
    ordem BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    atividade_id UUID NOT NULL,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    inscricao_id UUID NOT NULL,
    confirmada BOOLEAN NOT NULL,
    motivo VARCHAR(1000) NOT NULL CHECK (length(trim(motivo)) BETWEEN 5 AND 1000),
    responsavel_id UUID NOT NULL REFERENCES usuarios(id),
    registrada_em TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    FOREIGN KEY (atividade_id, evento_id) REFERENCES atividades(id, evento_id),
    FOREIGN KEY (inscricao_id, evento_id, usuario_id) REFERENCES inscricoes(id, evento_id, usuario_id)
);
CREATE INDEX idx_ajustes_participante ON ajustes_frequencia(atividade_id, usuario_id, ordem DESC);
CREATE TABLE historico_eventos (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES eventos(id),
    responsavel_id UUID REFERENCES usuarios(id),
    registrada_em TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    antes JSONB NOT NULL,
    depois JSONB NOT NULL
);
CREATE INDEX idx_historico_evento ON historico_eventos(evento_id, id DESC);
CREATE FUNCTION impedir_alteracao_historico() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Histórico imutável: acrescente uma nova retificação.' USING ERRCODE = '23514';
END;
$$;
CREATE TRIGGER ajustes_imutaveis BEFORE UPDATE OR DELETE ON ajustes_frequencia
FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_historico();
CREATE TRIGGER historico_eventos_imutavel BEFORE UPDATE OR DELETE ON historico_eventos
FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_historico();

CREATE FUNCTION proteger_evento_consolidado() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF (OLD.estado = 'ENCERRADO' OR (OLD.estado = 'BLOQUEADO' AND OLD.estado_anterior_bloqueio = 'ENCERRADO'))
       AND ROW(NEW.titulo, NEW.descricao, NEW.inicio, NEW.fim, NEW.local, NEW.categoria, NEW.frequencia_minima_percentual)
           IS DISTINCT FROM ROW(OLD.titulo, OLD.descricao, OLD.inicio, OLD.fim, OLD.local, OLD.categoria, OLD.frequencia_minima_percentual) THEN
        RAISE EXCEPTION 'Evento encerrado preserva dados e regra de frequência.' USING ERRCODE = '23514';
    END IF;
    IF ROW(NEW.inicio, NEW.fim) IS DISTINCT FROM ROW(OLD.inicio, OLD.fim) AND EXISTS (
        SELECT 1 FROM atividades WHERE evento_id = OLD.id AND (inicio < NEW.inicio OR fim > NEW.fim
            OR NEW.inicio IS NULL OR NEW.fim IS NULL)) THEN
        RAISE EXCEPTION 'O período precisa abranger as atividades.' USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER proteger_evento BEFORE UPDATE ON eventos
FOR EACH ROW EXECUTE FUNCTION proteger_evento_consolidado();
CREATE FUNCTION registrar_historico_evento() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    IF to_jsonb(NEW) IS DISTINCT FROM to_jsonb(OLD) THEN
        INSERT INTO historico_eventos(evento_id, responsavel_id, antes, depois)
        VALUES (NEW.id, NULLIF(current_setting('events.responsavel_id', true), '')::UUID, to_jsonb(OLD), to_jsonb(NEW));
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER historico_evento AFTER UPDATE ON eventos
FOR EACH ROW EXECUTE FUNCTION registrar_historico_evento();

-- Serializa a programação do mesmo evento para impedir duas reservas concorrentes
-- do mesmo espaço. Espacos são locais ao evento; o nome textual serve como fallback.
CREATE FUNCTION validar_programacao_espacos() RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE estado_atual TEXT; estado_anterior TEXT; nome_local TEXT;
BEGIN
    SELECT estado, estado_anterior_bloqueio INTO estado_atual, estado_anterior FROM eventos
        WHERE id = NEW.evento_id FOR UPDATE;
    IF estado_atual = 'BLOQUEADO' OR estado_atual = 'ENCERRADO' THEN
        RAISE EXCEPTION 'Programação de evento encerrado ou bloqueado é imutável.' USING ERRCODE = '23514';
    END IF;
    IF TG_OP = 'UPDATE' AND NEW.evento_id IS DISTINCT FROM OLD.evento_id THEN
        RAISE EXCEPTION 'Não é permitido transferir uma atividade.' USING ERRCODE = '23514';
    END IF;
    IF NEW.inicio IS NOT NULL THEN
        IF EXISTS (SELECT 1 FROM eventos e WHERE e.id = NEW.evento_id
                AND (e.inicio IS NULL OR NEW.inicio < e.inicio OR NEW.fim > e.fim)) THEN
            RAISE EXCEPTION 'Atividade fora do período do evento.' USING ERRCODE = '23514';
        END IF;
        nome_local := lower(regexp_replace(trim(COALESCE((SELECT nome FROM espacos WHERE id = NEW.espaco_id), NEW.local)), '[[:space:]]+', ' ', 'g'));
        IF EXISTS (
            SELECT 1 FROM atividades a LEFT JOIN espacos s ON s.id = a.espaco_id
            WHERE a.evento_id = NEW.evento_id AND a.id <> NEW.id
              AND a.inicio < NEW.fim AND NEW.inicio < a.fim
              AND ( (NEW.espaco_id IS NOT NULL AND a.espaco_id = NEW.espaco_id)
                 OR lower(regexp_replace(trim(COALESCE(s.nome, a.local)), '[[:space:]]+', ' ', 'g')) = nome_local)
        ) THEN
            RAISE EXCEPTION 'O espaço já está ocupado nesse horário.' USING ERRCODE = '23514';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER validar_espacos BEFORE INSERT OR UPDATE ON atividades
FOR EACH ROW EXECUTE FUNCTION validar_programacao_espacos();
CREATE FUNCTION impedir_exclusao_atividade_consolidada() RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    PERFORM 1 FROM eventos WHERE id = OLD.evento_id FOR UPDATE;
    IF EXISTS (SELECT 1 FROM eventos WHERE id = OLD.evento_id AND
        (estado = 'ENCERRADO' OR estado = 'BLOQUEADO')) THEN
        RAISE EXCEPTION 'Programação consolidada não pode ser removida.' USING ERRCODE = '23514';
    END IF;
    RETURN OLD;
END;
$$;
CREATE TRIGGER preservar_atividades BEFORE DELETE ON atividades
FOR EACH ROW EXECUTE FUNCTION impedir_exclusao_atividade_consolidada();

-- Fonte única para relatórios, certificados e elegibilidade de avaliação.
CREATE VIEW frequencias_efetivas AS
SELECT i.id AS inscricao_id, i.evento_id, i.usuario_id, a.id AS atividade_id,
       COALESCE(aj.confirmada,
         CASE a.politica_frequencia
           WHEN 'CHECKIN_UNICO' THEN p.id IS NOT NULL
           WHEN 'VALIDACAO_MANUAL' THEN conf.id IS NOT NULL
           WHEN 'ENTRADA_SAIDA' THEN saida.registrada_em > entrada.registrada_em
           WHEN 'PERCENTUAL_PERMANENCIA' THEN saida.registrada_em > entrada.registrada_em
             AND EXTRACT(EPOCH FROM (saida.registrada_em - entrada.registrada_em))
                 >= EXTRACT(EPOCH FROM (a.fim - a.inicio)) * a.permanencia_minima_percentual / 100.0
         END, false) AS confirmada,
       COALESCE(aj.registrada_em, p.registrada_em, conf.registrada_em, saida.registrada_em, entrada.registrada_em) AS registrada_em,
       entrada.registrada_em AS entrada_em, saida.registrada_em AS saida_em,
       aj.id AS ajuste_id, COALESCE(aj.responsavel_id, conf.registrada_por, saida.registrada_por, entrada.registrada_por) AS responsavel_id, aj.motivo,
       CASE WHEN aj.id IS NOT NULL THEN 'AJUSTE_MANUAL'
            WHEN a.politica_frequencia = 'CHECKIN_UNICO' THEN p.origem
            WHEN COALESCE(conf.id, entrada.id, saida.id) IS NOT NULL THEN 'MANUAL' ELSE NULL END AS origem
FROM inscricoes i JOIN atividades a ON a.evento_id = i.evento_id
LEFT JOIN presencas p ON p.atividade_id = a.id AND p.usuario_id = i.usuario_id
LEFT JOIN registros_frequencia conf ON conf.atividade_id = a.id AND conf.usuario_id = i.usuario_id AND conf.marcacao = 'CONFIRMACAO'
LEFT JOIN registros_frequencia entrada ON entrada.atividade_id = a.id AND entrada.usuario_id = i.usuario_id AND entrada.marcacao = 'ENTRADA'
LEFT JOIN registros_frequencia saida ON saida.atividade_id = a.id AND saida.usuario_id = i.usuario_id AND saida.marcacao = 'SAIDA'
LEFT JOIN LATERAL (
    SELECT * FROM ajustes_frequencia j WHERE j.atividade_id = a.id AND j.usuario_id = i.usuario_id
    ORDER BY j.ordem DESC LIMIT 1
) aj ON true;
INSERT INTO versoes_schema(versao, descricao) VALUES
    (11, 'Edicao de cadastro/evento, conflitos de espacos, retificacoes auditadas e historico');
