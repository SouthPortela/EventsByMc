package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.domain.model.Evento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Porta de saída para a persistência do agregado Evento. 
public interface EventoRepository {
    Evento salvar(Evento evento);
    Optional<Evento> buscarPorId(UUID id);
    List<Evento> listar();
    void removerPorId(UUID id);
}
