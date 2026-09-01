package br.com.eventsbymc.eventsapi.domain.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private Pessoa pessoa;
    private String senhaHash;
    private final Set<Perfil> perfis;

    public Usuario(Pessoa pessoa) {
        this(UUID.randomUUID(), pessoa, null, EnumSet.of(Perfil.VISITANTE));
    }

    public Usuario(Pessoa pessoa, String senhaHash) {
        this(UUID.randomUUID(), pessoa, senhaHash, EnumSet.of(Perfil.VISITANTE));
    }

    private Usuario(UUID id, Pessoa pessoa, String senhaHash, Set<Perfil> perfis) {
        if (id == null) {
            throw new IllegalArgumentException("Identificador do usuário é obrigatório.");
        }
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa é obrigatória.");
        }
        this.id = id;
        this.pessoa = pessoa;
        this.senhaHash = senhaHash;
        this.perfis = perfis.isEmpty() ? EnumSet.of(Perfil.VISITANTE) : EnumSet.copyOf(perfis);
    }

    public static Usuario reconstituir(UUID id, Pessoa pessoa, String senhaHash, Set<Perfil> perfis) {
        if (perfis == null) {
            throw new IllegalArgumentException("Perfis são obrigatórios.");
        }
        return new Usuario(id, pessoa, senhaHash, perfis);
    }

    public void adicionarPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil é obrigatório.");
        }

        perfis.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil é obrigatório.");
        }
        if (perfil == Perfil.VISITANTE) {
            throw new IllegalArgumentException("O perfil VISITANTE é obrigatório.");
        }
        perfis.remove(perfil);
    }

    public boolean possuiPerfil(Perfil perfil) {
        return perfis.contains(perfil);
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public UUID getId() { return id; }
    public String getSenhaHash() { return senhaHash; }
    public Set<Perfil> getPerfis() { return Collections.unmodifiableSet(perfis); }

    public void alterarPessoa(Pessoa pessoa) {
        if (pessoa == null) throw new IllegalArgumentException("Pessoa é obrigatória.");
        this.pessoa = pessoa;
    }

    public void alterarSenhaHash(String senhaHash) {
        if (senhaHash == null || senhaHash.isBlank()) throw new IllegalArgumentException("Hash de senha é obrigatório.");
        this.senhaHash = senhaHash;
    }
}
