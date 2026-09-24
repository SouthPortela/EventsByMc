package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.port.out.AvaliacaoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosAvaliacao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class JdbcAvaliacaoRepository implements AvaliacaoRepository {
    private final JdbcConnectionFactory conexoes;
    private final ObjectMapper json = new ObjectMapper();
    public JdbcAvaliacaoRepository(JdbcConnectionFactory conexoes) { this.conexoes = conexoes; }

    private static RuntimeException erro(SQLException e) {
        if ("23505".equals(e.getSQLState())) return new ConflitoOperacaoException("Questionário ou resposta já registrada.");
        return new IllegalStateException("Falha ao acessar avaliações.", e);
    }

    private List<String> opcoes(String valor) {
        if (valor == null) return null;
        try { return json.readValue(valor, new TypeReference<List<String>>() {}); }
        catch (Exception e) { throw new IllegalStateException("Opções do questionário inválidas.", e); }
    }

    private DadosAvaliacao.Questionario consultar(Connection c, UUID eventoId, UUID atividadeId) throws SQLException {
        String sql = "SELECT id, titulo, ativo FROM questionarios WHERE evento_id = ? AND atividade_id IS NOT DISTINCT FROM ?";
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId);
            s.setObject(2, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) return null;
                UUID id = r.getObject("id", UUID.class);
                return new DadosAvaliacao.Questionario(id, eventoId, atividadeId, r.getString("titulo"),
                        r.getBoolean("ativo"), questoes(c, id));
            }
        }
    }

    private List<DadosAvaliacao.Questao> questoes(Connection c, UUID questionarioId) throws SQLException {
        var lista = new ArrayList<DadosAvaliacao.Questao>();
        String sql = "SELECT id, ordem, enunciado, tipo, opcoes::text AS opcoes, escala_minima, escala_maxima, obrigatoria FROM questoes WHERE questionario_id = ? ORDER BY ordem";
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, questionarioId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosAvaliacao.Questao(r.getObject("id", UUID.class),
                        r.getInt("ordem"), r.getString("enunciado"), r.getString("tipo"),
                        opcoes(r.getString("opcoes")), (Integer) r.getObject("escala_minima"),
                        (Integer) r.getObject("escala_maxima"), r.getBoolean("obrigatoria")));
            }
        }
        return List.copyOf(lista);
    }

    public DadosAvaliacao.Questionario consultar(UUID eventoId) {
        return consultarAtividade(eventoId, null);
    }
    public DadosAvaliacao.Questionario consultarAtividade(UUID eventoId, UUID atividadeId) {
        try (var c = conexoes.abrirConexao()) { return consultar(c, eventoId, atividadeId); }
        catch (SQLException e) { throw erro(e); }
    }

    public UUID eventoDaAtividade(UUID atividadeId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement("SELECT evento_id FROM atividades WHERE id = ?")) {
            s.setObject(1, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return r.getObject(1, UUID.class);
            }
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosAvaliacao.Questionario criar(UUID eventoId, String titulo) {
        return criarAtividade(eventoId, null, titulo);
    }
    public DadosAvaliacao.Questionario criarAtividade(UUID eventoId, UUID atividadeId, String titulo) {
        String sql = "INSERT INTO questionarios(id, evento_id, atividade_id, titulo) VALUES (?, ?, ?, ?)";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, UUID.randomUUID()); s.setObject(2, eventoId);
            s.setObject(3, atividadeId); s.setString(4, titulo);
            s.executeUpdate(); return consultar(c, eventoId, atividadeId);
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosAvaliacao.Questao adicionarQuestao(UUID eventoId, DadosAvaliacao.NovaQuestao nova) {
        return adicionarQuestaoAtividade(eventoId, null, nova);
    }
    public DadosAvaliacao.Questao adicionarQuestaoAtividade(UUID eventoId, UUID atividadeId, DadosAvaliacao.NovaQuestao nova) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                UUID questionarioId;
                try (var s = c.prepareStatement("SELECT id, ativo FROM questionarios WHERE evento_id = ? AND atividade_id IS NOT DISTINCT FROM ? FOR UPDATE")) {
                    s.setObject(1, eventoId);
                    s.setObject(2, atividadeId);
                    try (var r = s.executeQuery()) {
                        if (!r.next()) throw new RecursoNaoEncontradoException();
                        if (r.getBoolean("ativo")) throw new ConflitoOperacaoException("Questionário publicado não pode ser alterado.");
                        questionarioId = r.getObject("id", UUID.class);
                    }
                }
                int ordem;
                try (var s = c.prepareStatement("SELECT COALESCE(MAX(ordem), 0) + 1 FROM questoes WHERE questionario_id = ?")) {
                    s.setObject(1, questionarioId);
                    try (var r = s.executeQuery()) { r.next(); ordem = r.getInt(1); }
                }
                if (ordem > 100) throw new ConflitoOperacaoException("Limite de 100 questões atingido.");
                UUID id = UUID.randomUUID();
                String sql = "INSERT INTO questoes(id, questionario_id, ordem, enunciado, tipo, opcoes, escala_minima, escala_maxima, obrigatoria) VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?)";
                try (var s = c.prepareStatement(sql)) {
                    s.setObject(1, id); s.setObject(2, questionarioId); s.setInt(3, ordem);
                    s.setString(4, nova.enunciado().trim()); s.setString(5, nova.tipo());
                    s.setString(6, nova.opcoes() == null ? null : json.writeValueAsString(nova.opcoes()));
                    s.setObject(7, nova.escalaMinima()); s.setObject(8, nova.escalaMaxima());
                    s.setBoolean(9, nova.obrigatoria() == null || nova.obrigatoria());
                    s.executeUpdate();
                }
                c.commit();
                return new DadosAvaliacao.Questao(id, ordem, nova.enunciado().trim(), nova.tipo(),
                        nova.opcoes(), nova.escalaMinima(), nova.escalaMaxima(),
                        nova.obrigatoria() == null || nova.obrigatoria());
            } catch (Exception e) {
                c.rollback();
                if (e instanceof SQLException sql) throw erro(sql);
                if (e instanceof RuntimeException runtime) throw runtime;
                throw new IllegalStateException("Falha ao gravar questão.", e);
            }
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosAvaliacao.Questionario publicar(UUID eventoId) {
        return publicarAtividade(eventoId, null);
    }
    public DadosAvaliacao.Questionario publicarAtividade(UUID eventoId, UUID atividadeId) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                var questionario = consultar(c, eventoId, atividadeId);
                if (questionario == null) throw new RecursoNaoEncontradoException();
                if (questionario.questoes().isEmpty()) throw new ConflitoOperacaoException("Adicione uma questão antes de publicar.");
                try (var s = c.prepareStatement("UPDATE questionarios SET ativo = TRUE WHERE id = ?")) {
                    s.setObject(1, questionario.id()); s.executeUpdate();
                }
                c.commit(); return consultar(c, eventoId, atividadeId);
            } catch (SQLException | RuntimeException e) {
                c.rollback(); if (e instanceof SQLException sql) throw erro(sql); throw e;
            }
        } catch (SQLException e) { throw erro(e); }
    }

    public UUID responder(UUID eventoId, UUID usuarioId, List<DadosAvaliacao.Resposta> respostas) {
        return responderAtividade(eventoId, null, usuarioId, respostas);
    }
    public UUID responderAtividade(UUID eventoId, UUID atividadeId, UUID usuarioId, List<DadosAvaliacao.Resposta> respostas) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                var questionario = consultar(c, eventoId, atividadeId);
                if (questionario == null || !questionario.ativo()) throw new RecursoNaoEncontradoException();
                if (!elegivel(c, eventoId, atividadeId, usuarioId))
                    throw new ConflitoOperacaoException("A avaliação exige inscrição ativa e presença confirmada no evento.");
                var valores = validar(questionario.questoes(), respostas);
                UUID id = UUID.randomUUID();
                try (var s = c.prepareStatement("INSERT INTO avaliacoes(id, questionario_id, usuario_id) VALUES (?, ?, ?)")) {
                    s.setObject(1, id); s.setObject(2, questionario.id()); s.setObject(3, usuarioId); s.executeUpdate();
                }
                try (var s = c.prepareStatement("INSERT INTO respostas_avaliacao(avaliacao_id, questionario_id, questao_id, valor) VALUES (?, ?, ?, ?)")) {
                    for (var valor : valores.entrySet()) {
                        s.setObject(1, id); s.setObject(2, questionario.id());
                        s.setObject(3, valor.getKey()); s.setString(4, valor.getValue()); s.addBatch();
                    }
                    s.executeBatch();
                }
                c.commit(); return id;
            } catch (SQLException | RuntimeException e) {
                c.rollback(); if (e instanceof SQLException sql) throw erro(sql); throw e;
            }
        } catch (SQLException e) { throw erro(e); }
    }

    private static boolean elegivel(Connection c, UUID eventoId, UUID atividadeId, UUID usuarioId) throws SQLException {
        String sql = """
                SELECT 1 FROM inscricoes i WHERE i.evento_id = ? AND i.usuario_id = ?
                  AND i.estado = 'ATIVA' AND EXISTS (
                    SELECT 1 FROM presencas p JOIN atividades at ON at.id = p.atividade_id
                     WHERE p.evento_id = i.evento_id AND p.usuario_id = i.usuario_id
                       AND at.politica_frequencia = 'CHECKIN_UNICO'
                       AND (CAST(? AS UUID) IS NULL OR p.atividade_id = ?)
                    UNION ALL
                    SELECT 1 FROM registros_frequencia rf JOIN atividades at ON at.id = rf.atividade_id
                     WHERE rf.evento_id = i.evento_id AND rf.usuario_id = i.usuario_id
                       AND (CAST(? AS UUID) IS NULL OR rf.atividade_id = ?)
                       AND ((at.politica_frequencia = 'VALIDACAO_MANUAL' AND rf.marcacao = 'CONFIRMACAO')
                         OR (at.politica_frequencia IN ('ENTRADA_SAIDA', 'PERCENTUAL_PERMANENCIA')
                           AND rf.marcacao = 'SAIDA' AND EXISTS (
                             SELECT 1 FROM registros_frequencia entrada
                              WHERE entrada.atividade_id = rf.atividade_id
                                AND entrada.usuario_id = rf.usuario_id AND entrada.marcacao = 'ENTRADA'
                                AND rf.registrada_em > entrada.registrada_em
                                AND (at.politica_frequencia = 'ENTRADA_SAIDA'
                                  OR EXTRACT(EPOCH FROM (rf.registrada_em - entrada.registrada_em))
                                    >= EXTRACT(EPOCH FROM (at.fim - at.inicio))
                                       * at.permanencia_minima_percentual / 100.0))))
                )
                """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId); s.setObject(2, usuarioId);
            s.setObject(3, atividadeId); s.setObject(4, atividadeId);
            s.setObject(5, atividadeId); s.setObject(6, atividadeId);
            try (var r = s.executeQuery()) { return r.next(); }
        }
    }

    private static Map<UUID, String> validar(List<DadosAvaliacao.Questao> questoes,
                                              List<DadosAvaliacao.Resposta> respostas) {
        var porId = new HashMap<UUID, DadosAvaliacao.Questao>();
        for (var q : questoes) porId.put(q.id(), q);
        var valores = new LinkedHashMap<UUID, String>();
        for (var resposta : respostas) {
            if (resposta == null || resposta.questaoId() == null || !porId.containsKey(resposta.questaoId())
                    || valores.containsKey(resposta.questaoId())) throw new IllegalArgumentException("Questão inválida ou repetida.");
            var q = porId.get(resposta.questaoId());
            String valor = resposta.valor() == null ? "" : resposta.valor().trim();
            if (valor.isEmpty()) throw new IllegalArgumentException("Resposta vazia.");
            switch (q.tipo()) {
                case "TEXTO" -> { if (valor.length() > 2000) throw new IllegalArgumentException("Texto muito longo."); }
                case "ESCOLHA_UNICA" -> { if (!q.opcoes().contains(valor)) throw new IllegalArgumentException("Opção inválida."); }
                case "ESCALA" -> {
                    try { int numero = Integer.parseInt(valor);
                        if (numero < q.escalaMinima() || numero > q.escalaMaxima()) throw new IllegalArgumentException("Nota fora da escala.");
                    } catch (NumberFormatException e) { throw new IllegalArgumentException("Nota inválida."); }
                }
                default -> throw new IllegalArgumentException("Tipo inválido.");
            }
            valores.put(q.id(), valor);
        }
        for (var q : questoes) if (q.obrigatoria() && !valores.containsKey(q.id()))
            throw new IllegalArgumentException("Responda todas as questões obrigatórias.");
        return valores;
    }

    public boolean respondeu(UUID eventoId, UUID usuarioId) {
        return respondeuAtividade(eventoId, null, usuarioId);
    }
    public boolean respondeuAtividade(UUID eventoId, UUID atividadeId, UUID usuarioId) {
        String sql = "SELECT 1 FROM avaliacoes a JOIN questionarios q ON q.id = a.questionario_id WHERE q.evento_id = ? AND q.atividade_id IS NOT DISTINCT FROM ? AND a.usuario_id = ?";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId); s.setObject(2, atividadeId); s.setObject(3, usuarioId);
            try (var r = s.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosAvaliacao.Resultados resultados(UUID eventoId) {
        return resultadosAtividade(eventoId, null);
    }
    public DadosAvaliacao.Resultados resultadosAtividade(UUID eventoId, UUID atividadeId) {
        try (var c = conexoes.abrirConexao()) {
            var questionario = consultar(c, eventoId, atividadeId);
            if (questionario == null) throw new RecursoNaoEncontradoException();
            long total;
            try (var s = c.prepareStatement("SELECT COUNT(*) FROM avaliacoes WHERE questionario_id = ?")) {
                s.setObject(1, questionario.id()); try (var r = s.executeQuery()) { r.next(); total = r.getLong(1); }
            }
            var distribuicoes = new HashMap<UUID, Map<String, Long>>();
            var textos = new HashMap<UUID, List<String>>();
            try (var s = c.prepareStatement("SELECT questao_id, valor FROM respostas_avaliacao WHERE questionario_id = ?")) {
                s.setObject(1, questionario.id());
                try (var r = s.executeQuery()) {
                    while (r.next()) {
                        UUID id = r.getObject("questao_id", UUID.class);
                        String valor = r.getString("valor");
                        if (questionario.questoes().stream().anyMatch(q -> q.id().equals(id) && q.tipo().equals("TEXTO")))
                            textos.computeIfAbsent(id, ignorado -> new ArrayList<>()).add(valor);
                        else distribuicoes.computeIfAbsent(id, ignorado -> new LinkedHashMap<>()).merge(valor, 1L, Long::sum);
                    }
                }
            }
            var lista = new ArrayList<DadosAvaliacao.ResultadoQuestao>();
            for (var q : questionario.questoes()) lista.add(new DadosAvaliacao.ResultadoQuestao(q.id(),
                    q.enunciado(), q.tipo(), Map.copyOf(distribuicoes.getOrDefault(q.id(), Map.of())),
                    List.copyOf(textos.getOrDefault(q.id(), List.of()))));
            return new DadosAvaliacao.Resultados(total, List.copyOf(lista));
        } catch (SQLException e) { throw erro(e); }
    }
}
