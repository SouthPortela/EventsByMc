package br.com.eventsbymc.eventsapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Atividade {

    private final UUID id;
    private final String titulo;
    private final String descricao;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private final String local;
    private final Integer capacidade;

    public Atividade(String titulo, String descricao) {
        this(UUID.randomUUID(), titulo, descricao, null, null, null, null);
    }

    public Atividade(String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String local, Integer capacidade) {
        this(UUID.randomUUID(), titulo, descricao, inicio, fim, local, capacidade);
    }

    public static Atividade reconstituir(UUID id, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim, String local, Integer capacidade) {
        return new Atividade(id, titulo, descricao, inicio, fim, local, capacidade);
    }

    private Atividade(UUID id, String titulo, String descricao, LocalDateTime inicio, LocalDateTime fim,
                      String local, Integer capacidade) {

        if (id == null) throw new IllegalArgumentException("Identificador da atividade é obrigatório.");

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException(
                    "Título da atividade é obrigatório."
            );
        }
        if ((inicio == null) != (fim == null)) throw new IllegalArgumentException("Início e fim devem ser informados juntos.");
        if (inicio != null && !fim.isAfter(inicio)) throw new IllegalArgumentException("Fim deve ser posterior ao início.");
        if (local != null && local.isBlank()) throw new IllegalArgumentException("Local não pode ser vazio.");
        if (capacidade != null && capacidade < 1) throw new IllegalArgumentException("Capacidade deve ser positiva.");

        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.inicio = inicio;
        this.fim = fim;
        this.local = local;
        this.capacidade = capacidade;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public UUID getId() { return id; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public String getLocal() { return local; }
    public Integer getCapacidade() { return capacidade; }
}
