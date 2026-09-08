package br.com.eventsbymc.eventsapi.application.exception;

public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException() {
        super("Recurso não encontrado.");
    }
}
