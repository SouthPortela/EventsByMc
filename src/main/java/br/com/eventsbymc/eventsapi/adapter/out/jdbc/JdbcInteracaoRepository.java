package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.port.out.InteracaoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosInteracao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class JdbcInteracaoRepository implements InteracaoRepository {
    private final JdbcConnectionFactory conexoes;
    public JdbcInteracaoRepository(JdbcConnectionFactory conexoes) { this.conexoes = conexoes; }
    public boolean participanteAtivo(UUID eventoId, UUID usuarioId) {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT 1 FROM inscricoes WHERE evento_id = ? AND usuario_id = ? AND estado = 'ATIVA'")) {
            s.setObject(1, eventoId); s.setObject(2, usuarioId);
            try (var r = s.executeQuery()) { return r.next(); }
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar participação.", e); }
    }
    private static DadosInteracao.Mensagem mapear(ResultSet r) throws SQLException {
        return new DadosInteracao.Mensagem(r.getObject("id", UUID.class),
                r.getObject("evento_id", UUID.class), r.getObject("usuario_id", UUID.class),
                r.getString("autor"), r.getString("mensagem"), r.getTimestamp("criada_em").toInstant());
    }
    public List<DadosInteracao.Mensagem> listar(UUID eventoId) {
        var lista = new ArrayList<DadosInteracao.Mensagem>();
        String sql = "SELECT m.id, m.evento_id, m.usuario_id, u.nome AS autor, m.mensagem, m.criada_em FROM mensagens_evento m JOIN usuarios u ON u.id = m.usuario_id WHERE m.evento_id = ? ORDER BY m.criada_em DESC, m.id DESC LIMIT 50";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, eventoId);
            try (var r = s.executeQuery()) { while (r.next()) lista.add(mapear(r)); }
            return List.copyOf(lista);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao listar mensagens.", e); }
    }
    public DadosInteracao.Mensagem publicar(UUID eventoId, UUID usuarioId, String mensagem) {
        UUID id = UUID.randomUUID();
        String sql = "INSERT INTO mensagens_evento(id, evento_id, usuario_id, mensagem) SELECT ?, i.evento_id, i.usuario_id, ? FROM inscricoes i WHERE i.evento_id = ? AND i.usuario_id = ? AND i.estado = 'ATIVA' RETURNING id, evento_id, usuario_id, mensagem, criada_em";
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql)) {
            s.setObject(1, id); s.setString(2, mensagem); s.setObject(3, eventoId); s.setObject(4, usuarioId);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException();
                String autor;
                try (var nome = c.prepareStatement("SELECT nome FROM usuarios WHERE id = ?")) {
                    nome.setObject(1, usuarioId); try (var rn = nome.executeQuery()) { rn.next(); autor = rn.getString(1); }
                }
                return new DadosInteracao.Mensagem(id, eventoId, usuarioId, autor,
                        mensagem, r.getTimestamp("criada_em").toInstant());
            }
        } catch (SQLException e) { throw new IllegalStateException("Falha ao publicar mensagem.", e); }
    }
}
