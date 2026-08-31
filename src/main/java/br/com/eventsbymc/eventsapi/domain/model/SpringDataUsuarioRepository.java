package br.com.eventsbymc.eventsapi.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntidade, Long> {
}