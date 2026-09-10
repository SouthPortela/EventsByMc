package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;

import java.util.Optional;

//guarda o usuário autenticado da requisição atual. testei e HttpExchange.setAttribute
//vaza entre requisições concorrentes, por isso é ThreadLocal e não isso.
public final class ContextoAutenticacao {

    private static final ThreadLocal<TokenClaims> ATUAL = new ThreadLocal<>();

    private ContextoAutenticacao() {
        // Construtor privado para evitar instanciação
    }

    public static void definir(TokenClaims claims) {
        ATUAL.set(claims);
    }

    public static Optional<TokenClaims> obter() {
        return Optional.ofNullable(ATUAL.get());
    }

    public static void limpar() {
        ATUAL.remove();
    }
}
