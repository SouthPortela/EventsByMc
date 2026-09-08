package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.MetodoNaoSuportadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutorizacaoInterceptor;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Substitui o DispatcherServlet do Spring MVC: mantém uma tabela de rotas (método HTTP +
 * caminho exato -> handler) e decide, pra cada requisição, se existe rota (senão 404), se o
 * método bate (senão 405), e se o handler exige perfil (@RequerPerfil) antes de delegar a
 * ele de fato.
 */
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
            if (!rota.caminho().equals(caminho)) {
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
}
