package br.com.eventsbymc.eventsapi.domain.model;

import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "evento")
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue
    private UUID id;

    private String nome;
    private String descricao;
    private String estado;
    private String imgUrl;
    private String eventoUrl;
    private Date data;


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