package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.port.out.RelatoriosRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosRelatorios;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JdbcRelatoriosRepository implements RelatoriosRepository {
    private static final String FREQUENCIA = """
            SELECT i.usuario_id, u.nome, u.email, i.estado AS inscricao_estado,
                   e.estado AS evento_estado, e.frequencia_minima_percentual,
                   (SELECT COUNT(*) FROM atividades a
                     WHERE a.evento_id = e.id AND a.presenca_obrigatoria) AS obrigatorias,
                   (SELECT COUNT(*) FROM atividades a
                    WHERE a.evento_id = e.id AND a.presenca_obrigatoria AND (
                      (a.politica_frequencia = 'CHECKIN_UNICO' AND EXISTS (
                        SELECT 1 FROM presencas p WHERE p.atividade_id = a.id
                          AND p.usuario_id = i.usuario_id))
                      OR (a.politica_frequencia = 'VALIDACAO_MANUAL' AND EXISTS (
                        SELECT 1 FROM registros_frequencia rf WHERE rf.atividade_id = a.id
                          AND rf.usuario_id = i.usuario_id AND rf.marcacao = 'CONFIRMACAO'))
                      OR (a.politica_frequencia IN ('ENTRADA_SAIDA', 'PERCENTUAL_PERMANENCIA')
                        AND EXISTS (
                          SELECT 1 FROM registros_frequencia entrada
                          JOIN registros_frequencia saida
                            ON saida.atividade_id = entrada.atividade_id
                           AND saida.usuario_id = entrada.usuario_id AND saida.marcacao = 'SAIDA'
                         WHERE entrada.atividade_id = a.id AND entrada.usuario_id = i.usuario_id
                           AND entrada.marcacao = 'ENTRADA'
                           AND saida.registrada_em > entrada.registrada_em
                           AND (a.politica_frequencia = 'ENTRADA_SAIDA'
                             OR (a.inicio IS NOT NULL AND a.fim IS NOT NULL
                               AND EXTRACT(EPOCH FROM (saida.registrada_em - entrada.registrada_em))
                                 >= EXTRACT(EPOCH FROM (a.fim - a.inicio))
                                    * a.permanencia_minima_percentual / 100.0))))
                    )) AS confirmadas
              FROM inscricoes i
              JOIN usuarios u ON u.id = i.usuario_id
              JOIN eventos e ON e.id = i.evento_id
             WHERE i.evento_id = ?
            """;
    private final JdbcConnectionFactory conexoes;

    public JdbcRelatoriosRepository(JdbcConnectionFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes);
    }

    public List<DadosRelatorios.Inscrito> inscritos(UUID eventoId) {
        String sql = """
                SELECT i.usuario_id, u.nome, u.email, i.estado, i.criada_em
                  FROM inscricoes i JOIN usuarios u ON u.id = i.usuario_id
                 WHERE i.evento_id = ? ORDER BY u.nome, i.usuario_id
                """;
        var lista = new ArrayList<DadosRelatorios.Inscrito>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosRelatorios.Inscrito(
                        r.getObject("usuario_id", UUID.class), r.getString("nome"),
                        r.getString("email"), r.getString("estado"),
                        r.getTimestamp("criada_em").toInstant()));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar inscritos.", e); }
    }

    private static DadosRelatorios.Frequencia mapear(ResultSet r) throws SQLException {
        int obrigatorias = r.getInt("obrigatorias");
        int confirmadas = r.getInt("confirmadas");
        int minimo = r.getInt("frequencia_minima_percentual");
        int percentual = obrigatorias == 0 ? 0 : (int) Math.floor(100.0 * confirmadas / obrigatorias);
        boolean ativa = "ATIVA".equals(r.getString("inscricao_estado"));
        boolean encerrado = "ENCERRADO".equals(r.getString("evento_estado"));
        boolean elegivel = ativa && encerrado && obrigatorias > 0 && percentual >= minimo;
        String situacao = !ativa ? "CANCELADA" : !encerrado ? "EM_ANDAMENTO"
                : elegivel ? "APROVADO" : "INSUFICIENTE";
        return new DadosRelatorios.Frequencia(r.getObject("usuario_id", UUID.class),
                r.getString("nome"), r.getString("email"), obrigatorias, confirmadas,
                percentual, minimo, situacao, elegivel);
    }

    public List<DadosRelatorios.Frequencia> frequencias(UUID eventoId) {
        var lista = new ArrayList<DadosRelatorios.Frequencia>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(FREQUENCIA + " ORDER BY u.nome, i.usuario_id")) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(mapear(r));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar frequência.", e); }
    }

    public DadosRelatorios.Frequencia frequenciaDoParticipante(UUID usuarioId, UUID eventoId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(FREQUENCIA + " AND i.usuario_id = ?")) {
            s.setObject(1, eventoId); s.setObject(2, usuarioId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return mapear(r);
            }
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar frequência.", e); }
    }
}
