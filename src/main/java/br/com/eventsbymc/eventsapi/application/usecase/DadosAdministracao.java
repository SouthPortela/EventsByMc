package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public final class DadosAdministracao {
    private DadosAdministracao() {}
    public record Resumo(long usuarios, long organizadores, long eventosPublicados, long eventosBloqueados, long registrosPresenca) {}
    public record Conta(UUID id, String nome, String email, Set<Perfil> perfis) {
        public Conta { perfis = Set.copyOf(perfis); }
    }
    public record EventoAdmin(DadosEvento evento, UUID organizadorId, String organizadorNome) {}
    public record Registro(UUID id, UUID responsavelId, String acao, UUID recursoId, String detalhe, Instant registradoEm) {}
}
