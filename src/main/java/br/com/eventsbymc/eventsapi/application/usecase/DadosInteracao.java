package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.util.UUID;

public final class DadosInteracao {
    private DadosInteracao() {}
    public record Mensagem(UUID id, UUID eventoId, UUID usuarioId, String autor,
                           String mensagem, Instant criadaEm) {}
    public record NovaMensagem(String mensagem) {}
}
