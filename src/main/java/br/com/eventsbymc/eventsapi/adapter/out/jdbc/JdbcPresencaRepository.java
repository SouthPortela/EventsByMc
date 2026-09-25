package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.out.PresencaRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosPresenca;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public final class JdbcPresencaRepository implements PresencaRepository {
    private final JdbcConnectionFactory conexoes;
    public JdbcPresencaRepository(JdbcConnectionFactory conexoes) { this.conexoes = Objects.requireNonNull(conexoes); }
    @FunctionalInterface private interface Trabalho<T> { T executar(Connection c) throws SQLException; }
    private <T> T transacao(Trabalho<T> trabalho) {
        try (Connection c = conexoes.abrirConexao()) {
            c.setAutoCommit(false);
            try {
                T resultado = trabalho.executar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) throw new ConflitoOperacaoException("Registro concorrente. Atualize e tente novamente.");
            throw new IllegalStateException("Não foi possível concluir a operação no banco.", e);
        }
    }
    private static Instant agora(Connection c) throws SQLException {
        try (var s = c.prepareStatement("SELECT clock_timestamp()"); var r = s.executeQuery()) {
            r.next(); return r.getTimestamp(1).toInstant();
        }
    }
    public DadosPresenca.Participacao consultarParticipacao(UUID usuarioId) {
        return transacao(c -> {
            var inscricoes = new ArrayList<DadosPresenca.InscricaoResumo>();
            String sqlInscricoes = """
                SELECT i.id, i.evento_id, i.estado, i.criada_em,
                       CASE WHEN e.estado = 'SUSPENSO' THEN '[Evento indisponível]' ELSE e.titulo END AS titulo,
                       e.inicio, e.fim,
                       CASE WHEN e.estado = 'SUSPENSO' THEN NULL ELSE e.local END AS local,
                       e.estado AS evento_estado
                  FROM inscricoes i JOIN eventos e ON e.id = i.evento_id
                 WHERE i.usuario_id = ?
                 ORDER BY i.criada_em DESC, i.id
                """;
            try (var s = c.prepareStatement(sqlInscricoes)) {
                s.setObject(1, usuarioId);
                try (var r = s.executeQuery()) {
                    while (r.next()) inscricoes.add(new DadosPresenca.InscricaoResumo(
                            r.getObject("id", UUID.class), r.getObject("evento_id", UUID.class),
                            r.getString("titulo"), r.getObject("inicio", java.time.LocalDateTime.class),
                            r.getObject("fim", java.time.LocalDateTime.class), r.getString("local"),
                            r.getString("evento_estado"), r.getString("estado"),
                            r.getTimestamp("criada_em").toInstant()));
                }
            }
            var atividades = new ArrayList<DadosPresenca.AtividadeAgenda>();
            String sqlAgenda = """
                SELECT a.id, a.evento_id, e.titulo AS evento_titulo, a.titulo,
                       a.inicio, a.fim, COALESCE(a.local, e.local) AS local,
                       p.registrada_em
                  FROM inscricoes i
                  JOIN eventos e ON e.id = i.evento_id
                  JOIN atividades a ON a.evento_id = i.evento_id
                  LEFT JOIN presencas p ON p.atividade_id = a.id AND p.usuario_id = i.usuario_id
                 WHERE i.usuario_id = ? AND i.estado = 'ATIVA' AND e.estado = 'PUBLICADO'
                 ORDER BY a.inicio NULLS LAST, a.id
                """;
            try (var s = c.prepareStatement(sqlAgenda)) {
                s.setObject(1, usuarioId);
                try (var r = s.executeQuery()) {
                    while (r.next()) {
                        Timestamp presenca = r.getTimestamp("registrada_em");
                        atividades.add(new DadosPresenca.AtividadeAgenda(
                                r.getObject("id", UUID.class), r.getObject("evento_id", UUID.class),
                                r.getString("evento_titulo"), r.getString("titulo"),
                                r.getObject("inicio", java.time.LocalDateTime.class),
                                r.getObject("fim", java.time.LocalDateTime.class), r.getString("local"),
                                presenca == null ? null : presenca.toInstant()));
                    }
                }
            }
            long totalPresencas;
            try (var s = c.prepareStatement("SELECT COUNT(*) FROM presencas WHERE usuario_id = ?")) {
                s.setObject(1, usuarioId);
                try (var r = s.executeQuery()) { r.next(); totalPresencas = r.getLong(1); }
            }
            return new DadosPresenca.Participacao(inscricoes, atividades, totalPresencas);
        });
    }
    public DadosPresenca.Inscricao inscrever(UUID usuarioId, UUID eventoId) {
        return transacao(c -> {
            Instant agora = agora(c);
            Integer limite;
            boolean inscricoesAbertas;
            Instant inicioInscricoes;
            Instant fimInscricoes;
            try (var s = c.prepareStatement("""
                    SELECT estado, inscricoes_abertas, inscricoes_inicio, inscricoes_fim,
                           limite_inscritos FROM eventos WHERE id = ? FOR UPDATE
                    """)) {
                s.setObject(1, eventoId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new RecursoNaoEncontradoException();
                    if (!"PUBLICADO".equals(r.getString(1))) throw new ConflitoOperacaoException("O evento não está aberto para inscrições.");
                    inscricoesAbertas = r.getBoolean("inscricoes_abertas");
                    inicioInscricoes = r.getTimestamp("inscricoes_inicio") == null ? null
                            : r.getTimestamp("inscricoes_inicio").toInstant();
                    fimInscricoes = r.getTimestamp("inscricoes_fim") == null ? null
                            : r.getTimestamp("inscricoes_fim").toInstant();
                    limite = (Integer) r.getObject("limite_inscritos");
                }
            }
            // Serializa inscrições deste usuário, sem confiar no perfil guardado no navegador.
            try (var s = c.prepareStatement("SELECT id FROM usuarios WHERE id = ? FOR UPDATE")) {
                s.setObject(1, usuarioId);
                try (var r = s.executeQuery()) { if (!r.next()) throw new TokenInvalidoException("Sessão inválida.", null); }
            }
            UUID inscricaoAnterior = null;
            try (var s = c.prepareStatement("SELECT id, estado FROM inscricoes WHERE evento_id = ? AND usuario_id = ?")) {
                s.setObject(1, eventoId); s.setObject(2, usuarioId);
                try (var r = s.executeQuery()) {
                    if (r.next()) {
                        inscricaoAnterior = r.getObject("id", UUID.class);
                        if ("ATIVA".equals(r.getString("estado")))
                            return new DadosPresenca.Inscricao(inscricaoAnterior, "ATIVA");
                    }
                }
            }
            if (!inscricoesAbertas || inicioInscricoes != null && agora.isBefore(inicioInscricoes)
                    || fimInscricoes != null && !agora.isBefore(fimInscricoes)) {
                throw new ConflitoOperacaoException("Inscrições fechadas para este evento.");
            }
            if (limite != null) {
                try (var s = c.prepareStatement("SELECT COUNT(*) FROM inscricoes WHERE evento_id = ? AND estado = 'ATIVA'")) {
                    s.setObject(1, eventoId);
                    try (var r = s.executeQuery()) {
                        r.next();
                        if (r.getLong(1) >= limite) throw new ConflitoOperacaoException("Evento sem vagas.");
                    }
                }
            }
            UUID id = inscricaoAnterior == null ? UUID.randomUUID() : inscricaoAnterior;
            if (inscricaoAnterior == null) {
                try (var s = c.prepareStatement("INSERT INTO inscricoes(id, evento_id, usuario_id) VALUES (?, ?, ?)")) {
                    s.setObject(1, id); s.setObject(2, eventoId); s.setObject(3, usuarioId); s.executeUpdate();
                }
            } else {
                try (var s = c.prepareStatement("UPDATE inscricoes SET estado = 'ATIVA', cancelada_em = NULL WHERE id = ?")) {
                    s.setObject(1, id); s.executeUpdate();
                }
            }
            try (var s = c.prepareStatement("INSERT INTO usuario_perfis(usuario_id, perfil) VALUES (?, 'PARTICIPANTE') ON CONFLICT DO NOTHING")) {
                s.setObject(1, usuarioId); s.executeUpdate();
            }
            return new DadosPresenca.Inscricao(id, "ATIVA");
        });
    }
    public DadosPresenca.Inscricao cancelarInscricao(UUID usuarioId, UUID eventoId) {
        return transacao(c -> {
            try (var s = c.prepareStatement("SELECT permitir_cancelamento FROM eventos WHERE id = ? FOR SHARE")) {
                s.setObject(1, eventoId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new RecursoNaoEncontradoException();
                    if (!r.getBoolean(1)) throw new ConflitoOperacaoException("O cancelamento está desabilitado para este evento.");
                }
            }
            UUID inscricaoId;
            String estado;
            try (var s = c.prepareStatement("SELECT id, estado FROM inscricoes WHERE evento_id = ? AND usuario_id = ? FOR UPDATE")) {
                s.setObject(1, eventoId); s.setObject(2, usuarioId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new RecursoNaoEncontradoException();
                    inscricaoId = r.getObject("id", UUID.class);
                    estado = r.getString("estado");
                }
            }
            if ("CANCELADA".equals(estado)) return new DadosPresenca.Inscricao(inscricaoId, "CANCELADA");
            try (var s = c.prepareStatement("DELETE FROM agenda_atividades WHERE usuario_id = ? AND evento_id = ?")) {
                s.setObject(1, usuarioId); s.setObject(2, eventoId); s.executeUpdate();
            }
            try (var s = c.prepareStatement("UPDATE inscricoes SET estado = 'CANCELADA', cancelada_em = ? WHERE id = ?")) {
                s.setTimestamp(1, Timestamp.from(agora(c))); s.setObject(2, inscricaoId); s.executeUpdate();
            }
            return new DadosPresenca.Inscricao(inscricaoId, "CANCELADA");
        });
    }
    public List<DadosPresenca.Atividade> listarAtividades(UUID eventoId) {
        return transacao(c -> {
            var lista = new ArrayList<DadosPresenca.Atividade>();
            try (var s = c.prepareStatement("SELECT id, titulo, inicio, fim, local FROM atividades WHERE evento_id = ? ORDER BY inicio NULLS LAST, titulo")) {
                s.setObject(1, eventoId);
                try (var r = s.executeQuery()) {
                    while (r.next()) lista.add(new DadosPresenca.Atividade(r.getObject("id", UUID.class),
                            r.getString("titulo"), r.getObject("inicio", java.time.LocalDateTime.class),
                            r.getObject("fim", java.time.LocalDateTime.class), r.getString("local")));
                }
            }
            return List.copyOf(lista);
        });
    }
    public void adicionarAtividade(UUID eventoId, Atividade a) {
        transacao(c -> {
            try (var s = c.prepareStatement("SELECT estado FROM eventos WHERE id = ? FOR UPDATE")) {
                s.setObject(1, eventoId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new RecursoNaoEncontradoException();
                    if (List.of("ENCERRADO", "SUSPENSO", "EXCLUIDO").contains(r.getString(1)))
                        throw new ConflitoOperacaoException("Evento indisponível.");
                }
            }
            try (var s = c.prepareStatement("INSERT INTO atividades(id, evento_id, titulo, descricao, inicio, fim, local) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                s.setObject(1, a.getId()); s.setObject(2, eventoId); s.setString(3, a.getTitulo());
                s.setString(4, a.getDescricao()); s.setObject(5, a.getInicio()); s.setObject(6, a.getFim());
                s.setString(7, a.getLocal()); s.executeUpdate();
            }
            return null;
        });
    }
    public UUID eventoDaAtividade(UUID atividadeId) {
        return transacao(c -> {
            try (var s = c.prepareStatement("SELECT evento_id FROM atividades WHERE id = ?")) {
                s.setObject(1, atividadeId);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new RecursoNaoEncontradoException();
                    return r.getObject(1, UUID.class);
                }
            }
        });
    }
    public void limitarTentativas(UUID usuarioId, String operacao) {
        // Transação separada: falhar na confirmação não desfaz a contagem.
        int tentativas = transacao(c -> {
            String sql = """
                INSERT INTO limites_presenca(usuario_id, operacao, janela_em, tentativas)
                VALUES (?, ?, CURRENT_TIMESTAMP, 1)
                ON CONFLICT (usuario_id, operacao) DO UPDATE SET
                  tentativas = CASE WHEN limites_presenca.janela_em <= CURRENT_TIMESTAMP - INTERVAL '1 minute'
                                    THEN 1 ELSE limites_presenca.tentativas + 1 END,
                  janela_em = CASE WHEN limites_presenca.janela_em <= CURRENT_TIMESTAMP - INTERVAL '1 minute'
                                   THEN CURRENT_TIMESTAMP ELSE limites_presenca.janela_em END
                RETURNING tentativas
                """;
            try (var s = c.prepareStatement(sql)) {
                s.setObject(1, usuarioId); s.setString(2, operacao);
                try (var r = s.executeQuery()) { r.next(); return r.getInt(1); }
            }
        });
        if (tentativas > 10) throw new LimiteTentativasException();
    }
    private record Contexto(UUID eventoId, UUID dono, String titulo) {}
    private Contexto bloquearAtividade(Connection c, UUID id) throws SQLException {
        // Emissão e confirmação usam os mesmos locks, incluindo o estado do evento.
        String sql = """
            SELECT a.evento_id, a.titulo, a.politica_frequencia, e.organizador_id, e.estado
            FROM atividades a JOIN eventos e ON e.id = a.evento_id
            WHERE a.id = ? FOR SHARE OF e FOR UPDATE OF a
            """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, id);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new RecursoNaoEncontradoException();
                if (!"PUBLICADO".equals(r.getString("estado"))) throw new ConflitoOperacaoException("A chamada exige evento publicado.");
                if (!"CHECKIN_UNICO".equals(r.getString("politica_frequencia")))
                    throw new ConflitoOperacaoException("Esta atividade usa outra política de frequência.");
                return new Contexto(r.getObject("evento_id", UUID.class), r.getObject("organizador_id", UUID.class), r.getString("titulo"));
            }
        }
    }
    public DadosPresenca.Chamada gerarChamada(UUID usuarioId, UUID atividadeId, String codigoHash) {
        return transacao(c -> {
            Contexto contexto = bloquearAtividade(c, atividadeId);
            try (var s = c.prepareStatement("SELECT perfil FROM usuario_perfis WHERE usuario_id = ?")) {
                s.setObject(1, usuarioId);
                boolean permitido = false;
                try (var r = s.executeQuery()) {
                    while (r.next()) permitido |= "ADMINISTRADOR".equals(r.getString(1))
                            || ("ORGANIZADOR".equals(r.getString(1)) && contexto.dono().equals(usuarioId));
                }
                if (!permitido) throw new AcessoNegadoException();
            }
            Instant inicio = agora(c), fim = inicio.plus(RegrasChamada.VALIDADE);
            try (var s = c.prepareStatement("UPDATE chamadas_presenca SET revogada_em = ? WHERE atividade_id = ? AND revogada_em IS NULL")) {
                s.setTimestamp(1, Timestamp.from(inicio)); s.setObject(2, atividadeId); s.executeUpdate();
            }
            UUID id = UUID.randomUUID();
            try (var s = c.prepareStatement("INSERT INTO chamadas_presenca(id, atividade_id, evento_id, criada_por, codigo_hash, criada_em, expira_em) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                s.setObject(1, id); s.setObject(2, atividadeId); s.setObject(3, contexto.eventoId());
                s.setObject(4, usuarioId); s.setString(5, codigoHash);
                s.setTimestamp(6, Timestamp.from(inicio)); s.setTimestamp(7, Timestamp.from(fim)); s.executeUpdate();
            }
            return new DadosPresenca.Chamada(id, contexto.titulo(), fim);
        });
    }
    public DadosPresenca.Confirmacao confirmar(UUID usuarioId, String codigoHash, OrigemPresenca origem) {
        return transacao(c -> {
            UUID atividadeId;
            try (var s = c.prepareStatement("SELECT atividade_id FROM chamadas_presenca WHERE codigo_hash = ?")) {
                s.setString(1, codigoHash);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("Código inválido ou expirado.");
                    atividadeId = r.getObject(1, UUID.class);
                }
            }
            Contexto contexto = bloquearAtividade(c, atividadeId);
            Instant instante = agora(c);
            UUID chamadaId;
            Instant expiracao;
            boolean revogada;
            try (var s = c.prepareStatement("SELECT id, expira_em, revogada_em FROM chamadas_presenca WHERE codigo_hash = ?")) {
                s.setString(1, codigoHash);
                try (var r = s.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("Código inválido ou expirado.");
                    expiracao = r.getTimestamp("expira_em").toInstant();
                    revogada = r.getTimestamp("revogada_em") != null;
                    RegrasChamada.exigirValida(instante, expiracao, revogada);
                    chamadaId = r.getObject("id", UUID.class);
                }
            }
            UUID inscricaoId;
            try (var s = c.prepareStatement("SELECT id, estado FROM inscricoes WHERE evento_id = ? AND usuario_id = ? FOR UPDATE")) {
                s.setObject(1, contexto.eventoId()); s.setObject(2, usuarioId);
                try (var r = s.executeQuery()) {
                    if (!r.next() || !"ATIVA".equals(r.getString("estado")))
                        throw new ConflitoOperacaoException("É necessário ter inscrição ativa neste evento.");
                    inscricaoId = r.getObject("id", UUID.class);
                }
            }
            // A espera pelo lock da inscrição também consome a validade da chamada.
            instante = agora(c);
            RegrasChamada.exigirValida(instante, expiracao, revogada);
            try (var s = c.prepareStatement("SELECT id, registrada_em FROM presencas WHERE atividade_id = ? AND usuario_id = ?")) {
                s.setObject(1, atividadeId); s.setObject(2, usuarioId);
                try (var r = s.executeQuery()) {
                    if (r.next()) return new DadosPresenca.Confirmacao(r.getObject("id", UUID.class),
                            contexto.titulo(), r.getTimestamp("registrada_em").toInstant(), true);
                }
            }
            UUID id = UUID.randomUUID();
            try (var s = c.prepareStatement("INSERT INTO presencas(id, atividade_id, evento_id, usuario_id, inscricao_id, chamada_id, registrada_em, origem) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                s.setObject(1, id); s.setObject(2, atividadeId); s.setObject(3, contexto.eventoId());
                s.setObject(4, usuarioId); s.setObject(5, inscricaoId); s.setObject(6, chamadaId);
                s.setTimestamp(7, Timestamp.from(instante)); s.setString(8, origem.name()); s.executeUpdate();
            }
            return new DadosPresenca.Confirmacao(id, contexto.titulo(), instante, false);
        });
    }
}
