package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public final class DadosPresenca {
    private DadosPresenca() {}
    public record Atividade(UUID id, String titulo, LocalDateTime dataInicio, LocalDateTime dataFim, String local) {}
    public record NovaAtividade(String titulo, String descricao, LocalDateTime dataInicio, LocalDateTime dataFim, String local) {}
    public record Chamada(UUID id, String atividade, Instant expiraEm) {}
    public record ChamadaGerada(UUID id, String atividade, String codigo, Instant expiraEm) {}
    public record Confirmacao(UUID id, String atividade, Instant registradaEm, boolean jaRegistrada) {}
    public record Inscricao(UUID id, String estado) {}
}
