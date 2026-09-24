package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesFrequencia;
import br.com.eventsbymc.eventsapi.application.usecase.DadosFrequencia;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.UUID;

public final class FrequenciaHandler implements HttpHandler {
    private final OperacoesFrequencia caso;
    private final ObjectMapper mapper;
    public FrequenciaHandler(OperacoesFrequencia caso, ObjectMapper mapper) { this.caso = caso; this.mapper = mapper; }
    @Override public void handle(HttpExchange exchange) throws IOException {
        UUID usuarioId = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
        UUID atividadeId = Router.uuidRecurso(exchange);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        if (exchange.getRequestMethod().equals("GET"))
            HttpRespostas.enviarJson(exchange, 200, caso.listar(usuarioId, atividadeId), mapper);
        else HttpRespostas.enviarJson(exchange, 201, caso.registrar(usuarioId, atividadeId,
                HttpRespostas.lerJson(exchange, DadosFrequencia.NovoRegistro.class, mapper)), mapper);
    }
}
