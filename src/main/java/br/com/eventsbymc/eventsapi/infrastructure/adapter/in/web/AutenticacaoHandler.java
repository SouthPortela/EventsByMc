package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.usecase.ResultadoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;

/** Equivalente ao antigo AutenticacaoControlador — POST /auth/login. */
public class AutenticacaoHandler implements HttpHandler {

    private final AutenticarUsuario autenticarUsuario;
    private final ObjectMapper objectMapper;

    public AutenticacaoHandler(AutenticarUsuario autenticarUsuario, ObjectMapper objectMapper) {
        this.autenticarUsuario = autenticarUsuario;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        LoginDTO loginDTO = objectMapper.readValue(exchange.getRequestBody(), LoginDTO.class);
        ResultadoAutenticacao resultado = autenticarUsuario.autenticar(loginDTO.getEmail(), loginDTO.getSenha());
        HttpRespostas.enviarJson(exchange, HttpURLConnection.HTTP_OK, LoginRespostaDTO.from(resultado), objectMapper);
    }
}
