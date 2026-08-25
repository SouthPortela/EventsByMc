package br.com.eventsbymc.eventsapi.domain.model;

public class Evento {

    private final String titulo;
    private final String descricao;
    private final Usuario organizador;
    private final Programacao programacao;

    private EstadoEvento estado;

    public Evento(String titulo, String descricao, Usuario organizador) {

        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }

        if (organizador == null) {
            throw new IllegalArgumentException("Organizador é obrigatório.");
        }

        if (!organizador.possuiPerfil(Perfil.ORGANIZADOR)) {
            throw new IllegalArgumentException(
                    "O usuário precisa possuir o perfil ORGANIZADOR."
            );
        }

        this.titulo = titulo;
        this.descricao = descricao;
        this.organizador = organizador;
        this.programacao = new Programacao();
        this.estado = EstadoEvento.RASCUNHO;
    }

    public void publicar() {

        if (estado != EstadoEvento.RASCUNHO) {
            throw new IllegalStateException(
                    "Somente eventos em rascunho podem ser publicados."
            );
        }

        estado = EstadoEvento.PUBLICADO;
    }

    public void encerrar() {

        if (estado != EstadoEvento.PUBLICADO) {
            throw new IllegalStateException(
                    "Somente eventos publicados podem ser encerrados."
            );
        }

        estado = EstadoEvento.ENCERRADO;
    }

    public void adicionarAtividade(Atividade atividade) {
        programacao.adicionarAtividade(atividade);
    }

    public Programacao getProgramacao() {
        return programacao;
    }

    public EstadoEvento getEstado() {
        return estado;
    }

    public String getTitulo() {
        return titulo;
    }
}