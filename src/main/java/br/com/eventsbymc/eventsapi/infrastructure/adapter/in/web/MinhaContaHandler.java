package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public final class MinhaContaHandler implements HttpHandler {
    private final ConsultarMinhaConta consulta;
    private final ObjectMapper mapper;

    public MinhaContaHandler(ConsultarMinhaConta consulta, ObjectMapper mapper) {
        this.consulta = consulta;
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var claims = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange, 200, consulta.executar(claims.usuarioId()), mapper);
    }
}
