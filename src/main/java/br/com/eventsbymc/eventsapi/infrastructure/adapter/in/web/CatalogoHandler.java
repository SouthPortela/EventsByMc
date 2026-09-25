package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public final class CatalogoHandler implements HttpHandler {
    private final OperacoesEvento eventos;
    private final ObjectMapper mapper;
    private final boolean detalhe;

    public CatalogoHandler(OperacoesEvento eventos, ObjectMapper mapper, boolean detalhe) {
        this.eventos = eventos;
        this.mapper = mapper;
        this.detalhe = detalhe;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Object resposta = detalhe ? eventos.consultarPublicado(Router.uuidEvento(exchange)) : eventos.listarPublicados();
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange, 200, resposta, mapper);
    }
}
