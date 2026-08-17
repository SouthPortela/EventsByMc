package br.com.eventsbymc.eventsapi.domain.model;

public class Participante {

    private final String nome;
    private final String email;

    public Participante(String nome, String email) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException(
                    "O nome é obrigatório."
            );
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "O e-mail é obrigatório."
            );
        }

        this.nome = nome;
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }
}