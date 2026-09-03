package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.domain.model.exception.CredenciaisInvalidasException;
import br.com.eventsbymc.eventsapi.domain.model.exception.EmailJaCadastradoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
//Aqui vamos tratar as exceções que podem ocorrer na aplicação, e retornar uma resposta padronizada para o front-end.
public class ManipuladorGlobalDeExcessoes {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalDeExcessoes.class);

    @ExceptionHandler(EmailJaCadastradoException.class)
    public ResponseEntity<ErroRespostaDTO> tratarEmailJaCadastrado(EmailJaCadastradoException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroRespostaDTO.criar(exception.getMessage()));
    }

    @ExceptionHandler({CredenciaisInvalidasException.class, TokenInvalidoException.class})
    public ResponseEntity<ErroRespostaDTO> tratarCredenciaisInvalidas(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErroRespostaDTO.criar(exception.getMessage()));
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroRespostaDTO> tratarAcessoNegado(AcessoNegadoException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErroRespostaDTO.criar(exception.getMessage()));
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroRespostaDTO> tratarArgumentoInvalido(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErroRespostaDTO.criar(exception.getMessage()));
    }
    @ExceptionHandler(NoResourceFoundException.class)
    //rota que não bate com nenhum endpoint mapeado; sem isso, caía no handler genérico e virava 500 em vez de 404.
    public ResponseEntity<ErroRespostaDTO> tratarRotaNaoEncontrada(NoResourceFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroRespostaDTO.criar("Recurso não encontrado."));
    }
    @ExceptionHandler(Exception.class)
    //Erros inesperados, que não foram tratados especificamente, serão tratados aqui, e retornaremos uma mensagem genérica de erro.
    public ResponseEntity<ErroRespostaDTO> tratarErroInesperado(Exception exception) {
        log.error("Erro inesperado:  {}", exception.getClass().getSimpleName(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErroRespostaDTO.criar("Ocorreu um erro inesperado. Por favor, tente novamente mais tarde."));
    }
}
