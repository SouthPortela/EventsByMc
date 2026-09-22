package br.com.eventsbymc.eventsapi.application.exception;
public final class LimiteTentativasException extends RuntimeException {
    public LimiteTentativasException() { super("Muitas tentativas. Aguarde um minuto e tente novamente."); }
}
