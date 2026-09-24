package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.port.out.FrequenciaRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosFrequencia;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JdbcFrequenciaRepository implements FrequenciaRepository {
    private final JdbcConnectionFactory conexoes;
    public JdbcFrequenciaRepository(JdbcConnectionFactory conexoes) { this.conexoes = conexoes; }
    private static RuntimeException erro(SQLException e) {
        if ("23505".equals(e.getSQLState())) return new ConflitoOperacaoException("Marcação já registrada.");
        return new IllegalStateException("Falha ao registrar frequência.", e);
    }
    public UUID eventoDaAtividade(UUID atividadeId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT evento_id FROM atividades WHERE id = ?")) {
            s.setObject(1, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return r.getObject(1, UUID.class);
            }
        } catch (SQLException e) { throw erro(e); }
    }
    private static DadosFrequencia.Registro mapear(ResultSet r) throws SQLException {
        return new DadosFrequencia.Registro(r.getObject("id", UUID.class),
                r.getObject("atividade_id", UUID.class), r.getObject("usuario_id", UUID.class),
                r.getString("marcacao"), r.getTimestamp("registrada_em").toInstant(),
                r.getObject("registrada_por", UUID.class));
    }
    public DadosFrequencia.Registro registrar(UUID atividadeId, UUID usuarioId, UUID responsavelId, String marcacao) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                var contexto = contexto(c, atividadeId, usuarioId);
                if (!"ATIVA".equals(contexto.inscricaoEstado()) || !"PUBLICADO".equals(contexto.eventoEstado()))
                    throw new ConflitoOperacaoException("É necessária inscrição ativa em evento publicado.");
                if (!marcacaoCompativel(contexto.politica(), marcacao))
                    throw new ConflitoOperacaoException("Marcação incompatível com a política da atividade.");
                if ("PERCENTUAL_PERMANENCIA".equals(contexto.politica())
                        && (contexto.inicio() == null || contexto.fim() == null))
                    throw new ConflitoOperacaoException("Atividade precisa de período para calcular permanência.");
                if ("SAIDA".equals(marcacao) && !possuiEntrada(c, atividadeId, usuarioId))
                    throw new ConflitoOperacaoException("Registre a entrada antes da saída.");
                UUID id = UUID.randomUUID();
                try (var s = c.prepareStatement("""
                        INSERT INTO registros_frequencia(id, atividade_id, evento_id, usuario_id,
                            inscricao_id, marcacao, registrada_por)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        RETURNING id, atividade_id, usuario_id, marcacao, registrada_em, registrada_por
                        """)) {
                    s.setObject(1, id); s.setObject(2, atividadeId); s.setObject(3, contexto.eventoId());
                    s.setObject(4, usuarioId); s.setObject(5, contexto.inscricaoId());
                    s.setString(6, marcacao); s.setObject(7, responsavelId);
                    try (var r = s.executeQuery()) { r.next(); var registro = mapear(r); c.commit(); return registro; }
                }
            } catch (SQLException | RuntimeException e) {
                c.rollback(); if (e instanceof SQLException sql) throw erro(sql); throw e;
            }
        } catch (SQLException e) { throw erro(e); }
    }
    private record Contexto(UUID eventoId, UUID inscricaoId, String inscricaoEstado,
                            String eventoEstado, String politica, LocalDateTime inicio, LocalDateTime fim) {}
    private static Contexto contexto(Connection c, UUID atividadeId, UUID usuarioId) throws SQLException {
        String sql = """
                SELECT a.evento_id, a.politica_frequencia, a.inicio, a.fim,
                       e.estado AS evento_estado, i.id AS inscricao_id, i.estado AS inscricao_estado
                  FROM atividades a JOIN eventos e ON e.id = a.evento_id
                  LEFT JOIN inscricoes i ON i.evento_id = a.evento_id AND i.usuario_id = ?
                 WHERE a.id = ? FOR UPDATE OF a
                """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, usuarioId); s.setObject(2, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return new Contexto(r.getObject("evento_id", UUID.class),
                        r.getObject("inscricao_id", UUID.class), r.getString("inscricao_estado"),
                        r.getString("evento_estado"), r.getString("politica_frequencia"),
                        r.getObject("inicio", LocalDateTime.class), r.getObject("fim", LocalDateTime.class));
            }
        }
    }
    private static boolean marcacaoCompativel(String politica, String marcacao) {
        return switch (politica) {
            case "VALIDACAO_MANUAL" -> "CONFIRMACAO".equals(marcacao);
            case "ENTRADA_SAIDA", "PERCENTUAL_PERMANENCIA" -> "ENTRADA".equals(marcacao) || "SAIDA".equals(marcacao);
            default -> false;
        };
    }
    private static boolean possuiEntrada(Connection c, UUID atividadeId, UUID usuarioId) throws SQLException {
        try (var s = c.prepareStatement("SELECT 1 FROM registros_frequencia WHERE atividade_id = ? AND usuario_id = ? AND marcacao = 'ENTRADA'")) {
            s.setObject(1, atividadeId); s.setObject(2, usuarioId);
            try (var r = s.executeQuery()) { return r.next(); }
        }
    }
    public List<DadosFrequencia.Registro> listar(UUID atividadeId) {
        var lista = new ArrayList<DadosFrequencia.Registro>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT id, atividade_id, usuario_id, marcacao, registrada_em, registrada_por FROM registros_frequencia WHERE atividade_id = ? ORDER BY registrada_em, id")) {
            s.setObject(1, atividadeId);
            try (var r = s.executeQuery()) { while (r.next()) lista.add(mapear(r)); }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }
}
