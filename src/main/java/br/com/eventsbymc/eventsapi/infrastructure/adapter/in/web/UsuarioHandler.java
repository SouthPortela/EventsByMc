package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;

//POST /usuarios
public class UsuarioHandler implements HttpHandler {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ObjectMapper objectMapper;

    public UsuarioHandler(RegistrarUsuarioUseCase registrarUsuarioUseCase, ObjectMapper objectMapper) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        UsuarioDTO usuarioDTO = objectMapper.readValue(exchange.getRequestBody(), UsuarioDTO.class);
        registrarUsuarioUseCase.registrarUsuario(usuarioDTO.getNome(), usuarioDTO.getEmail(), usuarioDTO.getSenha());
        //mantém o corpo em texto puro, o frontend já espera assim
        HttpRespostas.enviarTexto(exchange, HttpURLConnection.HTTP_OK, "Usuário registrado com sucesso!");
    }
}
