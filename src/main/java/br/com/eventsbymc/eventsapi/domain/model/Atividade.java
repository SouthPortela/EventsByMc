package br.com.eventsbymc.eventsapi.domain.model;

public class Atividade {

    private final String titulo;
    private final String tipo;
    private final Local local;

    public Atividade(String titulo, String tipo, Local local) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "O título da atividade é obrigatório."
            );
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "O tipo da atividade é obrigatório."
            );
        }

        if (local == null) {
            throw new IllegalArgumentException(
                    "A atividade precisa possuir um local."
            );
        }

        this.titulo = titulo;
        this.tipo = tipo;
        this.local = local;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public Local getLocal() {
        return local;
    }
}