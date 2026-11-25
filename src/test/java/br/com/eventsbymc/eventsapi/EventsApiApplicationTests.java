package br.com.eventsbymc.eventsapi;

import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.ManipuladorGlobalDeExcessoes;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.Router;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.RotasApi;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutenticacaoFiltro;
import br.com.eventsbymc.eventsapi.infrastructure.config.CompositionRoot;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//sem Spring não tem "contexto" pra carregar, então esse teste sobe o HttpServer
//de verdade numa porta livre e bate nele com o HttpClient do próprio Java.
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class EventsApiApplicationTests {

    private static HttpServer server;
    private static ExecutorService executor;
    private static int porta;
    private static CompositionRoot raiz;
    private static String emailDeTeste;
    private static String tokenValido;

    @BeforeAll
    static void subirServidor() throws Exception {
        raiz = CompositionRoot.montar();
        Router router = RotasApi.criar(raiz.registrarUsuarioUseCase, raiz.autenticarUsuario,
                raiz.consultarMinhaConta, raiz.eventos, raiz.objectMapper);
        ManipuladorGlobalDeExcessoes handler = new ManipuladorGlobalDeExcessoes(router, raiz.objectMapper);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", handler).getFilters().add(new AutenticacaoFiltro(raiz.tokenProvider, raiz.objectMapper));
        executor = Executors.newFixedThreadPool(4);
        server.setExecutor(executor);
        server.start();
        porta = server.getAddress().getPort();

        //cadastra e loga um usuário de teste real pra ter um token válido nos testes que
        //precisam passar pelo AutenticacaoFiltro antes de chegar no Router
        emailDeTeste = "teste-smoke-" + UUID.randomUUID() + "@example.com";
        enviarSemAutenticacao("POST", "/usuarios",
                "{\"nome\":\"Smoke Test\",\"email\":\"" + emailDeTeste + "\",\"senha\":\"senha1234\"}");
        HttpResponse<String> respostaLogin = enviarSemAutenticacao("POST", "/auth/login",
                "{\"email\":\"" + emailDeTeste + "\",\"senha\":\"senha1234\"}");
        tokenValido = extrairToken(respostaLogin.body());
    }

    @AfterAll
    static void pararServidorELimparUsuarioDeTeste() {
        try {
            if (raiz != null && emailDeTeste != null) {
                raiz.usuarioRepository.buscarPorEmail(emailDeTeste)
                        .ifPresent(u -> raiz.usuarioRepository.removerPorId(u.getId()));
            }
        } finally {
            if (server != null) server.stop(0);
            if (executor != null) executor.shutdownNow();
        }
    }

    @Test
    void rotaInexistenteComTokenValidoRetorna404() throws Exception {
        //sem token essa mesma chamada dá 401, porque o filtro bloqueia antes do Router
        //decidir se a rota existe — por isso precisa do token válido aqui
        HttpResponse<String> resposta = enviarComToken("GET", "/rota/que/nao/existe", null, tokenValido);
        assertEquals(404, resposta.statusCode());
        assertTrue(resposta.body().contains("Recurso não encontrado."));
    }

    @Test
    void rotaSemTokenRetorna401() throws Exception {
        HttpResponse<String> resposta = enviarSemAutenticacao("GET", "/rota/que/nao/existe", null);
        assertEquals(401, resposta.statusCode());
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        String corpo = "{\"email\":\"inexistente@teste.com\",\"senha\":\"qualquer123\"}";
        HttpResponse<String> resposta = enviarSemAutenticacao("POST", "/auth/login", corpo);
        assertEquals(401, resposta.statusCode());
        assertTrue(resposta.body().contains("E-mail ou senha inválidos."));
    }

    @Test
    void consultaMinhaContaPelaRotaCompartilhada() throws Exception {
        var resposta = enviarComToken("GET", "/usuarios/me", null, tokenValido);
        assertEquals(200, resposta.statusCode());
        var conta = raiz.objectMapper.readTree(resposta.body());
        assertEquals(emailDeTeste, conta.get("email").asText());
        assertTrue(!resposta.body().contains("senha_hash"));
    }

    private static String extrairToken(String corpoJson) throws IOException {
        return raiz.objectMapper.readTree(corpoJson).get("token").asText();
    }

    private static HttpResponse<String> enviarSemAutenticacao(String metodo, String caminho, String corpo) throws Exception {
        return enviarComToken(metodo, caminho, corpo, null);
    }

    private static HttpResponse<String> enviarComToken(String metodo, String caminho, String corpo, String token) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + porta + caminho));
        builder = corpo == null
                ? builder.method(metodo, HttpRequest.BodyPublishers.noBody())
                : builder.method(metodo, HttpRequest.BodyPublishers.ofString(corpo));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}
