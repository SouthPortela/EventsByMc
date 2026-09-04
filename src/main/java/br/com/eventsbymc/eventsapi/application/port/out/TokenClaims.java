package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;

import java.util.Set;
import java.util.UUID;

public record TokenClaims(UUID usuarioId, String email, Set<Perfil> perfis) {
}

//Aqui montamos o TOKEN, RECORD é um recurso java que permite criar classes imutáveis de forma concisa, com construtor, getters, equals, hashCode e toString gerados automaticamente.