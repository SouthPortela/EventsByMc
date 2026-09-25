package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.Set;
import java.util.UUID;

public final class DadosAdministracao {
    private DadosAdministracao() {}

    public record Evento(DadosEvento evento, UUID organizadorId, String organizadorNome) {}
    public record Usuario(UUID id, String nome, String emailMascarado, Set<Perfil> perfis) {
        public Usuario { perfis = Set.copyOf(perfis); }
    }
}
