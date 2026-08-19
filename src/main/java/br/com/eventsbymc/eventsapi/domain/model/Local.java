package br.com.eventsbymc.eventsapi.domain.model;

import jakarta.persistence.*;

import java.util.UUID;

@Table(name ="local")
@Entity
public class Local {
    @Id
    @GeneratedValue
    private UUID id;
    private final String nome;
    private final String endereco;
    private final int capacidade;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

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