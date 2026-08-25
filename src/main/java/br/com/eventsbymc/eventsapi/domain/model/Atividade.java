package br.com.eventsbymc.eventsapi.domain.model;

public class Atividade {

    private final String titulo;
    private final String descricao;

    public Atividade(String titulo, String descricao) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "Título da atividade é obrigatório."
            );
        }

        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }
}
