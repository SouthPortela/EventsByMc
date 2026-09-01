package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcUsuarioRepositoryTest {
    @Test
    void exigeConexaoJdbcEUsuarioParaPersistir() {
        JdbcConnectionFactory connectionFactory = new JdbcConnectionFactory(
                "jdbc:postgresql://localhost:5432/eventos", "usuario", "senha");
        JdbcUsuarioRepository repository = new JdbcUsuarioRepository(connectionFactory);

        assertDoesNotThrow(() -> new JdbcUsuarioRepository(connectionFactory));
        assertThrows(IllegalArgumentException.class, () -> repository.salvar(null));
        assertThrows(IllegalArgumentException.class, () -> new JdbcUsuarioRepository(null));
    }
}
