package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.MetodoNaoSuportadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutorizacaoInterceptor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//guarda as rotas (método + caminho -> handler) e decide se existe rota (senão 404),
//se o método bate (senão 405), e chama o AutorizacaoInterceptor antes de delegar de fato.
public final class Router implements HttpHandler {

    private record Rota(String metodo, String caminho, HttpHandler handler) {
    }

    private final List<Rota> rotas = new ArrayList<>();
    private final AutorizacaoInterceptor autorizacaoInterceptor = new AutorizacaoInterceptor();

    public Router registrar(String metodo, String caminho, HttpHandler handler) {
        rotas.add(new Rota(metodo, caminho, handler));
        return this;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String caminho = exchange.getRequestURI().getPath();
        String metodo = exchange.getRequestMethod();

        boolean caminhoExiste = false;
        for (Rota rota : rotas) {
            if (!corresponde(rota.caminho(), caminho)) {
                continue;
            }
            caminhoExiste = true;
            if (!rota.metodo().equals(metodo)) {
                continue;
            }
            autorizacaoInterceptor.verificar(rota.handler(), exchange);
            rota.handler().handle(exchange);
            return;
        }
        if (caminhoExiste) {
            throw new MetodoNaoSuportadoException();
        }
        throw new RecursoNaoEncontradoException();
    }

    public static boolean corresponde(String modelo, String caminho) {
        String[] esperado = modelo.split("/", -1);
        String[] recebido = caminho.split("/", -1);
        if (esperado.length != recebido.length) return false;
        for (int i = 0; i < esperado.length; i++) {
            if (esperado[i].startsWith("{") && esperado[i].endsWith("}")) {
                if (recebido[i].isBlank()) return false;
            } else if (!esperado[i].equals(recebido[i])) return false;
        }
        return true;
    }

    public static UUID uuidEvento(HttpExchange exchange) {
        return uuidRecurso(exchange);
    }

    public static UUID uuidRecurso(HttpExchange exchange) {
        String[] partes = exchange.getRequestURI().getPath().split("/", -1);
        if (partes.length < 3 || !partes[2].matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new IllegalArgumentException("Identificador do recurso inválido.");
        }
        return UUID.fromString(partes[2]);
    }
}
