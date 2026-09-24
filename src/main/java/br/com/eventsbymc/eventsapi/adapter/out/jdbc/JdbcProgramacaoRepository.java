package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.port.out.ProgramacaoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosProgramacao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class JdbcProgramacaoRepository implements ProgramacaoRepository {
    private static final String SELECT_ATIVIDADES = """
            SELECT a.id, a.evento_id, a.titulo, a.descricao, a.inicio, a.fim,
                   a.local, a.capacidade, a.trilha_id, t.nome AS trilha,
                   a.espaco_id, s.nome AS espaco, a.tipo, a.presenca_obrigatoria,
                   a.politica_frequencia, a.permanencia_minima_percentual,
                   (SELECT COUNT(*) FROM agenda_atividades ga WHERE ga.atividade_id = a.id) AS reservas
              FROM atividades a
              LEFT JOIN trilhas t ON t.id = a.trilha_id
              LEFT JOIN espacos s ON s.id = a.espaco_id
            """;
    private final JdbcConnectionFactory conexoes;

    public JdbcProgramacaoRepository(JdbcConnectionFactory conexoes) {
        this.conexoes = Objects.requireNonNull(conexoes);
    }

    private static RuntimeException erro(SQLException exception) {
        if ("23505".equals(exception.getSQLState())) {
            return new ConflitoOperacaoException("Esse registro já existe.");
        }
        if ("23503".equals(exception.getSQLState())) return new RecursoNaoEncontradoException();
        return new IllegalStateException("Não foi possível concluir a operação no banco.", exception);
    }

    private static Timestamp timestamp(Instant valor) {
        return valor == null ? null : Timestamp.from(valor);
    }

    private static Instant instant(ResultSet resultado, String coluna) throws SQLException {
        Timestamp valor = resultado.getTimestamp(coluna);
        return valor == null ? null : valor.toInstant();
    }

    public DadosProgramacao.Politica consultarPolitica(UUID eventoId) {
        String sql = """
                SELECT inscricoes_abertas, inscricoes_inicio, inscricoes_fim,
                       limite_inscritos, permitir_cancelamento, frequencia_minima_percentual
                  FROM eventos WHERE id = ?
                """;
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return new DadosProgramacao.Politica(r.getBoolean("inscricoes_abertas"),
                        instant(r, "inscricoes_inicio"), instant(r, "inscricoes_fim"),
                        (Integer) r.getObject("limite_inscritos"), r.getBoolean("permitir_cancelamento"),
                        r.getInt("frequencia_minima_percentual"));
            }
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosProgramacao.Politica atualizarPolitica(UUID eventoId, DadosProgramacao.Politica politica) {
        String sql = """
                UPDATE eventos SET inscricoes_abertas = ?, inscricoes_inicio = ?,
                    inscricoes_fim = ?, limite_inscritos = ?, permitir_cancelamento = ?,
                    frequencia_minima_percentual = ? WHERE id = ?
                """;
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setBoolean(1, politica.inscricoesAbertas());
            s.setTimestamp(2, timestamp(politica.inscricoesInicio()));
            s.setTimestamp(3, timestamp(politica.inscricoesFim()));
            if (politica.limiteInscritos() == null) s.setNull(4, java.sql.Types.INTEGER);
            else s.setInt(4, politica.limiteInscritos());
            s.setBoolean(5, politica.permitirCancelamento());
            s.setInt(6, politica.frequenciaMinimaPercentual());
            s.setObject(7, eventoId);
            if (s.executeUpdate() != 1) throw new RecursoNaoEncontradoException();
            return politica;
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosProgramacao.Trilha criarTrilha(UUID eventoId, String nome) {
        UUID id = UUID.randomUUID();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "INSERT INTO trilhas(id, evento_id, nome) VALUES (?, ?, ?)")) {
            s.setObject(1, id); s.setObject(2, eventoId); s.setString(3, nome);
            s.executeUpdate();
            return new DadosProgramacao.Trilha(id, nome);
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosProgramacao.Espaco criarEspaco(UUID eventoId, String nome, Integer capacidade) {
        UUID id = UUID.randomUUID();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "INSERT INTO espacos(id, evento_id, nome, capacidade) VALUES (?, ?, ?, ?)")) {
            s.setObject(1, id); s.setObject(2, eventoId); s.setString(3, nome);
            if (capacidade == null) s.setNull(4, java.sql.Types.INTEGER); else s.setInt(4, capacidade);
            s.executeUpdate();
            return new DadosProgramacao.Espaco(id, nome, capacidade);
        } catch (SQLException e) { throw erro(e); }
    }

    public DadosProgramacao.Pessoa criarPessoa(UUID eventoId, String nome, String email) {
        UUID id = UUID.randomUUID();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "INSERT INTO pessoas_evento(id, evento_id, nome, email) VALUES (?, ?, ?, ?)")) {
            s.setObject(1, id); s.setObject(2, eventoId); s.setString(3, nome); s.setString(4, email);
            s.executeUpdate();
            return new DadosProgramacao.Pessoa(id, nome, email);
        } catch (SQLException e) { throw erro(e); }
    }

    public void vincularPessoa(UUID eventoId, UUID atividadeId, UUID pessoaId, String papel) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "INSERT INTO atividade_pessoas(atividade_id, evento_id, pessoa_id, papel) VALUES (?, ?, ?, ?)")) {
            s.setObject(1, atividadeId); s.setObject(2, eventoId);
            s.setObject(3, pessoaId); s.setString(4, papel);
            s.executeUpdate();
        } catch (SQLException e) { throw erro(e); }
    }

    public List<DadosProgramacao.Trilha> listarTrilhas(UUID eventoId) {
        var lista = new ArrayList<DadosProgramacao.Trilha>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT id, nome FROM trilhas WHERE evento_id = ? ORDER BY nome")) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosProgramacao.Trilha(r.getObject("id", UUID.class), r.getString("nome")));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }

    public List<DadosProgramacao.Espaco> listarEspacos(UUID eventoId) {
        var lista = new ArrayList<DadosProgramacao.Espaco>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT id, nome, capacidade FROM espacos WHERE evento_id = ? ORDER BY nome")) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosProgramacao.Espaco(r.getObject("id", UUID.class),
                        r.getString("nome"), (Integer) r.getObject("capacidade")));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }

    public List<DadosProgramacao.Pessoa> listarPessoas(UUID eventoId) {
        var lista = new ArrayList<DadosProgramacao.Pessoa>();
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT id, nome, email FROM pessoas_evento WHERE evento_id = ? ORDER BY nome")) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosProgramacao.Pessoa(r.getObject("id", UUID.class),
                        r.getString("nome"), r.getString("email")));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }

    private List<DadosProgramacao.Papel> pessoas(Connection c, UUID atividadeId) throws SQLException {
        var lista = new ArrayList<DadosProgramacao.Papel>();
        String sql = """
                SELECT p.id, p.nome, ap.papel FROM atividade_pessoas ap
                JOIN pessoas_evento p ON p.id = ap.pessoa_id
                WHERE ap.atividade_id = ? ORDER BY p.nome, ap.papel
                """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, atividadeId);
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(new DadosProgramacao.Papel(r.getObject("id", UUID.class),
                        r.getString("nome"), r.getString("papel")));
            }
        }
        return lista;
    }

    private DadosProgramacao.Atividade atividade(Connection c, ResultSet r) throws SQLException {
        UUID id = r.getObject("id", UUID.class);
        return new DadosProgramacao.Atividade(id, r.getObject("evento_id", UUID.class),
                r.getString("titulo"), r.getString("descricao"),
                r.getObject("inicio", LocalDateTime.class), r.getObject("fim", LocalDateTime.class),
                r.getString("local"), (Integer) r.getObject("capacidade"), r.getLong("reservas"),
                r.getObject("trilha_id", UUID.class), r.getString("trilha"),
                r.getObject("espaco_id", UUID.class), r.getString("espaco"),
                r.getString("tipo"), r.getBoolean("presenca_obrigatoria"),
                r.getString("politica_frequencia"), r.getInt("permanencia_minima_percentual"), pessoas(c, id));
    }

    public List<DadosProgramacao.Atividade> listarAtividades(UUID eventoId, DadosProgramacao.Filtros filtros) {
        var lista = new ArrayList<DadosProgramacao.Atividade>();
        StringBuilder sql = new StringBuilder(SELECT_ATIVIDADES + " WHERE a.evento_id = ?");
        if (filtros != null) {
            if (filtros.trilhaId() != null) sql.append(" AND a.trilha_id = ?");
            if (filtros.espacoId() != null) sql.append(" AND a.espaco_id = ?");
            if (filtros.tipo() != null) sql.append(" AND a.tipo = ?");
            if (filtros.de() != null) sql.append(" AND a.inicio >= ?");
            if (filtros.ate() != null) sql.append(" AND a.inicio < ?");
        }
        sql.append(" ORDER BY a.inicio NULLS LAST, a.id");
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql.toString())) {
            int parametro = 1;
            s.setObject(parametro++, eventoId);
            if (filtros != null) {
                if (filtros.trilhaId() != null) s.setObject(parametro++, filtros.trilhaId());
                if (filtros.espacoId() != null) s.setObject(parametro++, filtros.espacoId());
                if (filtros.tipo() != null) s.setString(parametro++, filtros.tipo());
                if (filtros.de() != null) s.setObject(parametro++, filtros.de());
                if (filtros.ate() != null) s.setObject(parametro, filtros.ate());
            }
            try (var r = s.executeQuery()) {
                while (r.next()) lista.add(atividade(c, r));
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }

    private DadosProgramacao.Atividade buscarAtividade(Connection c, UUID id) throws SQLException {
        try (var s = c.prepareStatement(SELECT_ATIVIDADES + " WHERE a.id = ?")) {
            s.setObject(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return atividade(c, r);
            }
        }
    }

    public DadosProgramacao.Atividade criarAtividade(UUID eventoId, DadosProgramacao.NovaAtividade dados) {
        UUID id = UUID.randomUUID();
        String sql = """
                INSERT INTO atividades(id, evento_id, titulo, descricao, inicio, fim, local,
                    capacidade, trilha_id, espaco_id, tipo, presenca_obrigatoria,
                    politica_frequencia, permanencia_minima_percentual)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (var c = conexoes.abrirConexao()) {
            Integer capacidade = dados.capacidade();
            if (dados.espacoId() != null) {
                try (var consultar = c.prepareStatement(
                        "SELECT capacidade FROM espacos WHERE id = ? AND evento_id = ?")) {
                    consultar.setObject(1, dados.espacoId()); consultar.setObject(2, eventoId);
                    try (var r = consultar.executeQuery()) {
                        if (!r.next()) throw new RecursoNaoEncontradoException();
                        Integer limiteEspaco = (Integer) r.getObject("capacidade");
                        if (limiteEspaco != null && capacidade != null && capacidade > limiteEspaco) {
                            throw new ConflitoOperacaoException("A capacidade da atividade supera a do espaço.");
                        }
                        if (capacidade == null) capacidade = limiteEspaco;
                    }
                }
            }
            try (var s = c.prepareStatement(sql)) {
            s.setObject(1, id); s.setObject(2, eventoId); s.setString(3, dados.titulo());
            s.setString(4, dados.descricao()); s.setObject(5, dados.dataInicio());
            s.setObject(6, dados.dataFim()); s.setString(7, dados.local());
            if (capacidade == null) s.setNull(8, java.sql.Types.INTEGER);
            else s.setInt(8, capacidade);
            s.setObject(9, dados.trilhaId()); s.setObject(10, dados.espacoId());
            s.setString(11, dados.tipo()); s.setBoolean(12, dados.presencaObrigatoria());
            s.setString(13, dados.politicaFrequencia()); s.setInt(14, dados.permanenciaMinimaPercentual());
            s.executeUpdate();
            }
            return buscarAtividade(c, id);
        } catch (SQLException e) { throw erro(e); }
    }

    public List<DadosProgramacao.AgendaItem> consultarAgenda(UUID usuarioId) {
        var lista = new ArrayList<DadosProgramacao.AgendaItem>();
        String sql = SELECT_ATIVIDADES + " JOIN agenda_atividades ga ON ga.atividade_id = a.id"
                + " WHERE ga.usuario_id = ? ORDER BY a.inicio NULLS LAST, a.id";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, usuarioId);
            try (var r = s.executeQuery()) {
                while (r.next()) {
                    var selecionada = atividade(c, r);
                    lista.add(new DadosProgramacao.AgendaItem(selecionada, adicionadoEm(c, usuarioId, selecionada.id())));
                }
            }
            return List.copyOf(lista);
        } catch (SQLException e) { throw erro(e); }
    }

    private Instant adicionadoEm(Connection c, UUID usuarioId, UUID atividadeId) throws SQLException {
        try (var s = c.prepareStatement(
                "SELECT adicionada_em FROM agenda_atividades WHERE usuario_id = ? AND atividade_id = ?")) {
            s.setObject(1, usuarioId); s.setObject(2, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return r.getTimestamp(1).toInstant();
            }
        }
    }

    public DadosProgramacao.AgendaItem adicionarAgenda(UUID usuarioId, UUID atividadeId) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                DadosProgramacao.AgendaItem item = adicionarAgendaNaTransacao(c, usuarioId, atividadeId);
                c.commit();
                return item;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                if (e instanceof SQLException sql) throw erro(sql);
                throw e;
            }
        } catch (SQLException e) { throw erro(e); }
    }

    private DadosProgramacao.AgendaItem adicionarAgendaNaTransacao(Connection c, UUID usuarioId,
                                                                    UUID atividadeId) throws SQLException {
        // Serializa as seleções da mesma pessoa; a atividade é bloqueada para controlar vagas.
        try (var s = c.prepareStatement("SELECT id FROM usuarios WHERE id = ? FOR UPDATE")) {
            s.setObject(1, usuarioId);
            try (var r = s.executeQuery()) { if (!r.next()) throw new RecursoNaoEncontradoException(); }
        }
        UUID eventoId;
        UUID inscricaoId;
        LocalDateTime inicio;
        LocalDateTime fim;
        Integer capacidade;
        String sql = """
                SELECT a.evento_id, a.inicio, a.fim, a.capacidade,
                       e.estado AS evento_estado, i.id AS inscricao_id, i.estado AS inscricao_estado
                  FROM atividades a JOIN eventos e ON e.id = a.evento_id
                  LEFT JOIN inscricoes i ON i.evento_id = a.evento_id AND i.usuario_id = ?
                 WHERE a.id = ? FOR UPDATE OF a
                """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, usuarioId); s.setObject(2, atividadeId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                if (!"PUBLICADO".equals(r.getString("evento_estado"))
                        || !"ATIVA".equals(r.getString("inscricao_estado"))) {
                    throw new ConflitoOperacaoException("É necessária inscrição ativa em evento publicado.");
                }
                eventoId = r.getObject("evento_id", UUID.class);
                inscricaoId = r.getObject("inscricao_id", UUID.class);
                inicio = r.getObject("inicio", LocalDateTime.class);
                fim = r.getObject("fim", LocalDateTime.class);
                capacidade = (Integer) r.getObject("capacidade");
            }
        }
        if (inicio == null || fim == null) {
            throw new ConflitoOperacaoException("A atividade precisa ter horário para entrar na agenda.");
        }
        try (var s = c.prepareStatement(
                "SELECT 1 FROM agenda_atividades WHERE usuario_id = ? AND atividade_id = ?")) {
            s.setObject(1, usuarioId); s.setObject(2, atividadeId);
            try (var r = s.executeQuery()) {
                if (r.next()) return new DadosProgramacao.AgendaItem(
                        buscarAtividade(c, atividadeId), adicionadoEm(c, usuarioId, atividadeId));
            }
        }
        if (capacidade != null) {
            try (var s = c.prepareStatement(
                    "SELECT COUNT(*) FROM agenda_atividades WHERE atividade_id = ?")) {
                s.setObject(1, atividadeId);
                try (var r = s.executeQuery()) {
                    r.next();
                    if (r.getLong(1) >= capacidade) throw new ConflitoOperacaoException("Atividade sem vagas.");
                }
            }
        }
        String conflito = """
                SELECT 1 FROM agenda_atividades ga
                JOIN atividades outra ON outra.id = ga.atividade_id
                WHERE ga.usuario_id = ? AND outra.inicio < ? AND ? < outra.fim
                LIMIT 1
                """;
        try (var s = c.prepareStatement(conflito)) {
            s.setObject(1, usuarioId); s.setObject(2, fim); s.setObject(3, inicio);
            try (var r = s.executeQuery()) {
                if (r.next()) throw new ConflitoOperacaoException("Há conflito de horário com a sua agenda.");
            }
        }
        try (var s = c.prepareStatement("""
                INSERT INTO agenda_atividades(inscricao_id, evento_id, usuario_id, atividade_id)
                VALUES (?, ?, ?, ?)
                """)) {
            s.setObject(1, inscricaoId); s.setObject(2, eventoId);
            s.setObject(3, usuarioId); s.setObject(4, atividadeId);
            s.executeUpdate();
        }
        return new DadosProgramacao.AgendaItem(
                buscarAtividade(c, atividadeId), adicionadoEm(c, usuarioId, atividadeId));
    }

    public void removerAgenda(UUID usuarioId, UUID atividadeId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "DELETE FROM agenda_atividades WHERE usuario_id = ? AND atividade_id = ?")) {
            s.setObject(1, usuarioId); s.setObject(2, atividadeId);
            s.executeUpdate();
        } catch (SQLException e) { throw erro(e); }
    }
}
