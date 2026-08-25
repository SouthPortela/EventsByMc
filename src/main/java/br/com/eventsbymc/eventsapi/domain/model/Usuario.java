package br.com.eventsbymc.eventsapi.domain.model;

import java.util.EnumSet;
import java.util.Set;

public class Usuario {

    private final Pessoa pessoa;
    private final Set<Perfil> perfis;

    public Usuario(Pessoa pessoa) {
        if (pessoa == null) {
            throw new IllegalArgumentException("Pessoa é obrigatória.");
        }

        this.pessoa = pessoa;
        this.perfis = EnumSet.noneOf(Perfil.class);
    }

    public void adicionarPerfil(Perfil perfil) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil é obrigatório.");
        }

        perfis.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        perfis.remove(perfil);
    }

    public boolean possuiPerfil(Perfil perfil) {
        return perfis.contains(perfil);
    }

    public Pessoa getPessoa() {
        return pessoa;
    }
}