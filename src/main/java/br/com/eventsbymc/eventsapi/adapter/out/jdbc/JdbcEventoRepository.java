package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.domain.model.Atividade;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import br.com.eventsbymc.eventsapi.domain.model.RegistroModeracaoEvento;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Implementação PostgreSQL da porta de persistência de eventos. 
public final class JdbcEventoRepository implements EventoRepository {
    private static final String SELECT_EVENTO = """
            SELECT e.id, e.titulo, e.descricao, e.inicio, e.fim, e.local, e.estado, e.categoria,
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
                return Optional.of(carregarAtividades(connection, evento));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível consultar o evento.", exception);
        }
    }

    @Override
    public boolean alterarEstado(UUID id, EstadoEvento esperado, EstadoEvento destino) {
        String sql = "UPDATE eventos SET estado = ? WHERE id = ? AND estado = ?";
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, destino.name());
            statement.setObject(2, id);
            statement.setString(3, esperado.name());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível alterar o estado do evento.", exception);
        }
    }

    @Override
    public boolean alterarCategoria(UUID id, CategoriaEvento categoria) {
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE eventos SET categoria = ? WHERE id = ? AND estado NOT IN ('SUSPENSO', 'EXCLUIDO')")) {
            statement.setString(1, categoria.name());
            statement.setObject(2, id);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível alterar a categoria do evento.", exception);
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
                eventos.add(carregarAtividades(connection, evento));
            }
            return eventos;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível listar os eventos.", exception);
        }
    }

    @Override
    public boolean moderar(UUID id, EstadoEvento esperado, EstadoEvento destino, UUID administradorId, String motivo) {
        try (Connection connection = connectionFactory.abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                int alterados;
                String sql = """
                        UPDATE eventos SET estado = ?,
                            titulo = CASE WHEN ? = 'EXCLUIDO' THEN '[Evento removido]' ELSE titulo END,
                            descricao = CASE WHEN ? = 'EXCLUIDO' THEN NULL ELSE descricao END,
                            local = CASE WHEN ? = 'EXCLUIDO' THEN NULL ELSE local END
                        WHERE id = ? AND estado = ?
                        """;
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, destino.name());
                    for (int indice = 2; indice <= 4; indice++) statement.setString(indice, destino.name());
                    statement.setObject(5, id);
                    statement.setString(6, esperado.name());
                    alterados = statement.executeUpdate();
                }
                if (alterados == 0) { connection.rollback(); return false; }
                if (destino == EstadoEvento.EXCLUIDO) limparConteudo(connection, id);
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO moderacoes_evento
                            (id, evento_id, administrador_id, estado_anterior, estado_novo, motivo)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """)) {
                    statement.setObject(1, UUID.randomUUID());
                    statement.setObject(2, id);
                    statement.setObject(3, administradorId);
                    statement.setString(4, esperado.name());
                    statement.setString(5, destino.name());
                    statement.setString(6, motivo);
                    statement.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível moderar o evento.", exception);
        }
    }

    private void limparConteudo(Connection connection, UUID eventoId) throws SQLException {
        String[] comandos = {
                "UPDATE atividades SET titulo = '[Atividade removida]', descricao = NULL, local = NULL WHERE evento_id = ?",
                "UPDATE trilhas SET nome = 'Trilha removida ' || id::text WHERE evento_id = ?",
                "UPDATE espacos SET nome = 'Espaço removido ' || id::text WHERE evento_id = ?",
                "UPDATE pessoas_evento SET nome = 'Pessoa removida ' || id::text, email = NULL WHERE evento_id = ?",
                "UPDATE questionarios SET titulo = '[Questionário removido]' WHERE evento_id = ?",
                "UPDATE questoes SET enunciado = '[Questão removida]', opcoes = CASE WHEN tipo = 'ESCOLHA_UNICA' THEN '[\"Removida\",\"Removida\"]'::jsonb ELSE NULL END WHERE questionario_id IN (SELECT id FROM questionarios WHERE evento_id = ?)",
                "UPDATE respostas_avaliacao SET valor = '[Conteúdo removido]' WHERE questionario_id IN (SELECT id FROM questionarios WHERE evento_id = ?)",
                "UPDATE mensagens_evento SET mensagem = '[Conteúdo removido]' WHERE evento_id = ?"
        };
        for (String comando : comandos) {
            try (PreparedStatement statement = connection.prepareStatement(comando)) {
                statement.setObject(1, eventoId);
                statement.executeUpdate();
            }
        }
    }

    @Override
    public List<RegistroModeracaoEvento> listarModeracoes() {
        String sql = """
                SELECT m.id, m.evento_id, m.administrador_id, u.nome, m.estado_anterior,
                       m.estado_novo, m.motivo, m.criado_em
                  FROM moderacoes_evento m JOIN usuarios u ON u.id = m.administrador_id
                 ORDER BY m.criado_em DESC, m.id DESC
                """;
        List<RegistroModeracaoEvento> registros = new ArrayList<>();
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultado = statement.executeQuery()) {
            while (resultado.next()) {
                Instant criadoEm = resultado.getTimestamp("criado_em").toInstant();
                registros.add(new RegistroModeracaoEvento(resultado.getObject("id", UUID.class),
                        resultado.getObject("evento_id", UUID.class),
                        resultado.getObject("administrador_id", UUID.class), resultado.getString("nome"),
                        EstadoEvento.valueOf(resultado.getString("estado_anterior")),
                        EstadoEvento.valueOf(resultado.getString("estado_novo")),
                        resultado.getString("motivo"), criadoEm));
            }
            return registros;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível consultar a auditoria.", exception);
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
                INSERT INTO eventos (id, titulo, descricao, organizador_id, inicio, fim, local, estado, categoria)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET titulo = EXCLUDED.titulo, descricao = EXCLUDED.descricao,
                    organizador_id = EXCLUDED.organizador_id, inicio = EXCLUDED.inicio, fim = EXCLUDED.fim,
                    local = EXCLUDED.local, estado = EXCLUDED.estado, categoria = EXCLUDED.categoria
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, evento.getId()); statement.setString(2, evento.getTitulo());
            statement.setString(3, evento.getDescricao()); statement.setObject(4, evento.getOrganizador().getId());
            statement.setTimestamp(5, timestamp(evento.getInicio())); statement.setTimestamp(6, timestamp(evento.getFim()));
            statement.setString(7, evento.getLocal()); statement.setString(8, evento.getEstado().name());
            statement.setString(9, evento.getCategoria().name());
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
                localDateTime(row, "inicio"), localDateTime(row, "fim"), row.getString("local"),
                CategoriaEvento.valueOf(row.getString("categoria")), EstadoEvento.valueOf(row.getString("estado")));
    }

    private EnumSet<Perfil> buscarPerfis(Connection connection, UUID usuarioId) throws SQLException {
        EnumSet<Perfil> perfis = EnumSet.noneOf(Perfil.class);
        try (PreparedStatement statement = connection.prepareStatement("SELECT perfil FROM usuario_perfis WHERE usuario_id = ?")) {
            statement.setObject(1, usuarioId);
            try (ResultSet resultSet = statement.executeQuery()) { while (resultSet.next()) perfis.add(Perfil.valueOf(resultSet.getString(1))); }
        }
        return perfis;
    }

    private Evento carregarAtividades(Connection connection, Evento evento) throws SQLException {
        List<Atividade> atividades = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, titulo, descricao, inicio, fim, local, capacidade FROM atividades WHERE evento_id = ? ORDER BY inicio NULLS LAST, titulo")) {
            statement.setObject(1, evento.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) atividades.add(Atividade.reconstituir(resultSet.getObject("id", UUID.class), resultSet.getString("titulo"), resultSet.getString("descricao"), localDateTime(resultSet, "inicio"), localDateTime(resultSet, "fim"), resultSet.getString("local"), (Integer) resultSet.getObject("capacidade")));
            }
        }
        return Evento.reconstituir(evento.getId(), evento.getTitulo(), evento.getDescricao(), evento.getOrganizador(),
                evento.getInicio(), evento.getFim(), evento.getLocal(), evento.getCategoria(), evento.getEstado(), atividades);
    }

    private static Timestamp timestamp(java.time.LocalDateTime value) { return value == null ? null : Timestamp.valueOf(value); }
    private static java.time.LocalDateTime localDateTime(ResultSet resultSet, String column) throws SQLException { Timestamp value = resultSet.getTimestamp(column); return value == null ? null : value.toLocalDateTime(); }
}
