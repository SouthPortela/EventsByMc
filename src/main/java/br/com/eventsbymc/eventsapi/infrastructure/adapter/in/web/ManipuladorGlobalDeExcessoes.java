package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.MetodoNaoSuportadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.domain.model.exception.CredenciaisInvalidasException;
import br.com.eventsbymc.eventsapi.domain.model.exception.EmailJaCadastradoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Equivalente ao antigo @RestControllerAdvice: envolve o handler de verdade (aqui, o
 * Router) num try/catch e traduz cada exceção pro mesmo status HTTP + corpo JSON
 * (ErroRespostaDTO) que já existia.
 */
public class ManipuladorGlobalDeExcessoes implements HttpHandler {

    private static final Logger log = Logger.getLogger(ManipuladorGlobalDeExcessoes.class.getName());

    private final HttpHandler delegate;
    private final ObjectMapper objectMapper;

    public ManipuladorGlobalDeExcessoes(HttpHandler delegate, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            delegate.handle(exchange);
        } catch (EmailJaCadastradoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_CONFLICT, exception.getMessage(), objectMapper);
        } catch (CredenciaisInvalidasException | TokenInvalidoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_UNAUTHORIZED, exception.getMessage(), objectMapper);
        } catch (AcessoNegadoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_FORBIDDEN, exception.getMessage(), objectMapper);
        } catch (IllegalArgumentException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_BAD_REQUEST, exception.getMessage(), objectMapper);
        } catch (RecursoNaoEncontradoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_NOT_FOUND, exception.getMessage(), objectMapper);
        } catch (MetodoNaoSuportadoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_BAD_METHOD, exception.getMessage(), objectMapper);
        } catch (Exception exception) {
            log.log(Level.SEVERE, "Erro inesperado: " + exception.getClass().getSimpleName(), exception);
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", objectMapper);
        }
    }
}
