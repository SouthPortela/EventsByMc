package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.Set;
import java.util.UUID;

public record DadosMinhaConta(UUID usuarioId, String nome, String email, Set<Perfil> perfis) {
    public DadosMinhaConta { perfis = Set.copyOf(perfis); }
}
