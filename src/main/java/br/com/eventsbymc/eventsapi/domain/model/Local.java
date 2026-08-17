package br.com.eventsbymc.eventsapi.domain.model;

public class Local {

    private final String nome;
    private final String endereco;
    private final int capacidade;

    public Local(String nome, String endereco, int capacidade) {

        if (capacidade <= 0) {
            throw new IllegalArgumentException(
                    "A capacidade deve ser maior que zero."
            );
        }

        this.nome = nome;
        this.endereco = endereco;
        this.capacidade = capacidade;
    }

    public String getNome() {
        return nome;
    }

    public String getEndereco() {
        return endereco;
    }

    public int getCapacidade() {
        return capacidade;
    }
}