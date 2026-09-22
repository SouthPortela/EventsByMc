package br.com.eventsbymc.eventsapi.application.exception;

public class ConflitoOperacaoException extends RuntimeException {
    public ConflitoOperacaoException(String mensagem) { super(mensagem); }
}
