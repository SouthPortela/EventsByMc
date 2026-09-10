package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.domain.model.Atividade;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Implementação PostgreSQL da porta de persistência de eventos. 
public final class JdbcEventoRepository implements EventoRepository {
    private static final String SELECT_EVENTO = """
            SELECT e.id, e.titulo, e.descricao, e.inicio, e.fim, e.local, e.estado,
                   u.id AS usuario_id, u.nome AS organizador_nome, u.email AS organizador_email, u.senha_hash
              FROM eventos e
              JOIN usuarios u ON u.id = e.organizador_id
            """;
    private final JdbcConnectionFactory connectionFactory;

    public JdbcEventoRepository(JdbcConnectionFactory connectionFactory) {
        if (connectionFactory == null) throw new IllegalArgumentException("Conexão JDBC é obrigatória.");
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Evento salvar(Evento evento) {
        if (evento == null) throw new IllegalArgumentException("Evento é obrigatório.");
        try (Connection connection = connectionFactory.abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                salvarEvento(connection, evento);
                for (Atividade atividade : evento.getProgramacao().consultarAtividades()) salvarAtividade(connection, evento.getId(), atividade);
                connection.commit();
                return evento;
            } catch (SQLException exception) {
                connection.rollback();
                throw new IllegalStateException("Não foi possível salvar o evento.", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível acessar o banco de dados.", exception);
        }
    }

    @Override
    public Optional<Evento> buscarPorId(UUID id) {
        if (id == null) throw new IllegalArgumentException("Identificador do evento é obrigatório.");
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(SELECT_EVENTO + " WHERE e.id = ?")) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return Optional.empty();
                Evento evento = mapearEvento(connection, resultSet);
                carregarAtividades(connection, evento);
                return Optional.of(evento);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível consultar o evento.", exception);
        }
    }

    @Override
    public List<Evento> listar() {
        List<Evento> eventos = new ArrayList<>();
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(SELECT_EVENTO + " ORDER BY e.inicio NULLS LAST, e.titulo");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Evento evento = mapearEvento(connection, resultSet);
                carregarAtividades(connection, evento);
                eventos.add(evento);
            }
            return eventos;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível listar os eventos.", exception);
        }
    }

    @Override
    public void removerPorId(UUID id) {
        if (id == null) throw new IllegalArgumentException("Identificador do evento é obrigatório.");
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM eventos WHERE id = ?")) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível remover o evento.", exception);
        }
    }

    private void salvarEvento(Connection connection, Evento evento) throws SQLException {
        String sql = """
                INSERT INTO eventos (id, titulo, descricao, organizador_id, inicio, fim, local, estado)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, descricao = EXCLUDED.descricao,
                    organizador_id = EXCLUDED.organizador_id, inicio = EXCLUDED.inicio, fim = EXCLUDED.fim,
                    local = EXCLUDED.local, estado = EXCLUDED.estado
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, evento.getId()); statement.setString(2, evento.getTitulo());
            statement.setString(3, evento.getDescricao()); statement.setObject(4, evento.getOrganizador().getId());
            statement.setTimestamp(5, timestamp(evento.getInicio())); statement.setTimestamp(6, timestamp(evento.getFim()));
            statement.setString(7, evento.getLocal()); statement.setString(8, evento.getEstado().name());
            statement.executeUpdate();
        }
    }

    private void salvarAtividade(Connection connection, UUID eventoId, Atividade atividade) throws SQLException {
        String sql = """
                INSERT INTO atividades (id, evento_id, titulo, descricao, inicio, fim, local, capacidade)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, descricao = EXCLUDED.descricao,
                    inicio = EXCLUDED.inicio, fim = EXCLUDED.fim, local = EXCLUDED.local, capacidade = EXCLUDED.capacidade
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, atividade.getId()); statement.setObject(2, eventoId);
            statement.setString(3, atividade.getTitulo()); statement.setString(4, atividade.getDescricao());
            statement.setTimestamp(5, timestamp(atividade.getInicio())); statement.setTimestamp(6, timestamp(atividade.getFim()));
            statement.setString(7, atividade.getLocal());
            if (atividade.getCapacidade() == null) statement.setNull(8, java.sql.Types.INTEGER); else statement.setInt(8, atividade.getCapacidade());
            statement.executeUpdate();
        }
    }

    private Evento mapearEvento(Connection connection, ResultSet row) throws SQLException {
        UUID usuarioId = row.getObject("usuario_id", UUID.class);
        Usuario organizador = Usuario.reconstituir(usuarioId, new Pessoa(row.getString("organizador_nome"), row.getString("organizador_email")),
                row.getString("senha_hash"), buscarPerfis(connection, usuarioId));
        return Evento.reconstituir(row.getObject("id", UUID.class), row.getString("titulo"), row.getString("descricao"), organizador,
                localDateTime(row, "inicio"), localDateTime(row, "fim"), row.getString("local"), EstadoEvento.valueOf(row.getString("estado")));
    }

    private EnumSet<Perfil> buscarPerfis(Connection connection, UUID usuarioId) throws SQLException {
        EnumSet<Perfil> perfis = EnumSet.noneOf(Perfil.class);
        try (PreparedStatement statement = connection.prepareStatement("SELECT perfil FROM usuario_perfis WHERE usuario_id = ?")) {
            statement.setObject(1, usuarioId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) perfis.add(Perfil.valueOf(resultSet.getString(1))); }
        }
        return perfis;
    }

    private void carregarAtividades(Connection connection, Evento evento) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, titulo, descricao, inicio, fim, local, capacidade FROM atividades WHERE evento_id = ? ORDER BY inicio NULLS LAST, titulo")) {
            statement.setObject(1, evento.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) evento.adicionarAtividade(Atividade.reconstituir(resultSet.getObject("id", UUID.class), resultSet.getString("titulo"), resultSet.getString("descricao"), localDateTime(resultSet, "inicio"), localDateTime(resultSet, "fim"), resultSet.getString("local"), (Integer) resultSet.getObject("capacidade")));
            }
        }
    }

    private static Timestamp timestamp(java.time.LocalDateTime value) { return value == null ? null : Timestamp.valueOf(value); }
    private static java.time.LocalDateTime localDateTime(ResultSet resultSet, String column) throws SQLException { Timestamp value = resultSet.getTimestamp(column); return value == null ? null : value.toLocalDateTime(); }
}
