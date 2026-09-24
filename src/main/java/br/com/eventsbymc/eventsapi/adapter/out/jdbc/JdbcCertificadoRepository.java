package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.port.out.CertificadoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosCertificado;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public final class JdbcCertificadoRepository implements CertificadoRepository {
    private final JdbcConnectionFactory conexoes;
    public JdbcCertificadoRepository(JdbcConnectionFactory conexoes) { this.conexoes = conexoes; }

    private static DadosCertificado mapear(ResultSet r) throws SQLException {
        var enviado = r.getTimestamp("enviado_em");
        return new DadosCertificado(r.getObject("id", UUID.class), r.getObject("evento_id", UUID.class),
                r.getString("evento_titulo"), r.getString("destinatario"), r.getString("email"),
                r.getString("tipo"), r.getTimestamp("emitido_em").toInstant(),
                enviado == null ? null : enviado.toInstant());
    }

    private static DadosCertificado consultar(Connection c, UUID eventoId, UUID destinatarioId,
                                               boolean participante, String papel) throws SQLException {
        String sql = participante ? """
                SELECT c.id, c.evento_id, e.titulo AS evento_titulo, u.nome AS destinatario,
                       u.email, c.tipo, c.emitido_em, c.enviado_em
                  FROM certificados c JOIN eventos e ON e.id = c.evento_id
                  JOIN usuarios u ON u.id = c.usuario_id
                 WHERE c.evento_id = ? AND c.usuario_id = ?
                """ : """
                SELECT c.id, c.evento_id, e.titulo AS evento_titulo, p.nome AS destinatario,
                       p.email, c.tipo, c.emitido_em, c.enviado_em
                  FROM certificados c JOIN eventos e ON e.id = c.evento_id
                  JOIN pessoas_evento p ON p.id = c.pessoa_id
                 WHERE c.evento_id = ? AND c.pessoa_id = ? AND c.tipo = ?
                """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId); s.setObject(2, destinatarioId);
            if (!participante) s.setString(3, papel);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                return mapear(r);
            }
        }
    }

    public DadosCertificado emitirParticipante(UUID eventoId, UUID usuarioId) {
        String sql = "INSERT INTO certificados(id, evento_id, usuario_id, tipo) VALUES (?, ?, ?, 'PARTICIPANTE') ON CONFLICT DO NOTHING";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, UUID.randomUUID()); s.setObject(2, eventoId); s.setObject(3, usuarioId);
            s.executeUpdate(); return consultar(c, eventoId, usuarioId, true, null);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao emitir certificado.", e); }
    }

    public DadosCertificado emitirPessoa(UUID eventoId, UUID pessoaId, String papel) {
        String sql = """
                INSERT INTO certificados(id, evento_id, pessoa_id, tipo)
                SELECT ?, ?, ?, ? WHERE EXISTS (
                    SELECT 1 FROM atividade_pessoas ap
                     WHERE ap.evento_id = ? AND ap.pessoa_id = ? AND ap.papel = ?)
                ON CONFLICT DO NOTHING
                """;
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, UUID.randomUUID()); s.setObject(2, eventoId); s.setObject(3, pessoaId);
            s.setString(4, papel); s.setObject(5, eventoId); s.setObject(6, pessoaId); s.setString(7, papel);
            if (s.executeUpdate() == 0) {
                // Também pode ser uma emissão já existente: consultar distingue os casos.
                return consultar(c, eventoId, pessoaId, false, papel);
            }
            return consultar(c, eventoId, pessoaId, false, papel);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao emitir declaração.", e); }
    }

    public void marcarEnviado(UUID certificadoId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "UPDATE certificados SET enviado_em = CURRENT_TIMESTAMP WHERE id = ?")) {
            s.setObject(1, certificadoId); s.executeUpdate();
        } catch (SQLException e) { throw new IllegalStateException("Falha ao registrar envio.", e); }
    }
}
