package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesCertificado;
import br.com.eventsbymc.eventsapi.application.usecase.DadosCertificado;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public final class CertificadoHandler implements HttpHandler {
    public enum Acao { PARTICIPANTE_PDF, PARTICIPANTE_ENVIO, PESSOA_PDF, PESSOA_ENVIO }
    private final OperacoesCertificado caso;
    private final ObjectMapper mapper;
    private final Acao acao;
    public CertificadoHandler(OperacoesCertificado caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso; this.mapper = mapper; this.acao = acao;
    }
    private static UUID usuario() {
        return ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
    }
    private static UUID pessoa(HttpExchange exchange) {
        try { return UUID.fromString(exchange.getRequestURI().getPath().split("/")[5]); }
        catch (RuntimeException e) { throw new IllegalArgumentException("Pessoa inválida."); }
    }
    private static String papel(HttpExchange exchange) {
        String[] partes = exchange.getRequestURI().getPath().split("/");
        if (partes.length < 7) throw new IllegalArgumentException("Papel inválido.");
        return partes[6];
    }
    private static byte[] pdf(DadosCertificado c) {
        String data = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.of("America/Sao_Paulo")).format(c.emitidoEm());
        String descricao = "PARTICIPANTE".equals(c.tipo())
                ? "participou do evento e cumpriu a frequência mínima exigida."
                : "atuou como " + c.tipo().toLowerCase(java.util.Locale.ROOT) + " no evento.";
        return PdfSimples.gerar("EventsByMc - " + ("PARTICIPANTE".equals(c.tipo()) ? "Certificado" : "Declaração"),
                List.of("Certificamos que " + c.destinatario(), descricao,
                        "Evento: " + c.eventoTitulo(), "Emitido em: " + data,
                        "Código de autenticidade: " + c.id()));
    }
    private static void enviarPdf(HttpExchange exchange, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/pdf");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"certificado.pdf\"");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var saida = exchange.getResponseBody()) { saida.write(bytes); }
    }
    @Override public void handle(HttpExchange exchange) throws IOException {
        UUID eventoId = Router.uuidEvento(exchange), usuarioId = usuario();
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        switch (acao) {
            case PARTICIPANTE_PDF -> enviarPdf(exchange, pdf(caso.participante(usuarioId, eventoId)));
            case PESSOA_PDF -> enviarPdf(exchange, pdf(caso.pessoa(usuarioId, eventoId, pessoa(exchange), papel(exchange))));
            case PARTICIPANTE_ENVIO -> {
                var c = caso.participante(usuarioId, eventoId);
                HttpRespostas.enviarJson(exchange, 200, caso.enviarParticipante(usuarioId, eventoId, pdf(c)), mapper);
            }
            case PESSOA_ENVIO -> {
                UUID pessoaId = pessoa(exchange); String papel = papel(exchange);
                var c = caso.pessoa(usuarioId, eventoId, pessoaId, papel);
                HttpRespostas.enviarJson(exchange, 200, caso.enviarPessoa(usuarioId, eventoId, pessoaId, papel, pdf(c)), mapper);
            }
        }
    }
}
