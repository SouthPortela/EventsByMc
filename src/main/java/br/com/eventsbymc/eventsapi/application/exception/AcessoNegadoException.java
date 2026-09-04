package br.com.eventsbymc.eventsapi.application.exception;

public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException() {
        super("Você não tem permissão para executar esta ação.");
    }
}
//padroniza mensagem de erro para quando o usuário não tem permissão para executar determinada ação, como acessar um recurso protegido.
