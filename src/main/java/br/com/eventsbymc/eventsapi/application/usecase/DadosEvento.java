package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Contrato público: nunca serializar o agregado com o hash do organizador.
public record DadosEvento(UUID id, String titulo, String descricao, String local,
                          LocalDateTime dataInicio, LocalDateTime dataFim, EstadoEvento estado,
                          CategoriaEvento categoria,
                          List<DadosAtividade> atividades) {
    public DadosEvento { atividades = List.copyOf(atividades); }

    public record DadosAtividade(UUID id, String titulo, String local,
                                 LocalDateTime dataInicio, LocalDateTime dataFim) {}

    public static DadosEvento de(Evento evento) {
        var atividades = evento.getProgramacao().consultarAtividades().stream()
                .map(a -> new DadosAtividade(a.getId(), a.getTitulo(), a.getLocal(), a.getInicio(), a.getFim()))
                .toList();
        return new DadosEvento(evento.getId(), evento.getTitulo(), evento.getDescricao(),
                evento.getLocal(), evento.getInicio(), evento.getFim(), evento.getEstado(),
                evento.getCategoria(), atividades);
    }
}
