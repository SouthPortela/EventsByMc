package br.com.eventsbymc.eventsapi.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Programacao {

    private final List<Atividade> atividades;

    public Programacao() {
        this.atividades = new ArrayList<>();
    }

    public void adicionarAtividade(Atividade atividade) {

        if (atividade == null) {
            throw new IllegalArgumentException(
                    "Atividade é obrigatória."
            );
        }

        atividades.add(atividade);
    }

    public List<Atividade> consultarAtividades() {
        return Collections.unmodifiableList(atividades);
    }
}
