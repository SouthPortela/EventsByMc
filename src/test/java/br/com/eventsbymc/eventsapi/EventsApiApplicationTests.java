package br.com.eventsbymc.eventsapi;

import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.AutenticacaoHandler;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.ManipuladorGlobalDeExcessoes;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.Router;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.UsuarioHandler;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

//sem Spring não tem "contexto" pra carregar, então esse teste sobe o HttpServer
//de verdade numa porta livre e bate nele com o HttpClient do próprio Java.
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class EventsApiApplicationTests {

    private static HttpServer server;
    private static int porta;
    private static CompositionRoot raiz;
    private static String emailDeTeste;
    private static String tokenValido;

    @BeforeAll
    static void subirServidor() throws Exception {
        raiz = CompositionRoot.montar();
        Router router = new Router()
                .registrar("POST", "/usuarios", new UsuarioHandler(raiz.registrarUsuarioUseCase, raiz.objectMapper))
                .registrar("POST", "/auth/login", new AutenticacaoHandler(raiz.autenticarUsuario, raiz.objectMapper));
        ManipuladorGlobalDeExcessoes handler = new ManipuladorGlobalDeExcessoes(router, raiz.objectMapper);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", handler).getFilters().add(new AutenticacaoFiltro(raiz.tokenProvider, raiz.objectMapper));
        server.setExecutor(Executors.newFixedThreadPool(4));
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
        Optional<br.com.eventsbymc.eventsapi.domain.model.Usuario> usuario = raiz.usuarioRepository.buscarPorEmail(emailDeTeste);
        usuario.ifPresent(u -> raiz.usuarioRepository.removerPorId(u.getId()));
        server.stop(0);
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

    private static String extrairToken(String corpoJson) {
        int inicio = corpoJson.indexOf("\"token\":\"") + "\"token\":\"".length();
        int fim = corpoJson.indexOf('"', inicio);
        return corpoJson.substring(inicio, fim);
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
