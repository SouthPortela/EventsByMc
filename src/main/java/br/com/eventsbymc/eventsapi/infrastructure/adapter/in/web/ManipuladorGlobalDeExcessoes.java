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

//Aqui vamos tratar as exceções que podem ocorrer na aplicação, e retornar uma resposta padronizada para o front-end.
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
        //rota que não bate com nenhum endpoint mapeado sem isso iria cair no handler genérico e virava 500 em vez de 404.
        } catch (RecursoNaoEncontradoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_NOT_FOUND, exception.getMessage(), objectMapper);
        //acontece quando alguém acessa uma rota existente com o método HTTP errado (ex: GET em vez de POST) — sem isso caía no handler genérico e virava 500 em vez de 405.
        } catch (MetodoNaoSuportadoException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_BAD_METHOD, exception.getMessage(), objectMapper);
        //Erros inesperados, que não foram tratados especificamente, serão tratados aqui, e retornaremos uma mensagem genérica de erro.
        } catch (Exception exception) {
            log.log(Level.SEVERE, "Erro inesperado: " + exception.getClass().getSimpleName(), exception);
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_INTERNAL_ERROR,
                    "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.", objectMapper);
        }
    }
}
