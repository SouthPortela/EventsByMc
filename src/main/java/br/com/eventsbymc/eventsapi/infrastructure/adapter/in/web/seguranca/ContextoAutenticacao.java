package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;

import java.util.Optional;

/**
 * IMPORTANTE: com.sun.net.httpserver.HttpExchange.setAttribute/getAttribute NÃO são
 * isolados por requisição — testado empiricamente: duas requisições concorrentes no mesmo
 * HttpContext pisam no valor uma da outra. Por isso usamos ThreadLocal: o HttpServer executa
 * todos os filtros + o handler de UMA MESMA exchange sequencialmente na MESMA thread do
 * executor, então ThreadLocal isola corretamente entre requisições concorrentes — desde que
 * seja sempre limpo no finally do filtro (AutenticacaoFiltro.doFilter).
 */
public final class ContextoAutenticacao {

    private static final ThreadLocal<TokenClaims> ATUAL = new ThreadLocal<>();

    private ContextoAutenticacao() {
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
