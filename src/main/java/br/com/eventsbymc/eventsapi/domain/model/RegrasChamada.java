package br.com.eventsbymc.eventsapi.domain.model;

import java.time.Duration;
import java.time.Instant;

public final class RegrasChamada {
    public static final Duration VALIDADE = Duration.ofMinutes(5);
    private RegrasChamada() {}
    public static void exigirValida(Instant agora, Instant expiraEm, boolean revogada) {
        if (revogada || !agora.isBefore(expiraEm))
            throw new IllegalArgumentException("Código inválido ou expirado. Solicite a chamada atual.");
    }
}
