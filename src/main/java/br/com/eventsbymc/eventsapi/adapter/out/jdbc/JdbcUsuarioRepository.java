package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Adaptador JDBC para a persistência de usuários e seus perfis. */
public final class JdbcUsuarioRepository implements UsuarioRepository {
    private static final String SELECT_USUARIO = """
            SELECT id, nome, email, senha_hash
              FROM usuarios
            """;

    private final JdbcConnectionFactory connectionFactory;

    public JdbcUsuarioRepository(JdbcConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("Conexão JDBC é obrigatória.");
        }
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Usuario salvar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário é obrigatório.");
        }

        try (Connection connection = connectionFactory.abrirConexao()) {
            connection.setAutoCommit(false);
            try {
                salvarDadosBasicos(connection, usuario);
                substituirPerfis(connection, usuario);
                connection.commit();
                return usuario;
            } catch (SQLException exception) {
                connection.rollback();
                throw new IllegalStateException("Não foi possível salvar o usuário.", exception);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível acessar o banco de dados.", exception);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Identificador do usuário é obrigatório.");
        }

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(SELECT_USUARIO + " WHERE id = ?")) {
            statement.setObject(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearUsuario(connection, resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível consultar o usuário.", exception);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email){
        if(email == null || email.isBlank()){
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        // ? usado para não haver SQL injection, evitando que o usuário insira código malicioso no campo de email.
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(SELECT_USUARIO + " WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapearUsuario(connection, resultSet));
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível consultar o usuário.", exception);
        }

    }

    @Override
    public List<Usuario> listar() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement(SELECT_USUARIO + " ORDER BY nome, email");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                usuarios.add(mapearUsuario(connection, resultSet));
            }
            return usuarios;
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível listar os usuários.", exception);
        }
    }

    @Override
    public void removerPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Identificador do usuário é obrigatório.");
        }

        try (Connection connection = connectionFactory.abrirConexao();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM usuarios WHERE id = ?")) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível remover o usuário.", exception);
        }
    }

    private void salvarDadosBasicos(Connection connection, Usuario usuario) throws SQLException {
        String sql = """
                INSERT INTO usuarios (id, nome, email, senha_hash)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET nome = EXCLUDED.nome, email = EXCLUDED.email,
                    senha_hash = EXCLUDED.senha_hash
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, usuario.getId());
            statement.setString(2, usuario.getPessoa().getNome());
            statement.setString(3, usuario.getPessoa().getEmail());
            statement.setString(4, usuario.getSenhaHash());
            statement.executeUpdate();
        }
    }

    private void substituirPerfis(Connection connection, Usuario usuario) throws SQLException {
        try (PreparedStatement deleteStatement = connection.prepareStatement(
                "DELETE FROM usuario_perfis WHERE usuario_id = ?")) {
            deleteStatement.setObject(1, usuario.getId());
            deleteStatement.executeUpdate();
        }

        try (PreparedStatement insertStatement = connection.prepareStatement(
                "INSERT INTO usuario_perfis (usuario_id, perfil) VALUES (?, ?)")) {
            for (Perfil perfil : usuario.getPerfis()) {
                insertStatement.setObject(1, usuario.getId());
                insertStatement.setString(2, perfil.name());
                insertStatement.addBatch();
            }
            insertStatement.executeBatch();
        }
    }

    private Usuario mapearUsuario(Connection connection, ResultSet row) throws SQLException {
        UUID id = row.getObject("id", UUID.class);
        Pessoa pessoa = new Pessoa(row.getString("nome"), row.getString("email"));
        return Usuario.reconstituir(id, pessoa, row.getString("senha_hash"), buscarPerfis(connection, id));
    }

    private EnumSet<Perfil> buscarPerfis(Connection connection, UUID usuarioId) throws SQLException {
        EnumSet<Perfil> perfis = EnumSet.noneOf(Perfil.class);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT perfil FROM usuario_perfis WHERE usuario_id = ?")) {
            statement.setObject(1, usuarioId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    perfis.add(Perfil.valueOf(resultSet.getString("perfil")));
                }
            }
        }
        return perfis;
    }
}
