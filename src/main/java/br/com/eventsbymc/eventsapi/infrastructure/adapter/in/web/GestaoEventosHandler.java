package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import br.com.eventsbymc.eventsapi.application.usecase.DadosNovoEvento;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.RequerPerfil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;

public final class GestaoEventosHandler implements HttpHandler {
    public enum Acao { LISTAR, CRIAR, PUBLICAR, ENCERRAR, ALTERAR_CATEGORIA }
    public record DadosCategoria(CategoriaEvento categoria) {}
    private final OperacoesEvento eventos;
    private final ObjectMapper mapper;
    private final Acao acao;

    public GestaoEventosHandler(OperacoesEvento eventos, ObjectMapper mapper, Acao acao) {
        this.eventos = eventos;
        this.mapper = mapper;
        this.acao = acao;
    }

    @Override
    @RequerPerfil({Perfil.ORGANIZADOR, Perfil.ADMINISTRADOR})
    public void handle(HttpExchange exchange) throws IOException {
        var usuarioId = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
        Object resultado = switch (acao) {
            case LISTAR -> eventos.listarDoOrganizador(usuarioId);
            case CRIAR -> eventos.criar(usuarioId, HttpRespostas.lerJson(exchange, DadosNovoEvento.class, mapper));
            case PUBLICAR -> eventos.alterarEstado(usuarioId, Router.uuidEvento(exchange), EstadoEvento.PUBLICADO);
            case ENCERRAR -> eventos.alterarEstado(usuarioId, Router.uuidEvento(exchange), EstadoEvento.ENCERRADO);
            case ALTERAR_CATEGORIA -> eventos.alterarCategoria(usuarioId, Router.uuidEvento(exchange),
                    HttpRespostas.lerJson(exchange, DadosCategoria.class, mapper).categoria());
        };
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange, acao == Acao.CRIAR ? 201 : 200, resultado, mapper);
    }
}
