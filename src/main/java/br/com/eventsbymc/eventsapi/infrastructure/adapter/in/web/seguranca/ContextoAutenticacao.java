package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

public final class ContextoAutenticacao {

    private static final String ATRIBUTO = "eventsbymc.autenticacao";

    private ContextoAutenticacao() {
        // Construtor privado para evitar instanciação
    }

    public static void definir(HttpServletRequest request, TokenClaims claims) {
        request.setAttribute(ATRIBUTO, claims);
    }

    public static Optional<TokenClaims> obter(HttpServletRequest request) {
        Object valor = request.getAttribute(ATRIBUTO);
        return valor instanceof TokenClaims claims ? Optional.of(claims) : Optional.empty();
    }
}
