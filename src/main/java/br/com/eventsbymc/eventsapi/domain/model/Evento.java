package br.com.eventsbymc.eventsapi.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public class Evento {

    private final UUID id;
    private final String titulo;
    private final String descricao;
    private final Usuario organizador;
    private final Programacao programacao;
    private final LocalDateTime inicio;
    private final LocalDateTime fim;
    private final String local;
    private CategoriaEvento categoria;

    private EstadoEvento estado;

    public Evento(String titulo, String descricao, Usuario organizador) {
        this(UUID.randomUUID(), titulo, descricao, organizador, null, null, null, CategoriaEvento.OUTROS, EstadoEvento.RASCUNHO, false);
    }

    public Evento(String titulo, String descricao, Usuario organizador, LocalDateTime inicio,
                  LocalDateTime fim, String local) {
        this(titulo, descricao, organizador, inicio, fim, local, CategoriaEvento.OUTROS);
    }

    public Evento(String titulo, String descricao, Usuario organizador, LocalDateTime inicio,
                  LocalDateTime fim, String local, CategoriaEvento categoria) {
        this(UUID.randomUUID(), titulo, descricao, organizador, inicio, fim, local, categoria, EstadoEvento.RASCUNHO, false);
    }

    public static Evento reconstituir(UUID id, String titulo, String descricao, Usuario organizador,
                                      LocalDateTime inicio, LocalDateTime fim, String local, EstadoEvento estado) {
        return reconstituir(id, titulo, descricao, organizador, inicio, fim, local, CategoriaEvento.OUTROS, estado);
    }

    public static Evento reconstituir(UUID id, String titulo, String descricao, Usuario organizador,
                                      LocalDateTime inicio, LocalDateTime fim, String local,
                                      CategoriaEvento categoria, EstadoEvento estado) {
        return new Evento(id, titulo, descricao, organizador, inicio, fim, local, categoria, estado, true);
    }

    public static Evento reconstituir(UUID id, String titulo, String descricao, Usuario organizador,
                                      LocalDateTime inicio, LocalDateTime fim, String local, EstadoEvento estado,
                                      List<Atividade> atividades) {
        return reconstituir(id, titulo, descricao, organizador, inicio, fim, local,
                CategoriaEvento.OUTROS, estado, atividades);
    }

    public static Evento reconstituir(UUID id, String titulo, String descricao, Usuario organizador,
                                      LocalDateTime inicio, LocalDateTime fim, String local,
                                      CategoriaEvento categoria, EstadoEvento estado, List<Atividade> atividades) {
        Evento evento = reconstituir(id, titulo, descricao, organizador, inicio, fim, local, categoria, estado);
        // Reconstituir registros não é adicionar novas atividades a um evento encerrado.
        for (Atividade atividade : List.copyOf(atividades)) {
            evento.validarAtividadeNoPeriodo(atividade);
            evento.programacao.adicionarAtividade(atividade);
        }
        return evento;
    }

    private Evento(UUID id, String titulo, String descricao, Usuario organizador, LocalDateTime inicio,
                   LocalDateTime fim, String local, CategoriaEvento categoria,
                   EstadoEvento estado, boolean reconstituindo) {

        if (id == null) throw new IllegalArgumentException("Identificador do evento é obrigatório.");
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("Título é obrigatório.");
        }

        if (organizador == null) {
            throw new IllegalArgumentException("Organizador é obrigatório.");
        }

        // Revogar um perfil não torna ilegível um evento já persistido.
        if (!reconstituindo && !organizador.possuiPerfil(Perfil.ORGANIZADOR) && !organizador.possuiPerfil(Perfil.ADMINISTRADOR)) {
            throw new IllegalArgumentException(
                    "O usuário precisa possuir o perfil ORGANIZADOR."
            );
        }
        if ((inicio == null) != (fim == null)) throw new IllegalArgumentException("Início e fim devem ser informados juntos.");
        if (inicio != null && !fim.isAfter(inicio)) throw new IllegalArgumentException("Fim deve ser posterior ao início.");
        if (local != null && local.isBlank()) throw new IllegalArgumentException("Local não pode ser vazio.");
        if (estado == null) throw new IllegalArgumentException("Estado é obrigatório.");
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");

        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.organizador = organizador;
        this.programacao = new Programacao();
        this.inicio = inicio;
        this.fim = fim;
        this.local = local;
        this.categoria = categoria;
        this.estado = estado;
    }

    public void alterarCategoria(CategoriaEvento novaCategoria) {
        if (novaCategoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");
        this.categoria = novaCategoria;
    }

    public void publicar() {

        if (estado != EstadoEvento.RASCUNHO) {
            throw new IllegalStateException(
                    "Somente eventos em rascunho podem ser publicados."
            );
        }

        if (inicio == null || fim == null || local == null || local.isBlank()) {
            throw new IllegalStateException("Informe período e local antes de publicar.");
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
        if (estado == EstadoEvento.ENCERRADO) {
            throw new IllegalStateException("Não é possível alterar um evento encerrado.");
        }
        validarAtividadeNoPeriodo(atividade);
        programacao.adicionarAtividade(atividade);
    }

    private void validarAtividadeNoPeriodo(Atividade atividade) {
        if (atividade == null || inicio == null || atividade.getInicio() == null) return;
        if (atividade.getInicio().isBefore(inicio) || atividade.getFim().isAfter(fim)) {
            throw new IllegalArgumentException("A atividade deve ocorrer dentro do período do evento.");
        }
    }

    public Programacao getProgramacao() {
        Programacao copia = new Programacao();
        programacao.consultarAtividades().forEach(copia::adicionarAtividade);
        return copia;
    }
    public EstadoEvento getEstado() {
        return estado;
    }
    public String getTitulo() {
        return titulo;
    }
    public UUID getId() { return id; }
    public String getDescricao() { return descricao; }
    public Usuario getOrganizador() { return organizador; }
    public LocalDateTime getInicio() { return inicio; }
    public LocalDateTime getFim() { return fim; }
    public String getLocal() { return local; }
    public CategoriaEvento getCategoria() { return categoria; }
}
