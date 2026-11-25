package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.util.UUID;

public final class DadosRelatorios {
    private DadosRelatorios() {}
    public record Atividade(UUID atividadeId, String atividadeTitulo, UUID usuarioId, String nome, String email,
            String inscricaoEstado, boolean reservada, boolean confirmada, String politicaFrequencia,
            Instant registradaEm, Instant entradaEm, Instant saidaEm, String origem, String motivo,
            UUID responsavelId) {}
    public record Inscrito(UUID usuarioId, String nome, String email,
                           String estado, Instant inscritaEm) {}
    public record Frequencia(UUID usuarioId, String nome, String email,
                             int atividadesObrigatorias, int presencasValidas,
                             int percentual, int minimoExigido, String situacao,
                             boolean elegivelCertificado) {}
}
