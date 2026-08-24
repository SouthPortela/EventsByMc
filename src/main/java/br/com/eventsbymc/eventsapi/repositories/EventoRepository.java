package br.com.eventsbymc.eventsapi.repositories;

import  br.com.eventsbymc.eventsapi.domain.model.evento.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoRepository extends JpaRepository<Evento, UUID> {
}
