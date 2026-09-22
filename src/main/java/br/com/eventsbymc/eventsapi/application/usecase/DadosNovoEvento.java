package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.LocalDateTime;

public record DadosNovoEvento(String titulo, String descricao, String local,
                              LocalDateTime dataInicio, LocalDateTime dataFim) {}
