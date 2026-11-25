package br.com.eventsbymc.eventsapi.application.usecase;
import java.time.Instant;
import java.util.UUID;
public record DadosHistoricoEvento(long id, UUID responsavelId, Instant registradaEm, String antes, String depois) {}
