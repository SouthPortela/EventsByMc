package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.util.UUID;

public record DadosCertificado(UUID id, UUID eventoId, String eventoTitulo,
                               String destinatario, String email, String tipo,
                               Instant emitidoEm, Instant enviadoEm) {}
