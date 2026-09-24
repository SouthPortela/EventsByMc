package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesAvaliacao;
import br.com.eventsbymc.eventsapi.application.usecase.DadosAvaliacao;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public final class AvaliacaoHandler implements HttpHandler {
    public enum Acao { CONSULTAR, CONSULTAR_GESTAO, CRIAR, QUESTAO, PUBLICAR, RESPONDER, RESPONDEU, RESULTADOS }
    public record NovoQuestionario(String titulo) {}
    private final OperacoesAvaliacao caso;
    private final ObjectMapper mapper;
    private final Acao acao;
    public AvaliacaoHandler(OperacoesAvaliacao caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso; this.mapper = mapper; this.acao = acao;
    }
    private static UUID usuario() {
        return ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
    }
    @Override public void handle(HttpExchange exchange) throws IOException {
        UUID eventoId = Router.uuidEvento(exchange);
        Object resposta = switch (acao) {
            case CONSULTAR -> caso.consultar(eventoId);
            case CONSULTAR_GESTAO -> caso.consultarGestao(usuario(), eventoId);
            case CRIAR -> caso.criar(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, NovoQuestionario.class, mapper).titulo());
            case QUESTAO -> caso.adicionarQuestao(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosAvaliacao.NovaQuestao.class, mapper));
            case PUBLICAR -> caso.publicar(usuario(), eventoId);
            case RESPONDER -> Map.of("id", caso.responder(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosAvaliacao.Envio.class, mapper)));
            case RESPONDEU -> Map.of("respondeu", caso.respondeu(usuario(), eventoId));
            case RESULTADOS -> caso.resultados(usuario(), eventoId);
        };
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange,
                acao == Acao.CRIAR || acao == Acao.QUESTAO || acao == Acao.RESPONDER ? 201 : 200, resposta, mapper);
    }
}
