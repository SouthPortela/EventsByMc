package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.out.AdministracaoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.sql.*;
import java.util.*;

public final class JdbcAdministracaoRepository implements AdministracaoRepository {
    private final JdbcConnectionFactory conexoes;
    public JdbcAdministracaoRepository(JdbcConnectionFactory conexoes) { this.conexoes = Objects.requireNonNull(conexoes); }
    public Resumo resumo() {
        String sql = """
            SELECT (SELECT COUNT(*) FROM usuarios) AS usuarios,
              (SELECT COUNT(*) FROM usuario_perfis WHERE perfil = 'ORGANIZADOR') AS organizadores,
              (SELECT COUNT(*) FROM eventos WHERE estado = 'PUBLICADO') AS publicados,
              (SELECT COUNT(*) FROM eventos WHERE estado = 'BLOQUEADO') AS bloqueados,
              ((SELECT COUNT(*) FROM presencas) + (SELECT COUNT(*) FROM registros_frequencia)) AS presencas
            """;
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(sql); var r = s.executeQuery()) {
            r.next(); return new Resumo(r.getLong("usuarios"), r.getLong("organizadores"),
                r.getLong("publicados"), r.getLong("bloqueados"), r.getLong("presencas"));
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar resumo administrativo.", e); }
    }
    public List<Registro> auditoria() {
        try (var c = conexoes.abrirConexao(); var s = c.prepareStatement(
                "SELECT id, responsavel_id, acao, recurso_id, detalhe, registrado_em FROM auditoria_administrativa ORDER BY registrado_em DESC, id LIMIT 500");
             var r = s.executeQuery()) {
            var registros = new ArrayList<Registro>();
            while (r.next()) registros.add(new Registro(r.getObject("id", UUID.class), r.getObject("responsavel_id", UUID.class),
                r.getString("acao"), r.getObject("recurso_id", UUID.class), r.getString("detalhe"), r.getTimestamp("registrado_em").toInstant()));
            return List.copyOf(registros);
        } catch (SQLException e) { throw new IllegalStateException("Falha ao consultar auditoria.", e); }
    }
    @FunctionalInterface private interface Operacao { void executar(Connection c) throws SQLException; }
    private void transacao(UUID responsavel, Operacao operacao) {
        try (var c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                // Serializa alteracoes administrativas, inclusive a protecao do ultimo administrador.
                try (var s = c.prepareStatement("SELECT pg_advisory_xact_lock(20851101)")) { s.execute(); }
                try (var s = c.prepareStatement("SELECT 1 FROM usuario_perfis WHERE usuario_id = ? AND perfil = 'ADMINISTRADOR'")) {
                    s.setObject(1, responsavel);
                    try (var r = s.executeQuery()) { if (!r.next()) throw new AcessoNegadoException(); }
                }
                operacao.executar(c); c.commit();
            } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
        } catch (SQLException e) { throw new IllegalStateException("Falha ao salvar operacao administrativa.", e); }
    }
    private static void registrar(Connection c, UUID responsavel, String acao, UUID recurso, String detalhe) throws SQLException {
        try (var s = c.prepareStatement("INSERT INTO auditoria_administrativa(id, responsavel_id, acao, recurso_id, detalhe) VALUES (?, ?, ?, ?, ?)")) {
            s.setObject(1, UUID.randomUUID()); s.setObject(2, responsavel); s.setString(3, acao);
            s.setObject(4, recurso); s.setString(5, detalhe); s.executeUpdate();
        }
    }
    public void alterarPerfis(UUID responsavel, UUID usuario, Set<Perfil> perfis) {
        transacao(responsavel, c -> {
            try (var s = c.prepareStatement("SELECT id FROM usuarios WHERE id = ? FOR UPDATE")) {
                s.setObject(1, usuario);
                try (var r = s.executeQuery()) { if (!r.next()) throw new RecursoNaoEncontradoException(); }
            }
            var anteriores = EnumSet.noneOf(Perfil.class);
            try (var s = c.prepareStatement("SELECT perfil FROM usuario_perfis WHERE usuario_id = ?")) {
                s.setObject(1, usuario);
                try (var r = s.executeQuery()) { while (r.next()) anteriores.add(Perfil.valueOf(r.getString(1))); }
            }
            if (anteriores.equals(perfis)) return;
            if (anteriores.contains(Perfil.ADMINISTRADOR) && !perfis.contains(Perfil.ADMINISTRADOR)) {
                try (var s = c.prepareStatement("SELECT COUNT(*) FROM usuario_perfis WHERE perfil = 'ADMINISTRADOR'"); var r = s.executeQuery()) {
                    r.next();
                    try { PoliticaPerfis.exigirAdministradorRestante(anteriores, perfis, r.getLong(1)); }
                    catch (IllegalStateException e) { throw new ConflitoOperacaoException(e.getMessage()); }
                }
            }
            try (var s = c.prepareStatement("DELETE FROM usuario_perfis WHERE usuario_id = ?")) {
                s.setObject(1, usuario); s.executeUpdate();
            }
            try (var s = c.prepareStatement("INSERT INTO usuario_perfis(usuario_id, perfil) VALUES (?, ?)")) {
                for (var perfil : perfis) { s.setObject(1, usuario); s.setString(2, perfil.name()); s.addBatch(); }
                s.executeBatch();
            }
            registrar(c, responsavel, "PERFIS_ALTERADOS", usuario, anteriores + " -> " + EnumSet.copyOf(perfis));
        });
    }
    public void alterarBloqueio(UUID responsavel, Evento evento, EstadoEvento esperado) {
        transacao(responsavel, c -> {
            try (var s = c.prepareStatement("UPDATE eventos SET estado = ?, motivo_bloqueio = ?, estado_anterior_bloqueio = ? WHERE id = ? AND estado = ?")) {
                s.setString(1, evento.getEstado().name()); s.setString(2, evento.getMotivoBloqueio());
                s.setString(3, evento.getEstadoAnteriorBloqueio() == null ? null : evento.getEstadoAnteriorBloqueio().name());
                s.setObject(4, evento.getId()); s.setString(5, esperado.name());
                if (s.executeUpdate() != 1) throw new ConflitoOperacaoException("Evento alterado por outra operacao. Atualize a lista.");
            }
            boolean bloqueado = evento.getEstado() == EstadoEvento.BLOQUEADO;
            registrar(c, responsavel, bloqueado ? "EVENTO_BLOQUEADO" : "EVENTO_DESBLOQUEADO", evento.getId(),
                bloqueado ? evento.getMotivoBloqueio() : "Estado restaurado: " + evento.getEstado());
        });
    }
}
