package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarRelatorios;
import br.com.eventsbymc.eventsapi.application.usecase.DadosRelatorios;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class RelatoriosHandler implements HttpHandler {
    public enum Acao { INSCRITOS, FREQUENCIA, MINHA_FREQUENCIA, INSCRITOS_CSV, FREQUENCIA_CSV, INSCRITOS_PDF, FREQUENCIA_PDF }
    private final ConsultarRelatorios caso;
    private final ObjectMapper mapper;
    private final Acao acao;

    public RelatoriosHandler(ConsultarRelatorios caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso;
        this.mapper = mapper;
        this.acao = acao;
    }

    private static UUID usuario() {
        return ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
    }

    @Override public void handle(HttpExchange exchange) throws IOException {
        UUID eventoId = Router.uuidEvento(exchange);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        switch (acao) {
            case INSCRITOS -> HttpRespostas.enviarJson(exchange, 200,
                    caso.inscritos(usuario(), eventoId), mapper);
            case FREQUENCIA -> HttpRespostas.enviarJson(exchange, 200,
                    caso.frequencias(usuario(), eventoId), mapper);
            case MINHA_FREQUENCIA -> HttpRespostas.enviarJson(exchange, 200,
                    caso.minhaFrequencia(usuario(), eventoId), mapper);
            case INSCRITOS_CSV -> enviarCsv(exchange, "inscritos.csv", inscritosCsv(caso.inscritos(usuario(), eventoId)));
            case FREQUENCIA_CSV -> enviarCsv(exchange, "frequencia.csv", frequenciaCsv(caso.frequencias(usuario(), eventoId)));
            case INSCRITOS_PDF -> enviarPdf(exchange, "inscritos.pdf", "Inscritos",
                    caso.inscritos(usuario(), eventoId).stream()
                            .map(i -> i.nome() + " | " + i.email() + " | " + i.estado()).toList());
            case FREQUENCIA_PDF -> enviarPdf(exchange, "frequencia.pdf", "Frequência",
                    caso.frequencias(usuario(), eventoId).stream()
                            .map(f -> f.nome() + " | " + f.presencasValidas() + "/"
                                    + f.atividadesObrigatorias() + " | " + f.percentual() + "% | " + f.situacao()).toList());
        }
    }

    private static String celula(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) texto = "'" + texto;
        return "\"" + texto.replace("\"", "\"\"").replace('\r', ' ').replace('\n', ' ') + "\"";
    }

    private static String inscritosCsv(List<DadosRelatorios.Inscrito> inscritos) {
        StringBuilder csv = new StringBuilder("usuario_id;nome;email;estado;inscrita_em\r\n");
        for (var i : inscritos) csv.append(celula(i.usuarioId())).append(';')
                .append(celula(i.nome())).append(';').append(celula(i.email())).append(';')
                .append(celula(i.estado())).append(';').append(celula(i.inscritaEm())).append("\r\n");
        return csv.toString();
    }

    private static String frequenciaCsv(List<DadosRelatorios.Frequencia> frequencias) {
        StringBuilder csv = new StringBuilder("usuario_id;nome;email;atividades_obrigatorias;presencas_validas;percentual;minimo_exigido;situacao;elegivel_certificado\r\n");
        for (var f : frequencias) csv.append(celula(f.usuarioId())).append(';')
                .append(celula(f.nome())).append(';').append(celula(f.email())).append(';')
                .append(f.atividadesObrigatorias()).append(';').append(f.presencasValidas()).append(';')
                .append(f.percentual()).append(';').append(f.minimoExigido()).append(';')
                .append(celula(f.situacao())).append(';').append(f.elegivelCertificado()).append("\r\n");
        return csv.toString();
    }

    private static void enviarCsv(HttpExchange exchange, String arquivo, String texto) throws IOException {
        byte[] bytes = ("\uFEFF" + texto).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/csv; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + arquivo + "\"");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var saida = exchange.getResponseBody()) { saida.write(bytes); }
    }

    private static void enviarPdf(HttpExchange exchange, String arquivo, String titulo, List<String> linhas) throws IOException {
        byte[] bytes = PdfSimples.gerar(titulo, linhas);
        exchange.getResponseHeaders().set("Content-Type", "application/pdf");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + arquivo + "\"");
        exchange.sendResponseHeaders(200, bytes.length);
        try (var saida = exchange.getResponseBody()) { saida.write(bytes); }
    }
}
