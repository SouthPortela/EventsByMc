package br.com.eventsbymc.eventsapi.domain.model;

public class Evento {

    private String nome;
    private String descricao;
    private String estado;

    public Evento(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
        this.estado = "RASCUNHO";
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getEstado() {
        return estado;
    }

    public void publicar() {
        if (!estado.equals("RASCUNHO")) {
            throw new IllegalStateException(
                    "Somente eventos em rascunho podem ser publicados."
            );
        }

        estado = "PUBLICADO";
    }

    public void encerrar() {
        if (!estado.equals("PUBLICADO")) {
            throw new IllegalStateException(
                    "Somente eventos publicados podem ser encerrados."
            );
        }

        estado = "ENCERRADO";
    }
}