package br.com.eventsbymc.eventsapi.application.exception;

public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String message, Throwable cause) {
        super(message, cause);
    }
}

//throwable adiciona o motivo da exceção, que pode ser útil para depuração e rastreamento de erros.
