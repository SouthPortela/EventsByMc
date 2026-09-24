package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.LocalDateTime;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;

public record DadosNovoEvento(String titulo, String descricao, String local,
                              LocalDateTime dataInicio, LocalDateTime dataFim,
                              CategoriaEvento categoria) {
    public DadosNovoEvento(String titulo, String descricao, String local,
                           LocalDateTime dataInicio, LocalDateTime dataFim) {
        this(titulo, descricao, local, dataInicio, dataFim, null);
    }
}
