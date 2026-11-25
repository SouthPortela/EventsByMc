package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.out.AdministracaoRepository;
import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.JwtTokenProviderAdapter;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutenticacaoFiltro;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AdministracaoUseCaseTest {
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    private final Usuario admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
    private final Usuario dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
    private final Fake persistencia = new Fake();
    private final AdministracaoUseCase caso = new AdministracaoUseCase(usuarios, eventos, persistencia);
    private final EventosUseCase gestao = new EventosUseCase(eventos, usuarios);
    private Evento evento(EstadoEvento estado) {
        var inicio = LocalDateTime.of(2026, 11, 25, 9, 0);
        var e = new Evento("Evento teste", "Descricao de um evento de teste.", dono, inicio, inicio.plusHours(2), "Sala");
        if (estado != EstadoEvento.RASCUNHO) e.publicar();
        if (estado == EstadoEvento.ENCERRADO) e.encerrar();
        return eventos.salvar(e);
    }
    private final class Fake implements AdministracaoRepository {
        final List<Registro> registros = new ArrayList<>();
        public Resumo resumo() { return new Resumo(usuarios.listar().size(), 1, 0, 0, 0); }
        public List<Registro> auditoria() { return List.copyOf(registros); }
        public void alterarPerfis(UUID responsavel, UUID alvo, Set<Perfil> novos) {
            var usuario = usuarios.buscarPorId(alvo).orElseThrow(RecursoNaoEncontradoException::new);
            for (var perfil : novos) usuario.adicionarPerfil(perfil);
            for (var perfil : Set.copyOf(usuario.getPerfis())) if (!novos.contains(perfil)) usuario.removerPerfil(perfil);
        }
        public void alterarBloqueio(UUID responsavel, Evento e, EstadoEvento esperado) {
            assertEquals(esperado, eventos.buscarPorId(e.getId()).orElseThrow().getEstado());
            eventos.salvar(e);
            registros.add(new Registro(UUID.randomUUID(), responsavel, e.getEstado().name(), e.getId(),
                Objects.toString(e.getMotivoBloqueio(), "Desbloqueado"), Instant.now()));
        }
    }
    @Test void bloqueiaComMotivoVisivelSomenteNaGestaoERestauraTodosOsEstados() {
        for (var estado : List.of(EstadoEvento.RASCUNHO, EstadoEvento.PUBLICADO, EstadoEvento.ENCERRADO)) {
            var e = evento(estado);
            var resultado = caso.bloquear(admin.getId(), e.getId(), "  Informacoes do local precisam ser corrigidas.  ");
            assertEquals(EstadoEvento.BLOQUEADO, resultado.evento().estado());
            assertEquals("Informacoes do local precisam ser corrigidas.", resultado.evento().motivoBloqueio());
            assertEquals(dono.getId(), resultado.organizadorId());
            var encontrado = gestao.listarDoOrganizador(dono.getId()).stream().filter(x -> x.id().equals(e.getId())).findFirst().orElseThrow();
            assertEquals(resultado.evento().motivoBloqueio(), encontrado.motivoBloqueio());
            assertTrue(gestao.listarPublicados().stream().noneMatch(x -> x.id().equals(e.getId())));
            assertThrows(RecursoNaoEncontradoException.class, () -> gestao.consultarPublicado(e.getId()));
            assertThrows(ConflitoOperacaoException.class, () -> gestao.alterarEstado(dono.getId(), e.getId(), EstadoEvento.PUBLICADO));
            assertThrows(ConflitoOperacaoException.class, () -> gestao.alterarCategoria(dono.getId(), e.getId(), CategoriaEvento.OUTROS));
            assertEquals(estado, caso.desbloquear(admin.getId(), e.getId()).evento().estado());
            assertNull(eventos.buscarPorId(e.getId()).orElseThrow().getMotivoBloqueio());
        }
        assertEquals(6, caso.auditoria(admin.getId()).size());
    }
    @Test void apenasAdministradorAtualPodeOperarMesmoComTokenAntigo() {
        var e = evento(EstadoEvento.PUBLICADO);
        assertThrows(AcessoNegadoException.class, () -> caso.usuarios(dono.getId()));
        assertThrows(AcessoNegadoException.class, () -> caso.eventos(dono.getId()));
        assertThrows(AcessoNegadoException.class, () -> caso.bloquear(dono.getId(), e.getId(), "Motivo"));
        assertThrows(AcessoNegadoException.class, () -> caso.desbloquear(dono.getId(), e.getId()));
        assertThrows(AcessoNegadoException.class, () -> caso.alterarPerfis(dono.getId(), dono.getId(), Set.of(Perfil.ADMINISTRADOR)));
        admin.removerPerfil(Perfil.ADMINISTRADOR);
        assertThrows(AcessoNegadoException.class, () -> caso.resumo(admin.getId()));
    }
    @Test void validaMotivoETransicoesSemPersistirAlteracaoInvalida() {
        var e = evento(EstadoEvento.PUBLICADO);
        for (String motivo : Arrays.asList(null, " ", "a".repeat(2001))) {
            assertThrows(IllegalArgumentException.class, () -> caso.bloquear(admin.getId(), e.getId(), motivo));
        }
        assertThrows(ConflitoOperacaoException.class, () -> caso.desbloquear(admin.getId(), e.getId()));
        assertEquals(0, persistencia.registros.size());
        caso.bloquear(admin.getId(), e.getId(), "Motivo valido");
        assertThrows(ConflitoOperacaoException.class, () -> caso.bloquear(admin.getId(), e.getId(), "Outro motivo"));
        assertEquals(1, persistencia.registros.size());
    }
    @Test void perfisMultiplosSemAprovacaoEProtecaoDoUltimoAdministrador() {
        var novos = Set.of(Perfil.PARTICIPANTE, Perfil.ORGANIZADOR);
        assertEquals(novos, caso.alterarPerfis(admin.getId(), dono.getId(), novos).perfis());
        assertThrows(IllegalArgumentException.class, () -> caso.alterarPerfis(admin.getId(), dono.getId(), Set.of()));
        assertThrows(IllegalArgumentException.class, () -> caso.alterarPerfis(admin.getId(), dono.getId(), Set.of(Perfil.VISITANTE)));
        assertThrows(IllegalArgumentException.class, () -> PoliticaPerfis.validar(new HashSet<>(Arrays.asList((Perfil) null))));
        assertThrows(IllegalStateException.class, () -> PoliticaPerfis.exigirAdministradorRestante(Set.of(Perfil.ADMINISTRADOR), novos, 1));
        assertDoesNotThrow(() -> PoliticaPerfis.exigirAdministradorRestante(Set.of(Perfil.ADMINISTRADOR), novos, 2));
    }
    @Test void apiExigeJwtRoleAtualUuidENaoExpoeHash() throws Exception {
        var mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        var jwt = new JwtTokenProviderAdapter("segredo-apenas-de-teste-123456789012345", 10);
        var router = new Router();
        AdministracaoHandler.registrar(router, caso, mapper);
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        var context = server.createContext("/", new ManipuladorGlobalDeExcessoes(router, mapper));
        context.getFilters().add(new AutenticacaoFiltro(jwt, mapper));
        server.start();
        try {
            var client = HttpClient.newHttpClient();
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            var adminToken = jwt.gerarToken(admin.getId(), admin.getPessoa().getEmail(), admin.getPerfis());
            var donoToken = jwt.gerarToken(dono.getId(), dono.getPessoa().getEmail(), dono.getPerfis());
            assertEquals(401, client.send(HttpRequest.newBuilder(URI.create(base + "/admin/usuarios")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(403, client.send(HttpRequest.newBuilder(URI.create(base + "/admin/usuarios")).header("Authorization", "Bearer " + donoToken).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            var resposta = client.send(HttpRequest.newBuilder(URI.create(base + "/admin/usuarios")).header("Authorization", "Bearer " + adminToken).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, resposta.statusCode());
            assertFalse(resposta.body().contains("hash"));
            assertTrue(resposta.body().contains(admin.getId().toString()));
            var evento = evento(EstadoEvento.PUBLICADO);
            String caminho = base + "/admin/eventos/" + evento.getId() + "/bloqueio";
            var bloqueio = client.send(HttpRequest.newBuilder(URI.create(caminho)).header("Authorization", "Bearer " + adminToken)
                .header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{\"motivo\":\"=1+1\\nCorrigir local\"}")).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, bloqueio.statusCode());
            assertEquals("BLOQUEADO", mapper.readTree(bloqueio.body()).get("evento").get("estado").asText());
            var csv = client.send(HttpRequest.newBuilder(URI.create(base + "/admin/auditoria.csv")).header("Authorization", "Bearer " + adminToken).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, csv.statusCode());
            assertTrue(csv.headers().firstValue("Content-Type").orElseThrow().contains("text/csv"));
            assertTrue(csv.body().contains("\"'=1+1\nCorrigir local\""));
            assertEquals(200, client.send(HttpRequest.newBuilder(URI.create(caminho)).header("Authorization", "Bearer " + adminToken).DELETE().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            assertEquals(EstadoEvento.PUBLICADO, eventos.buscarPorId(evento.getId()).orElseThrow().getEstado());
            assertEquals(400, client.send(HttpRequest.newBuilder(URI.create(base + "/admin/eventos/123/bloqueio")).header("Authorization", "Bearer " + adminToken).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString("{\"motivo\":\"Motivo\"}")).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            admin.removerPerfil(Perfil.ADMINISTRADOR);
            assertEquals(403, client.send(HttpRequest.newBuilder(URI.create(base + "/admin/usuarios")).header("Authorization", "Bearer " + adminToken).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
        } finally { server.stop(0); }
    }
}
