package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;
import br.com.eventsbymc.eventsapi.application.port.in.AtualizarMinhaConta;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import java.io.IOException;
public final class AtualizarContaHandler implements HttpHandler {
    public record Entrada(String nome, String email) {}
    private final AtualizarMinhaConta caso; private final ObjectMapper mapper;
    public AtualizarContaHandler(AtualizarMinhaConta caso, ObjectMapper mapper) { this.caso = caso; this.mapper = mapper; }
    public void handle(HttpExchange e) throws IOException {
        var id = ContextoAutenticacao.obter().orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
        var entrada = HttpRespostas.lerJson(e, Entrada.class, mapper);
        if (entrada == null) throw new IllegalArgumentException("Informe os dados da conta.");
        e.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(e, 200, caso.atualizar(id, entrada.nome(), entrada.email()), mapper);
    }
}
