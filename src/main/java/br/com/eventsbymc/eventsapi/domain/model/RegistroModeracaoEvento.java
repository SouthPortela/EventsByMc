package br.com.eventsbymc.eventsapi.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RegistroModeracaoEvento(UUID id, UUID eventoId, UUID administradorId,
                                     String administradorNome, EstadoEvento estadoAnterior,
                                     EstadoEvento estadoNovo, String motivo, Instant criadoEm) {}
