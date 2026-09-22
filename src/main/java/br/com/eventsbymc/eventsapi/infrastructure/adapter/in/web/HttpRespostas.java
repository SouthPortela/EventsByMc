package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

//evita duplicar a escrita de resposta HTTP (headers + corpo) em cada handler
public final class HttpRespostas {

    private HttpRespostas() {
    }

    public static <T> T lerJson(HttpExchange exchange, Class<T> tipo, ObjectMapper objectMapper) throws IOException {
        String conteudo = exchange.getRequestHeaders().getFirst("Content-Type");
        if (conteudo == null || !conteudo.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) {
            throw new IllegalArgumentException("Envie o corpo no formato application/json.");
        }
        byte[] corpo = exchange.getRequestBody().readNBytes(65_537);
        if (corpo.length > 65_536) throw new IllegalArgumentException("Corpo da requisição excede o limite permitido.");
        T resultado = objectMapper.readValue(corpo, tipo);
        if (resultado == null) throw new IllegalArgumentException("Informe o corpo da requisição.");
        return resultado;
    }

    public static void enviarJson(HttpExchange exchange, int status, Object corpo, ObjectMapper objectMapper) throws IOException {
        byte[] bytes = objectMapper.writeValueAsBytes(corpo);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }

    public static void enviarErro(HttpExchange exchange, int status, String mensagem, ObjectMapper objectMapper) throws IOException {
        enviarJson(exchange, status, ErroRespostaDTO.criar(mensagem), objectMapper);
    }

    public static void enviarTexto(HttpExchange exchange, int status, String texto) throws IOException {
        byte[] bytes = texto.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain;charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream saida = exchange.getResponseBody()) {
            saida.write(bytes);
        }
    }
}
