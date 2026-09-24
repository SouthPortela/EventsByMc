package br.com.eventsbymc.eventsapi;

import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.ManipuladorGlobalDeExcessoes;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.Router;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.RotasApi;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutenticacaoFiltro;
import br.com.eventsbymc.eventsapi.infrastructure.config.CompositionRoot;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public final class EventsApiApplication {

    public static void main(String[] args) throws IOException {
        CompositionRoot raiz = CompositionRoot.montar();

        Router router = RotasApi.criar(raiz.registrarUsuarioUseCase, raiz.autenticarUsuario,
                raiz.consultarMinhaConta, raiz.eventos, raiz.objectMapper);
        RotasApi.adicionarPresenca(router, raiz.presencas, raiz.objectMapper);
        RotasApi.adicionarProgramacao(router, raiz.programacao, raiz.objectMapper);
        RotasApi.adicionarRelatorios(router, raiz.relatorios, raiz.objectMapper);
        RotasApi.adicionarAvaliacoes(router, raiz.avaliacoes, raiz.objectMapper);
        RotasApi.adicionarAvaliacoesAtividade(router, raiz.avaliacoes, raiz.objectMapper);
        RotasApi.adicionarInteracao(router, raiz.interacao, raiz.objectMapper);
        RotasApi.adicionarCertificados(router, raiz.certificados, raiz.objectMapper);
        RotasApi.adicionarFrequencia(router, raiz.frequencia, raiz.objectMapper);

        ManipuladorGlobalDeExcessoes handlerComTratamentoDeErro =
                new ManipuladorGlobalDeExcessoes(router, raiz.objectMapper);

        int porta = Integer.parseInt(System.getenv().getOrDefault("SERVER_PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);

        //um contexto raiz só, pra pegar toda requisição, com o filtro de autenticação nele
        HttpContext contextoRaiz = server.createContext("/", handlerComTratamentoDeErro);
        contextoRaiz.getFilters().add(new AutenticacaoFiltro(raiz.tokenProvider, raiz.objectMapper));

        //sem isso o HttpServer atende uma requisição de cada vez, na mesma thread
        server.setExecutor(Executors.newFixedThreadPool(Math.max(8, Runtime.getRuntime().availableProcessors() * 2)));
        server.start();

        System.out.println("EventsByMc API ouvindo na porta " + porta + " (com.sun.net.httpserver, sem Spring)");
    }

    private EventsApiApplication() {
    }
}
