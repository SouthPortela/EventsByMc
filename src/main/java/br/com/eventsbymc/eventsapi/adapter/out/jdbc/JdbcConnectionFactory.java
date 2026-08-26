package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Adaptador JDBC sem acoplamento com frameworks. */
public final class JdbcConnectionFactory {
    private final String url;
    private final String username;
    private final String password;

    public JdbcConnectionFactory(String url, String username, String password) {
        this.url = requireText(url, "DB_URL");
        this.username = requireText(username, "DB_USERNAME");
        this.password = requireText(password, "DB_PASSWORD");
    }

    public static JdbcConnectionFactory fromEnvironment() {
        return new JdbcConnectionFactory(System.getenv("DB_URL"), System.getenv("DB_USERNAME"), System.getenv("DB_PASSWORD"));
    }

    public Connection abrirConexao() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalStateException("A variável de ambiente " + name + " é obrigatória.");
        return value;
    }
}
