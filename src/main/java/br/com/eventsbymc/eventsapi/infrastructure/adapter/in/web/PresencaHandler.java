package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesPresenca;
import br.com.eventsbymc.eventsapi.application.usecase.DadosPresenca;
import br.com.eventsbymc.eventsapi.domain.model.OrigemPresenca;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import java.io.IOException;

public final class PresencaHandler implements HttpHandler {
    public enum Acao { INSCREVER, LISTAR_ATIVIDADES, CRIAR_ATIVIDADE, GERAR, CONFIRMAR }
    public record EntradaConfirmacao(String codigo, OrigemPresenca origem) {}
    private final OperacoesPresenca caso;
    private final ObjectMapper mapper;
    private final Acao acao;
    public PresencaHandler(OperacoesPresenca caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso; this.mapper = mapper; this.acao = acao;
    }
    public void handle(HttpExchange exchange) throws IOException {
        var usuarioId = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        Object resultado = switch (acao) {
            case INSCREVER -> caso.inscrever(usuarioId, Router.uuidEvento(exchange));
            case LISTAR_ATIVIDADES -> caso.listarAtividades(usuarioId, Router.uuidEvento(exchange));
            case CRIAR_ATIVIDADE -> caso.criarAtividade(usuarioId, Router.uuidEvento(exchange),
                    HttpRespostas.lerJson(exchange, DadosPresenca.NovaAtividade.class, mapper));
            case GERAR -> caso.gerar(usuarioId, Router.uuidRecurso(exchange));
            case CONFIRMAR -> {
                var entrada = HttpRespostas.lerJson(exchange, EntradaConfirmacao.class, mapper);
                yield caso.confirmar(usuarioId, entrada.codigo(), entrada.origem());
            }
        };
        HttpRespostas.enviarJson(exchange, acao == Acao.GERAR || acao == Acao.CRIAR_ATIVIDADE ? 201 : 200, resultado, mapper);
    }
}
