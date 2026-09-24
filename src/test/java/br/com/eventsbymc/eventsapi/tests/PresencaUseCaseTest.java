package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.CodigoPresencaSeguro;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PresencaUseCaseTest {
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    private final PresencasFake repo = new PresencasFake();
    private final CodigoPresencaSeguro codigos = new CodigoPresencaSeguro();
    private final PresencaUseCase caso = new PresencaUseCase(repo, usuarios, eventos, codigos);
    private final Usuario dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
    private final LocalDateTime inicio = LocalDateTime.of(2026, 10, 10, 9, 0);
    private Evento evento() {
        var e = new Evento("Simpósio", "Descrição", dono, inicio, inicio.plusHours(3), "Auditório");
        eventos.salvar(e); repo.eventoId = e.getId(); return e;
    }
    @Test void apenasDonoOuAdminGeraChamada() {
        evento();
        var outro = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        assertThrows(AcessoNegadoException.class, () -> caso.gerar(outro.getId(), UUID.randomUUID()));
        var resposta = caso.gerar(dono.getId(), UUID.randomUUID());
        assertEquals(12, resposta.codigo().length());
        assertEquals(codigos.hash(resposta.codigo()), repo.hashRecebido);
        assertFalse(repo.hashRecebido.contains(resposta.codigo()));
        assertEquals(1, repo.tentativas);
        dono.removerPerfil(Perfil.ORGANIZADOR);
        assertThrows(AcessoNegadoException.class, () -> caso.gerar(dono.getId(), UUID.randomUUID()));
    }
    @Test void participanteNaoGeraMasPodeConfirmarComCodigoNormalizado() {
        evento();
        var pessoa = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        assertThrows(AcessoNegadoException.class, () -> caso.gerar(pessoa.getId(), UUID.randomUUID()));
        caso.confirmar(pessoa.getId(), "abcd-efgh-jkmn", OrigemPresenca.CODIGO);
        assertEquals(codigos.hash("ABCDEFGHJKMN"), repo.hashRecebido);
        assertThrows(IllegalArgumentException.class, () -> caso.confirmar(pessoa.getId(), "123", OrigemPresenca.QR));
        assertEquals(2, repo.tentativas, "Código com formato inválido também consome tentativa.");
        assertThrows(TokenInvalidoException.class, () -> caso.confirmar(UUID.randomUUID(), "ABCDEFGHJKMN", OrigemPresenca.QR));
    }
    @Test void criacaoDeAtividadeValidaPeriodoEPropriedade() {
        var e = evento();
        var dados = new DadosPresenca.NovaAtividade("Palestra", "Descrição", inicio, inicio.plusHours(1), "Sala");
        assertNotNull(caso.criarAtividade(dono.getId(), e.getId(), dados).id());
        assertEquals("Palestra", repo.atividade.getTitulo());
        assertThrows(IllegalArgumentException.class, () -> caso.criarAtividade(dono.getId(), e.getId(),
                new DadosPresenca.NovaAtividade("Outra", "", inicio.minusHours(1), inicio.plusHours(1), "Sala")));
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        assertThrows(AcessoNegadoException.class, () -> caso.listarAtividades(participante.getId(), e.getId()));
        assertThrows(AcessoNegadoException.class, () -> caso.criarAtividade(participante.getId(), e.getId(), dados));
    }
    @Test void participantePodeSolicitarInscricao() {
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        assertEquals("ATIVA", caso.inscrever(participante.getId(), UUID.randomUUID()).estado());
        assertThrows(TokenInvalidoException.class, () -> caso.inscrever(UUID.randomUUID(), UUID.randomUUID()));
    }
    @Test void resumoDaParticipacaoUsaIdentidadeAutenticada() {
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var resultado = caso.consultarParticipacao(participante.getId());
        assertEquals(participante.getId(), repo.consultaUsuarioId);
        assertTrue(resultado.inscricoes().isEmpty());
        assertThrows(TokenInvalidoException.class, () -> caso.consultarParticipacao(UUID.randomUUID()));
    }
    @Test void validadeNaoIncluiInstanteDaExpiracaoENaoAceitaRevogacao() {
        Instant inicio = Instant.parse("2026-10-10T12:00:00Z");
        Instant fim = inicio.plus(RegrasChamada.VALIDADE);
        assertDoesNotThrow(() -> RegrasChamada.exigirValida(fim.minusNanos(1), fim, false));
        assertThrows(IllegalArgumentException.class, () -> RegrasChamada.exigirValida(fim, fim, false));
        assertThrows(IllegalArgumentException.class, () -> RegrasChamada.exigirValida(inicio, fim, true));
        assertEquals(Duration.ofMinutes(5), RegrasChamada.VALIDADE);
    }
    @Test void codigoSeguroTemFormatoEsperadoEHashDeterministico() {
        var gerados = new HashSet<String>();
        for (int i = 0; i < 100; i++) {
            String codigo = codigos.gerar();
            assertTrue(codigo.matches("[0-9A-HJKMNP-TV-Z]{12}"));
            assertTrue(codigos.hash(codigo).matches("[0-9a-f]{64}"));
            gerados.add(codigo);
        }
        assertEquals(100, gerados.size());
        assertEquals(codigos.hash("ABCDEFGHJKMN"), codigos.hash("abcd-efgh-jkmn"));
        assertThrows(IllegalArgumentException.class, () -> codigos.hash("IIIIIIIIIIII"));
        assertThrows(IllegalArgumentException.class, () -> codigos.hash(null));
        assertThrows(IllegalArgumentException.class, () -> codigos.hash("X".repeat(33)));
    }
}
