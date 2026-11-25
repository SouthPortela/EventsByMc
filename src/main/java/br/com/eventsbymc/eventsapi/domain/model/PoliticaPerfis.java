package br.com.eventsbymc.eventsapi.domain.model;

import java.util.Objects;
import java.util.Set;

public final class PoliticaPerfis {
    private PoliticaPerfis() {}
    public static void validar(Set<Perfil> perfis) {
        if (perfis == null || perfis.isEmpty() || perfis.stream().anyMatch(Objects::isNull) || perfis.contains(Perfil.VISITANTE))
            throw new IllegalArgumentException("Escolha ao menos um perfil autenticado.");
    }
    public static void exigirAdministradorRestante(Set<Perfil> atuais, Set<Perfil> novos, long administradores) {
        validar(novos);
        if (atuais.contains(Perfil.ADMINISTRADOR) && !novos.contains(Perfil.ADMINISTRADOR) && administradores <= 1)
            throw new IllegalStateException("Nao e permitido remover o ultimo administrador.");
    }
}
