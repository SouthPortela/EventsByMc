package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.AdministrarPlataforma;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.RequerPerfil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.UUID;

public final class AdministracaoHandler implements HttpHandler {
    public enum Acao { LISTAR_EVENTOS, CONSULTAR_EVENTO, LISTAR_USUARIOS, LISTAR_MODERACOES,
        SUSPENDER, RESTAURAR, EXCLUIR }
    public record DadosMotivo(String motivo) {}

    private final AdministrarPlataforma administracao;
    private final ObjectMapper mapper;
    private final Acao acao;

    public AdministracaoHandler(AdministrarPlataforma administracao, ObjectMapper mapper, Acao acao) {
        this.administracao = administracao;
        this.mapper = mapper;
        this.acao = acao;
    }

    @Override
    @RequerPerfil(Perfil.ADMINISTRADOR)
    public void handle(HttpExchange exchange) throws IOException {
        UUID administradorId = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
        Object resposta = switch (acao) {
            case LISTAR_EVENTOS -> administracao.listarEventos(administradorId);
            case CONSULTAR_EVENTO -> administracao.consultarEvento(administradorId, eventoId(exchange));
            case LISTAR_USUARIOS -> administracao.listarUsuarios(administradorId);
            case LISTAR_MODERACOES -> administracao.listarModeracoes(administradorId);
            case SUSPENDER -> administracao.suspender(administradorId, eventoId(exchange), motivo(exchange));
            case RESTAURAR -> administracao.restaurar(administradorId, eventoId(exchange), motivo(exchange));
            case EXCLUIR -> administracao.excluir(administradorId, eventoId(exchange), motivo(exchange));
        };
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange, 200, resposta, mapper);
    }

    private String motivo(HttpExchange exchange) throws IOException {
        return HttpRespostas.lerJson(exchange, DadosMotivo.class, mapper).motivo();
    }

    private UUID eventoId(HttpExchange exchange) {
        String[] partes = exchange.getRequestURI().getPath().split("/", -1);
        if (partes.length < 4 || !partes[3].matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new IllegalArgumentException("Identificador do evento inválido.");
        }
        return UUID.fromString(partes[3]);
    }
}
