package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutenticacaoFiltro;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

// Servidor e JWT reais; persistência isolada em memória. Não usa DB_URL nem o banco do usuário.
class ApiWebIntegrationTest {
    private HttpServer servidor;
    private ExecutorService executor;
    private HttpClient cliente;
    private String base;
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    private final JwtTokenProviderAdapter jwt = new JwtTokenProviderAdapter(UUID.randomUUID().toString(), 10);
    private Usuario dono;
    private String token;
    private final PresencasFake presencas = new PresencasFake();
    private static final String DADOS = """
            {"titulo":"Simpósio de teste","descricao":"Descrição suficientemente longa do evento.",
             "local":"Auditório","dataInicio":"2026-10-10T09:00","dataFim":"2026-10-10T18:00"}
            """;

    @BeforeEach void iniciar() throws Exception {
        dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        token = token(dono);
        var senha = new BcryptCodePassAdapter();
        var router = RotasApi.criar(new RegistrarUsuarioUseCase(usuarios, senha),
                new AutenticarUsuarioUseCase(usuarios, senha, jwt),
                new ConsultarMinhaContaUseCase(usuarios), new EventosUseCase(eventos, usuarios), mapper);
        RotasApi.adicionarAdministracao(router, new AdministracaoUseCase(eventos, usuarios), mapper);
        RotasApi.adicionarPresenca(router, new PresencaUseCase(presencas, usuarios, eventos, new CodigoPresencaSeguro()), mapper);
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", new ManipuladorGlobalDeExcessoes(router, mapper))
                .getFilters().add(new AutenticacaoFiltro(jwt, mapper));
        executor = Executors.newSingleThreadExecutor();
        servidor.setExecutor(executor);
        servidor.start();
        base = "http://127.0.0.1:" + servidor.getAddress().getPort();
        cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    }
    @AfterEach void encerrar() {
        if (cliente != null) cliente.close();
        if (servidor != null) servidor.stop(0);
        if (executor != null) executor.shutdownNow();
    }
    private String token(Usuario usuario) {
        return jwt.gerarToken(usuario.getId(), usuario.getPessoa().getEmail(), usuario.getPerfis());
    }
    private HttpResponse<String> pedir(String metodo, String caminho, String auth, String corpo) throws Exception {
        var req = HttpRequest.newBuilder(URI.create(base + caminho)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json");
        if (auth != null) req.header("Authorization", "Bearer " + auth);
        req.method(metodo, corpo == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(corpo));
        return cliente.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }
    private String criar() throws Exception {
        var resposta = pedir("POST", "/eventos", token, DADOS);
        assertEquals(201, resposta.statusCode(), resposta.body());
        return mapper.readTree(resposta.body()).get("id").asText();
    }
    @Test void contaProtegidaNaoExpoeHashENaoVazaContextoEntreRequisicoes() throws Exception {
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var resposta = pedir("GET", "/usuarios/me", token(participante), null);
        assertEquals(200, resposta.statusCode());
        JsonNode conta = mapper.readTree(resposta.body());
        assertEquals(participante.getId().toString(), conta.get("usuarioId").asText());
        assertEquals(4, conta.size());
        assertFalse(resposta.body().contains("senha"));
        assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
        assertEquals(401, pedir("GET", "/usuarios/me", null, null).statusCode());
        assertEquals(401, pedir("GET", "/usuarios/me", "invalido", null).statusCode());
        usuarios.removerPorId(participante.getId());
        assertEquals(401, pedir("GET", "/usuarios/me", token(participante), null).statusCode());
    }
    @Test void cadastroELoginMantemContratoExistente() throws Exception {
        String cadastro = "{\"nome\":\"Conta de teste\",\"email\":\"login@example.test\",\"senha\":\"SenhaTeste123!\"}";
        var registro = pedir("POST", "/usuarios", null, cadastro);
        assertEquals(200, registro.statusCode());
        assertTrue(registro.headers().firstValue("Content-Type").orElseThrow().startsWith("text/plain"));
        var login = pedir("POST", "/auth/login", null, "{\"email\":\"login@example.test\",\"senha\":\"SenhaTeste123!\"}");
        assertEquals(200, login.statusCode(), login.body());
        JsonNode sessao = mapper.readTree(login.body());
        assertTrue(sessao.has("usuarioID"));
        assertEquals("PARTICIPANTE", sessao.get("perfis").get(0).asText());
        assertEquals(200, pedir("GET", "/usuarios/me", sessao.get("token").asText(), null).statusCode());
    }
    @Test void cadastroOrganizadorPermiteCriarEventoMasCadastroAdminEVisitanteSaoRejeitados() throws Exception {
        String baseCadastro = "{\"nome\":\"Conta de teste\",\"email\":\"%s\",\"senha\":\"SenhaTeste123!\",\"perfil\":\"%s\"}";
        assertEquals(400, pedir("POST", "/usuarios", null,
                baseCadastro.formatted("admin@example.test", "ADMINISTRADOR")).statusCode());
        assertEquals(400, pedir("POST", "/usuarios", null,
                baseCadastro.formatted("visitante@example.test", "VISITANTE")).statusCode());
        assertEquals(200, pedir("POST", "/usuarios", null,
                baseCadastro.formatted("organizador@example.test", "ORGANIZADOR")).statusCode());
        var login = pedir("POST", "/auth/login", null,
                "{\"email\":\"organizador@example.test\",\"senha\":\"SenhaTeste123!\"}");
        assertEquals(200, login.statusCode());
        var sessao = mapper.readTree(login.body());
        assertEquals("ORGANIZADOR", sessao.get("perfis").get(0).asText());
        assertEquals(201, pedir("POST", "/eventos", sessao.get("token").asText(), DADOS).statusCode());
    }
    @Test void participacaoExigeJwtERetornaSomenteDadosDoUsuario() throws Exception {
        assertEquals(401, pedir("GET", "/usuarios/me/participacao", null, null).statusCode());
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var inscricao = new DadosPresenca.InscricaoResumo(UUID.randomUUID(), UUID.randomUUID(),
                "Simpósio", null, null, "Auditório", "PUBLICADO", "ATIVA", java.time.Instant.now());
        presencas.participacao = new DadosPresenca.Participacao(java.util.List.of(inscricao), java.util.List.of(), 0);
        var resposta = pedir("GET", "/usuarios/me/participacao", token(participante), null);
        assertEquals(200, resposta.statusCode(), resposta.body());
        assertEquals(participante.getId(), presencas.consultaUsuarioId);
        assertEquals("Simpósio", mapper.readTree(resposta.body()).get("inscricoes").get(0).get("eventoTitulo").asText());
        assertEquals("no-store", resposta.headers().firstValue("Cache-Control").orElseThrow());
    }
    @Test void rascunhoPublicacaoEEncerramentoPorHttp() throws Exception {
        String id = criar();
        assertEquals(404, pedir("GET", "/eventos/" + id, null, null).statusCode());
        assertEquals(0, mapper.readTree(pedir("GET", "/eventos", null, null).body()).size());
        assertEquals(1, mapper.readTree(pedir("GET", "/usuarios/me/eventos", token, null).body()).size());
        assertEquals(200, pedir("POST", "/eventos/" + id + "/publicacao", token, null).statusCode());
        assertEquals(200, pedir("POST", "/eventos/" + id + "/publicacao", token, null).statusCode());
        var detalhe = pedir("GET", "/eventos/" + id, null, null);
        assertEquals(200, detalhe.statusCode());
        assertEquals("2026-10-10T09:00:00", mapper.readTree(detalhe.body()).get("dataInicio").asText());
        assertFalse(detalhe.body().contains("senha"));
        assertFalse(detalhe.body().contains("email"));
        assertEquals(1, mapper.readTree(pedir("GET", "/eventos", null, null).body()).size());
        assertEquals(200, pedir("POST", "/eventos/" + id + "/encerramento", token, null).statusCode());
        assertEquals(409, pedir("POST", "/eventos/" + id + "/publicacao", token, null).statusCode());
        assertEquals(404, pedir("GET", "/eventos/" + id, null, null).statusCode());
    }
    @Test void escritaExigeTokenPerfilEPropriedade() throws Exception {
        assertEquals(401, pedir("POST", "/eventos", null, DADOS).statusCode());
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        assertEquals(403, pedir("POST", "/eventos", token(participante), DADOS).statusCode());
        String id = criar();
        var outro = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        assertEquals(403, pedir("POST", "/eventos/" + id + "/publicacao", token(outro), null).statusCode());
        dono.removerPerfil(Perfil.ORGANIZADOR);
        assertEquals(403, pedir("POST", "/eventos", token, DADOS).statusCode());
    }
    @Test void moderacaoExigeAdminEExclusaoRetiraConteudoSemApagarAuditoria() throws Exception {
        String id = criar();
        var admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        String authAdmin = token(admin);
        String motivo = "{\"motivo\":\"Conteúdo impróprio identificado.\"}";
        assertEquals(401, pedir("GET", "/admin/eventos", null, null).statusCode());
        assertEquals(403, pedir("GET", "/admin/eventos", token(participante), null).statusCode());
        assertEquals(403, pedir("GET", "/admin/eventos", token, null).statusCode());
        assertEquals(1, mapper.readTree(pedir("GET", "/admin/eventos", authAdmin, null).body()).size());
        assertEquals("RASCUNHO", mapper.readTree(pedir("GET", "/admin/eventos/" + id, authAdmin, null).body())
                .get("evento").get("estado").asText());
        assertEquals(403, pedir("POST", "/eventos", authAdmin, DADOS).statusCode());
        assertEquals(403, pedir("DELETE", "/admin/eventos/" + id, token, motivo).statusCode());
        assertEquals(400, pedir("POST", "/admin/eventos/" + id + "/suspensao", authAdmin,
                "{\"motivo\":\"curto\"}").statusCode());
        assertEquals(200, pedir("POST", "/admin/eventos/" + id + "/suspensao", authAdmin, motivo).statusCode());
        assertEquals(409, pedir("POST", "/eventos/" + id + "/publicacao", token, null).statusCode());
        assertEquals(200, pedir("POST", "/admin/eventos/" + id + "/restauracao", authAdmin, motivo).statusCode());
        assertEquals(200, pedir("DELETE", "/admin/eventos/" + id, authAdmin, motivo).statusCode());
        assertEquals(404, pedir("GET", "/eventos/" + id, null, null).statusCode());
        assertFalse(pedir("GET", "/admin/eventos/" + id, authAdmin, null).body().contains("Simpósio de teste"));
        assertEquals(3, mapper.readTree(pedir("GET", "/admin/moderacoes", authAdmin, null).body()).size());
        assertEquals(0, mapper.readTree(pedir("GET", "/usuarios/me/eventos", token, null).body()).size());
    }
    @Test void categoriaDoEventoVaiDoCadastroAoCatalogoEExigePropriedadeParaEdicao() throws Exception {
        String dados = DADOS.replace("\"local\":", "\"categoria\":\"ACADEMICO\",\"local\":");
        var criacao = pedir("POST", "/eventos", token, dados);
        assertEquals(201, criacao.statusCode(), criacao.body());
        String id = mapper.readTree(criacao.body()).get("id").asText();
        assertEquals("ACADEMICO", mapper.readTree(criacao.body()).get("categoria").asText());
        assertEquals(401, pedir("PATCH", "/eventos/" + id + "/categoria", null,
                "{\"categoria\":\"TECNOLOGIA\"}").statusCode());
        var outro = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        assertEquals(403, pedir("PATCH", "/eventos/" + id + "/categoria", token(outro),
                "{\"categoria\":\"TECNOLOGIA\"}").statusCode());
        assertEquals(400, pedir("PATCH", "/eventos/" + id + "/categoria", token,
                "{\"categoria\":\"INVALIDA\"}").statusCode());
        var alteracao = pedir("PATCH", "/eventos/" + id + "/categoria", token,
                "{\"categoria\":\"TECNOLOGIA\"}");
        assertEquals(200, alteracao.statusCode(), alteracao.body());
        assertEquals("TECNOLOGIA", mapper.readTree(alteracao.body()).get("categoria").asText());
        assertEquals(200, pedir("POST", "/eventos/" + id + "/publicacao", token, null).statusCode());
        assertEquals("TECNOLOGIA", mapper.readTree(pedir("GET", "/eventos", null, null).body())
                .get(0).get("categoria").asText());
    }
    @Test void validaJsonUuidMetodosETamanhoDoCorpo() throws Exception {
        assertEquals(400, pedir("GET", "/eventos/123", null, null).statusCode());
        assertEquals(400, pedir("POST", "/eventos", token, "{").statusCode());
        assertEquals(400, pedir("POST", "/eventos", token, "null").statusCode());
        assertEquals(400, pedir("POST", "/eventos", token, "{}").statusCode());
        assertEquals(400, pedir("POST", "/eventos", token, " ".repeat(65537)).statusCode());
        assertEquals(405, pedir("PATCH", "/eventos", token, "{}").statusCode());
        assertEquals(404, pedir("GET", "/inexistente", token, null).statusCode());
    }
    @Test void rotasDeChamadaEConfirmacaoExigemJwtEAutorizacao() throws Exception {
        String atividadeId = UUID.randomUUID().toString();
        assertEquals(401, pedir("POST", "/atividades/" + atividadeId + "/chamadas", null, null).statusCode());
        assertEquals(401, pedir("POST", "/presencas/confirmacoes", "invalido", "{}").statusCode());
        String eventoId = criar();
        presencas.eventoId = UUID.fromString(eventoId);
        var chamada = pedir("POST", "/atividades/" + atividadeId + "/chamadas", token, null);
        assertEquals(201, chamada.statusCode(), chamada.body());
        assertEquals("no-store", chamada.headers().firstValue("Cache-Control").orElseThrow());
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        assertEquals(403, pedir("POST", "/atividades/" + atividadeId + "/chamadas", token(participante), null).statusCode());
        var codigo = mapper.readTree(chamada.body()).get("codigo").asText();
        assertEquals(200, pedir("POST", "/presencas/confirmacoes", token(participante),
                "{\"codigo\":\"" + codigo + "\",\"origem\":\"QR\"}").statusCode());
        assertEquals(400, pedir("POST", "/presencas/confirmacoes", token(participante),
                "{\"codigo\":\"123\",\"origem\":\"CODIGO\"}").statusCode());
        assertEquals(405, pedir("GET", "/presencas/confirmacoes", token, null).statusCode());
        assertEquals(200, pedir("POST", "/eventos/" + eventoId + "/inscricoes", token(participante), null).statusCode());
        assertEquals(403, pedir("GET", "/eventos/" + eventoId + "/atividades", token(participante), null).statusCode());
    }
}
