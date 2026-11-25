-- Complemento dos mocks já aplicados.
-- Inclui inscrições, cancelamentos, agenda, presenças, avaliações e certificados.
-- Atua somente nos participantes e eventos do DemoDataSeeder.
-- Mantém senhas e pode ser executado novamente sem duplicar registros.

BEGIN;

SET LOCAL search_path TO public;

SELECT pg_advisory_xact_lock(20851102);

-- UUID compatível com os mocks originais.
CREATE OR REPLACE FUNCTION pg_temp.demo_uuid(tipo TEXT, numero INTEGER)
RETURNS UUID
LANGUAGE SQL IMMUTABLE STRICT
AS $$
SELECT (
           substr(h, 1, 8) || '-' ||
           substr(h, 9, 4) || '-3' ||
           substr(h, 14, 3) || '-' ||
           substr(
                   '89ab',
                   (strpos('0123456789abcdef', substr(h, 17, 1)) - 1) % 4 + 1,
                   1
           ) ||
           substr(h, 18, 3) || '-' ||
           substr(h, 21, 12)
           )::UUID
FROM (
         SELECT md5('EventsByMc/demo/v1/' || tipo || '/' || numero) AS h
     ) AS dados;
$$;

-- IDs estáveis para os novos registros.
CREATE OR REPLACE FUNCTION pg_temp.mock_id(tipo TEXT, chave TEXT)
RETURNS UUID
LANGUAGE SQL IMMUTABLE STRICT
AS $$
SELECT md5('EventsByMc/demo/complemento/v1/' || tipo || '/' || chave)::UUID;
$$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM versoes_schema WHERE versao = 10) THEN
        RAISE EXCEPTION 'O banco precisa estar atualizado até a versão 10.';
END IF;
END;
$$;

CREATE TEMP TABLE mock_usuarios ON COMMIT DROP AS
SELECT n AS numero, u.id
FROM generate_series(1, 100) AS serie(n)
         JOIN usuarios u
              ON u.id = pg_temp.demo_uuid('participante', n)
                  AND u.email = 'demo.participante' || lpad(n::TEXT, 3, '0') || '@example.test';

CREATE TEMP TABLE mock_eventos ON COMMIT DROP AS
SELECT n AS numero, e.*
FROM generate_series(1, 50) AS serie(n)
         JOIN eventos e
              ON e.id = pg_temp.demo_uuid('evento', n)
                  AND e.organizador_id = pg_temp.demo_uuid('organizador', ((n - 1) % 20) + 1)
WHERE e.titulo LIKE '%(Demo)';

DO $$
BEGIN
    IF (SELECT count(*) FROM mock_usuarios) <> 100
       OR (SELECT count(*) FROM mock_eventos) <> 50 THEN
        RAISE EXCEPTION 'Não foram encontrados todos os mocks originais: 100 participantes e 50 eventos.';
END IF;
END;
$$;

-- Garante o perfil dos participantes fictícios.
INSERT INTO usuario_perfis (usuario_id, perfil)
SELECT id, 'PARTICIPANTE'
FROM mock_usuarios
    ON CONFLICT DO NOTHING;

-- Cada participante recebe três eventos encerrados e três publicados.
-- Rascunhos e eventos bloqueados não recebem inscrições.
CREATE TEMP TABLE mock_inscricoes_planejadas ON COMMIT DROP AS
SELECT
    u.numero AS usuario_numero,
    e.numero AS evento_numero,
    u.id AS usuario_id,
    e.id AS evento_id,
    pg_temp.mock_id('inscricao', e.id::TEXT || '/' || u.id::TEXT) AS id,
    CASE
        WHEN (u.numero + e.numero) % 10 = 0 THEN 'CANCELADA'
        ELSE 'ATIVA'
        END AS estado,
    (e.inicio AT TIME ZONE 'America/Sao_Paulo') - INTERVAL '7 days' AS criada_em
FROM mock_usuarios u
    CROSS JOIN generate_series(0, 2) AS sequencia(k)
    JOIN mock_eventos e
ON e.numero = 36 + ((u.numero - 1 + sequencia.k * 3) % 10)
WHERE e.estado = 'ENCERRADO'

UNION ALL

SELECT
    u.numero,
    e.numero,
    u.id,
    e.id,
    pg_temp.mock_id('inscricao', e.id::TEXT || '/' || u.id::TEXT),
    CASE
        WHEN (u.numero + e.numero) % 10 = 0 THEN 'CANCELADA'
        ELSE 'ATIVA'
        END,
    LEAST(
                    CURRENT_TIMESTAMP - INTERVAL '1 day',
                    (e.inicio AT TIME ZONE 'America/Sao_Paulo') - INTERVAL '7 days'
    )
FROM mock_usuarios u
         CROSS JOIN generate_series(0, 2) AS sequencia(k)
         JOIN mock_eventos e
              ON e.numero = 1 + ((u.numero - 1 + sequencia.k * 5) % 15)
WHERE e.estado = 'PUBLICADO';

-- Ajusta a data de cadastro para ser anterior às inscrições.
UPDATE usuarios u
SET criado_em = LEAST(
        u.criado_em,
        datas.primeira_inscricao - INTERVAL '1 day'
                )
    FROM (
    SELECT
        usuario_id,
        min(criada_em AT TIME ZONE 'America/Sao_Paulo') AS primeira_inscricao
    FROM mock_inscricoes_planejadas
    GROUP BY usuario_id
) datas
WHERE u.id = datas.usuario_id;

INSERT INTO inscricoes (
    id, evento_id, usuario_id, estado, criada_em, cancelada_em
)
SELECT
    id,
    evento_id,
    usuario_id,
    estado,
    criada_em,
    CASE
        WHEN estado = 'CANCELADA' THEN criada_em + INTERVAL '12 hours'
END
FROM mock_inscricoes_planejadas
ON CONFLICT (evento_id, usuario_id) DO NOTHING;

-- Usa os IDs efetivos caso já existam inscrições.
CREATE TEMP TABLE mock_vinculos ON COMMIT DROP AS
SELECT
    p.usuario_numero,
    p.evento_numero,
    i.*
FROM mock_inscricoes_planejadas p
         JOIN inscricoes i
              ON i.evento_id = p.evento_id
                  AND i.usuario_id = p.usuario_id;

-- Somente as duas atividades originais de cada evento.
CREATE TEMP TABLE mock_atividades ON COMMIT DROP AS
SELECT
    e.numero AS evento_numero,
    s.posicao,
    a.*
FROM mock_eventos e
         CROSS JOIN generate_series(0, 1) AS s(posicao)
         JOIN atividades a
              ON a.id = pg_temp.demo_uuid('atividade', e.numero * 2 + s.posicao)
                  AND a.evento_id = e.id;

-- Agenda para inscrições ativas.
INSERT INTO agenda_atividades (
    inscricao_id, evento_id, usuario_id, atividade_id, adicionada_em
)
SELECT
    i.id,
    i.evento_id,
    i.usuario_id,
    a.id,
    i.criada_em + INTERVAL '1 hour'
FROM mock_vinculos i
    JOIN mock_atividades a ON a.evento_id = i.evento_id
WHERE i.estado = 'ATIVA'
ON CONFLICT DO NOTHING;

-- Cenários de frequência nos eventos encerrados:
-- grupo 0: nenhuma presença;
-- grupo 1: somente a primeira atividade;
-- grupos 2, 3 e 4: todas as atividades.
CREATE TEMP TABLE mock_presencas_planejadas ON COMMIT DROP AS
SELECT
    i.id AS inscricao_id,
    i.usuario_id,
    i.usuario_numero,
    a.id AS atividade_id,
    a.evento_id,
    a.politica_frequencia,
    a.inicio,
    a.fim,
    e.organizador_id
FROM mock_vinculos i
         JOIN mock_eventos e ON e.id = i.evento_id
         JOIN mock_atividades a ON a.evento_id = e.id
WHERE i.estado = 'ATIVA'
  AND e.estado = 'ENCERRADO'
  AND (e.fim AT TIME ZONE 'America/Sao_Paulo') < CURRENT_TIMESTAMP
  AND (i.usuario_numero + i.evento_numero) % 5 <> 0
  AND (
      (i.usuario_numero + i.evento_numero) % 5 <> 1
      OR a.posicao = 0
  );

-- Chamadas históricas já expiradas e revogadas.
-- Não cria um QR Code utilizável para registrar novas presenças.
INSERT INTO chamadas_presenca (
    id, atividade_id, evento_id, criada_por,
    codigo_hash, criada_em, expira_em, revogada_em
)
SELECT DISTINCT
    pg_temp.mock_id('chamada', p.atividade_id::TEXT),
    p.atividade_id,
    p.evento_id,
    p.organizador_id,
    encode(
            sha256(convert_to('chamada-mock/' || p.atividade_id::TEXT, 'UTF8')),
            'hex'
    ),
    p.inicio AT TIME ZONE 'America/Sao_Paulo',
    (p.inicio AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '5 minutes',
    (p.inicio AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '5 minutes'
FROM mock_presencas_planejadas p
WHERE p.politica_frequencia = 'CHECKIN_UNICO'
ON CONFLICT DO NOTHING;

INSERT INTO presencas (
    id, atividade_id, evento_id, usuario_id,
    inscricao_id, chamada_id, registrada_em, origem
)
SELECT
    pg_temp.mock_id(
            'presenca',
            p.atividade_id::TEXT || '/' || p.usuario_id::TEXT
    ),
    p.atividade_id,
    p.evento_id,
    p.usuario_id,
    p.inscricao_id,
    c.id,
    c.criada_em + INTERVAL '1 minute',
    CASE WHEN p.usuario_numero % 2 = 0 THEN 'QR' ELSE 'CODIGO' END
FROM mock_presencas_planejadas p
JOIN chamadas_presenca c
  ON c.id = pg_temp.mock_id('chamada', p.atividade_id::TEXT)
 AND c.atividade_id = p.atividade_id
 AND c.evento_id = p.evento_id
WHERE p.politica_frequencia = 'CHECKIN_UNICO'
ON CONFLICT DO NOTHING;

-- Também suporta atividades cuja política foi alterada no sistema.
INSERT INTO registros_frequencia (
    id, atividade_id, evento_id, usuario_id, inscricao_id,
    marcacao, registrada_em, registrada_por
)
SELECT
    pg_temp.mock_id(
            'frequencia',
            p.atividade_id::TEXT || '/' || p.usuario_id::TEXT || '/' || m.marcacao
    ),
    p.atividade_id,
    p.evento_id,
    p.usuario_id,
    p.inscricao_id,
    m.marcacao,
    (
        CASE WHEN m.marcacao = 'SAIDA' THEN p.fim ELSE p.inicio END
        ) AT TIME ZONE 'America/Sao_Paulo',
    p.organizador_id
FROM mock_presencas_planejadas p
         CROSS JOIN (
    VALUES ('CONFIRMACAO'), ('ENTRADA'), ('SAIDA')
) AS m(marcacao)
WHERE
    (p.politica_frequencia = 'VALIDACAO_MANUAL'
        AND m.marcacao = 'CONFIRMACAO')
   OR
    (p.politica_frequencia IN ('ENTRADA_SAIDA', 'PERCENTUAL_PERMANENCIA')
        AND m.marcacao IN ('ENTRADA', 'SAIDA'))
    ON CONFLICT DO NOTHING;

-- Consolida presenças conforme as mesmas políticas utilizadas pela aplicação.
CREATE TEMP TABLE mock_confirmacoes ON COMMIT DROP AS
SELECT p.atividade_id, p.usuario_id
FROM presencas p
         JOIN atividades a ON a.id = p.atividade_id
         JOIN mock_vinculos i
              ON i.id = p.inscricao_id
                  AND i.estado = 'ATIVA'
WHERE a.politica_frequencia = 'CHECKIN_UNICO'

UNION

SELECT r.atividade_id, r.usuario_id
FROM registros_frequencia r
         JOIN atividades a ON a.id = r.atividade_id
         JOIN mock_vinculos i
              ON i.id = r.inscricao_id
                  AND i.estado = 'ATIVA'
WHERE a.politica_frequencia = 'VALIDACAO_MANUAL'
  AND r.marcacao = 'CONFIRMACAO'

UNION

SELECT entrada.atividade_id, entrada.usuario_id
FROM registros_frequencia entrada
         JOIN registros_frequencia saida
              ON saida.atividade_id = entrada.atividade_id
                  AND saida.usuario_id = entrada.usuario_id
                  AND saida.marcacao = 'SAIDA'
         JOIN atividades a ON a.id = entrada.atividade_id
         JOIN mock_vinculos i
              ON i.id = entrada.inscricao_id
                  AND i.estado = 'ATIVA'
WHERE entrada.marcacao = 'ENTRADA'
  AND saida.registrada_em > entrada.registrada_em
  AND (
    a.politica_frequencia = 'ENTRADA_SAIDA'
        OR (
        a.politica_frequencia = 'PERCENTUAL_PERMANENCIA'
            AND EXTRACT(EPOCH FROM (saida.registrada_em - entrada.registrada_em))
            >= EXTRACT(EPOCH FROM (a.fim - a.inicio))
                   * a.permanencia_minima_percentual / 100.0
        )
    );

-- A conclusão é calculada pela frequência; não existe coluna "concluído".
CREATE TEMP TABLE mock_resultados ON COMMIT DROP AS
SELECT
    i.id AS inscricao_id,
    i.usuario_id,
    i.evento_id,
    i.estado AS inscricao_estado,
    e.estado AS evento_estado,
    e.fim,
    e.frequencia_minima_percentual,
    count(a.id) FILTER (WHERE a.presenca_obrigatoria) AS obrigatorias,
    count(a.id) FILTER (
        WHERE a.presenca_obrigatoria AND c.usuario_id IS NOT NULL
    ) AS confirmadas
FROM mock_vinculos i
         JOIN mock_eventos e ON e.id = i.evento_id
         LEFT JOIN atividades a ON a.evento_id = e.id
         LEFT JOIN mock_confirmacoes c
                   ON c.atividade_id = a.id
                       AND c.usuario_id = i.usuario_id
GROUP BY
    i.id, i.usuario_id, i.evento_id, i.estado,
    e.estado, e.fim, e.frequencia_minima_percentual;

-- Certificados apenas para participantes aprovados em eventos encerrados.
INSERT INTO certificados (
    id, evento_id, usuario_id, tipo, emitido_em
)
SELECT
    pg_temp.mock_id(
            'certificado',
            r.evento_id::TEXT || '/' || r.usuario_id::TEXT
    ),
    r.evento_id,
    r.usuario_id,
    'PARTICIPANTE',
    (r.fim AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '1 hour'
FROM mock_resultados r
WHERE r.inscricao_estado = 'ATIVA'
  AND r.evento_estado = 'ENCERRADO'
  AND r.obrigatorias > 0
  AND floor(100.0 * r.confirmadas / r.obrigatorias)
    >= r.frequencia_minima_percentual
  AND (r.fim AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '1 hour'
    <= CURRENT_TIMESTAMP
ON CONFLICT DO NOTHING;

-- Questionários gerais dos eventos encerrados.
INSERT INTO questionarios (
    id, evento_id, titulo, ativo, criado_em
)
SELECT
    pg_temp.mock_id('questionario', e.id::TEXT),
    e.id,
    'Avaliação do evento (Demo)',
    TRUE,
    (e.inicio AT TIME ZONE 'America/Sao_Paulo') - INTERVAL '1 day'
FROM mock_eventos e
WHERE e.estado = 'ENCERRADO'
  AND NOT EXISTS (
    SELECT 1
    FROM questionarios q
    WHERE q.evento_id = e.id AND q.atividade_id IS NULL
    )
ON CONFLICT DO NOTHING;

-- Acrescenta questões somente aos questionários deste script.
INSERT INTO questoes (
    id, questionario_id, ordem, enunciado, tipo,
    escala_minima, escala_maxima, obrigatoria
)
SELECT
    pg_temp.mock_id('questao', q.id::TEXT || '/' || s.ordem),
    q.id,
    s.ordem,
    CASE WHEN s.ordem = 1
             THEN 'Qual sua nota para o evento?'
         ELSE 'O que você mais gostou e o que pode melhorar?'
        END,
    CASE WHEN s.ordem = 1 THEN 'ESCALA' ELSE 'TEXTO' END,
    CASE WHEN s.ordem = 1 THEN 1 END,
    CASE WHEN s.ordem = 1 THEN 5 END,
    TRUE
FROM questionarios q
         JOIN mock_eventos e ON e.id = q.evento_id
         CROSS JOIN generate_series(1, 2) AS s(ordem)
WHERE q.id = pg_temp.mock_id('questionario', e.id::TEXT)
    ON CONFLICT DO NOTHING;

-- Respostas somente de inscritos ativos com alguma presença confirmada.
-- Parte dos participantes permanece sem responder.
INSERT INTO avaliacoes (
    id, questionario_id, usuario_id, enviada_em
)
SELECT
    pg_temp.mock_id('avaliacao', q.id::TEXT || '/' || i.usuario_id::TEXT),
    q.id,
    i.usuario_id,
    (e.fim AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '30 minutes'
FROM mock_vinculos i
    JOIN mock_eventos e ON e.id = i.evento_id
    JOIN questionarios q
    ON q.id = pg_temp.mock_id('questionario', e.id::TEXT)
    AND q.evento_id = e.id
WHERE i.estado = 'ATIVA'
  AND q.ativo
  AND e.estado = 'ENCERRADO'
  AND i.usuario_numero % 4 <> 0
  AND (e.fim AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '30 minutes'
    <= CURRENT_TIMESTAMP
  AND EXISTS (
    SELECT 1
    FROM mock_confirmacoes c
    JOIN atividades a ON a.id = c.atividade_id
    WHERE a.evento_id = e.id AND c.usuario_id = i.usuario_id
    )
ON CONFLICT DO NOTHING;

INSERT INTO respostas_avaliacao (
    avaliacao_id, questionario_id, questao_id, valor
)
SELECT
    av.id,
    av.questionario_id,
    q.id,
    CASE WHEN q.ordem = 1
             THEN (3 + (u.numero % 3))::TEXT
         ELSE (ARRAY[
                   'Conteúdo bem explicado e exemplos práticos interessantes.',
               'Boa organização. Sugiro mais tempo para perguntas.',
               'Gostei das atividades práticas e da interação.',
               'Evento proveitoso. Sugiro disponibilizar material complementar.'
                   ])[(u.numero % 4) + 1]
        END
FROM avaliacoes av
         JOIN mock_usuarios u ON u.id = av.usuario_id
         JOIN questoes q ON q.questionario_id = av.questionario_id
WHERE av.id = pg_temp.mock_id(
        'avaliacao',
        av.questionario_id::TEXT || '/' || av.usuario_id::TEXT
              )
  AND q.id = pg_temp.mock_id(
        'questao',
        av.questionario_id::TEXT || '/' || q.ordem
             )
    ON CONFLICT DO NOTHING;

-- Mensagens fictícias para testar a interação nos eventos.
INSERT INTO mensagens_evento (
    id, evento_id, usuario_id, mensagem, criada_em
)
SELECT
    pg_temp.mock_id(
            'mensagem',
            i.evento_id::TEXT || '/' || i.usuario_id::TEXT
    ),
    i.evento_id,
    i.usuario_id,
    CASE WHEN e.estado = 'ENCERRADO'
             THEN 'Obrigado pelo evento! Os exemplos práticos ajudaram bastante. (Demo)'
         ELSE 'Inscrição confirmada! Estou animado para participar. (Demo)'
        END,
    CASE WHEN e.estado = 'ENCERRADO'
             THEN (e.fim AT TIME ZONE 'America/Sao_Paulo') + INTERVAL '15 minutes'
    ELSE i.criada_em + INTERVAL '2 hours'
END
FROM mock_vinculos i
JOIN mock_eventos e ON e.id = i.evento_id
WHERE i.estado = 'ATIVA'
  AND i.usuario_numero % 5 = 0
  AND (
      e.estado = 'PUBLICADO'
      OR (
          e.estado = 'ENCERRADO'
          AND (e.fim AT TIME ZONE 'America/Sao_Paulo')
              + INTERVAL '15 minutes' <= CURRENT_TIMESTAMP
      )
  )
ON CONFLICT DO NOTHING;

-- Resumo das situações calculadas.
SELECT
    CASE
        WHEN inscricao_estado = 'CANCELADA' THEN 'CANCELADA'
        WHEN evento_estado <> 'ENCERRADO' THEN 'EM_ANDAMENTO'
        WHEN obrigatorias > 0
            AND floor(100.0 * confirmadas / obrigatorias)
                 >= frequencia_minima_percentual
            THEN 'APROVADO'
        ELSE 'INSUFICIENTE'
        END AS situacao,
    count(*) AS quantidade
FROM mock_resultados
GROUP BY 1
ORDER BY 1;

COMMIT;