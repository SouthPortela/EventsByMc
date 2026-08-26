package br.com.eventsbymc.eventsapi.infrastructure.jdbc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcEventoRepositoryTest {
    @Test
    void exigeConfiguracaoJdbcEEventoParaPersistir() {
        JdbcConnectionFactory connectionFactory = new JdbcConnectionFactory(
                "jdbc:postgresql://localhost:5432/eventos", "usuario", "senha");
        JdbcEventoRepository repository = new JdbcEventoRepository(connectionFactory);

        assertDoesNotThrow(() -> new JdbcEventoRepository(connectionFactory));
        assertThrows(IllegalArgumentException.class, () -> repository.salvar(null));
        assertThrows(IllegalArgumentException.class, () -> new JdbcEventoRepository(null));
    }

    @Test
    void rejeitaConfiguracaoJDBCIncompleta() {
        assertThrows(IllegalStateException.class, () -> new JdbcConnectionFactory("", "usuario", "senha"));
    }
}
