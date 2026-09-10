package br.com.eventsbymc.eventsapi.application.exception;

public class MetodoNaoSuportadoException extends RuntimeException {
    public MetodoNaoSuportadoException() {
        super("Método HTTP não permitido para essa rota.");
    }
}
