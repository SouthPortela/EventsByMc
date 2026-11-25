package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.util.UUID;

public final class DadosFrequencia {
    private DadosFrequencia() {}
    public record NovoAjuste(UUID id, UUID usuarioId, Boolean confirmada, String motivo) {}
    public record Ajuste(UUID id, UUID atividadeId, UUID usuarioId, boolean confirmada,
                         String motivo, Instant registradaEm, UUID responsavelId) {}
    public record NovoRegistro(UUID usuarioId, String marcacao) {}
    public record Registro(UUID id, UUID atividadeId, UUID usuarioId, String marcacao,
                           Instant registradaEm, UUID registradaPor) {}
}
