package br.com.eventsbymc.eventsapi.repositories;

import br.com.eventsbymc.eventsapi.domain.model.Local;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LocalRepository extends JpaRepository<Local, UUID> {
}
