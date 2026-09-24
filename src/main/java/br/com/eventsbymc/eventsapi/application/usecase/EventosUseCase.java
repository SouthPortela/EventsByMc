package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class EventosUseCase implements OperacoesEvento {
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;

    public EventosUseCase(EventoRepository eventos, UsuarioRepository usuarios) {
        this.eventos = Objects.requireNonNull(eventos);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override
    public List<DadosEvento> listarPublicados() {
        return eventos.listar().stream().filter(e -> e.getEstado() == EstadoEvento.PUBLICADO)
                .map(DadosEvento::de).toList();
    }

    @Override
    public DadosEvento consultarPublicado(UUID eventoId) {
        return eventos.buscarPorId(eventoId).filter(e -> e.getEstado() == EstadoEvento.PUBLICADO)
                .map(DadosEvento::de).orElseThrow(RecursoNaoEncontradoException::new);
    }

    @Override
    public List<DadosEvento> listarDoOrganizador(UUID usuarioId) {
        var usuario = organizadorAutorizado(usuarioId);
        return eventos.listar().stream().filter(e -> e.getOrganizador().getId().equals(usuario.getId()))
                .map(DadosEvento::de).toList();
    }

    @Override
    public DadosEvento criar(UUID usuarioId, DadosNovoEvento dados) {
        var organizador = organizadorAutorizado(usuarioId);
        if (dados == null) throw new IllegalArgumentException("Informe os dados do evento.");
        String titulo = texto(dados.titulo(), "Título", 5, 200);
        String descricao = texto(dados.descricao(), "Descrição", 20, 10000);
        String local = texto(dados.local(), "Local", 1, 200);
        if (dados.dataInicio() == null || dados.dataFim() == null) {
            throw new IllegalArgumentException("Informe início e fim do evento.");
        }
        var categoria = dados.categoria() == null ? CategoriaEvento.OUTROS : dados.categoria();
        var evento = new Evento(titulo, descricao, organizador, dados.dataInicio(), dados.dataFim(), local, categoria);
        return DadosEvento.de(eventos.salvar(evento));
    }

    @Override
    public DadosEvento alterarCategoria(UUID usuarioId, UUID eventoId, CategoriaEvento categoria) {
        var usuario = organizadorAutorizado(usuarioId);
        if (categoria == null) throw new IllegalArgumentException("Categoria é obrigatória.");
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (!evento.getOrganizador().getId().equals(usuarioId) && !usuario.possuiPerfil(Perfil.ADMINISTRADOR)) {
            throw new AcessoNegadoException();
        }
        if (!eventos.alterarCategoria(eventoId, categoria)) {
            throw new RecursoNaoEncontradoException();
        }
        evento.alterarCategoria(categoria);
        return DadosEvento.de(evento);
    }

    @Override
    public DadosEvento alterarEstado(UUID usuarioId, UUID eventoId, EstadoEvento destino) {
        var usuario = organizadorAutorizado(usuarioId);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (!evento.getOrganizador().getId().equals(usuarioId) && !usuario.possuiPerfil(Perfil.ADMINISTRADOR)) {
            throw new AcessoNegadoException();
        }
        if (destino != EstadoEvento.PUBLICADO && destino != EstadoEvento.ENCERRADO) {
            throw new IllegalArgumentException("Transição não suportada.");
        }
        // Repetir a mesma solicitação não produz uma segunda alteração.
        if (evento.getEstado() == destino) return DadosEvento.de(evento);
        EstadoEvento anterior = evento.getEstado();
        try {
            if (destino == EstadoEvento.PUBLICADO) evento.publicar();
            else evento.encerrar();
        } catch (IllegalStateException exception) {
            throw new ConflitoOperacaoException(exception.getMessage());
        }
        if (!eventos.alterarEstado(eventoId, anterior, destino)) {
            throw new ConflitoOperacaoException("O evento foi alterado por outra solicitação. Atualize a lista.");
        }
        return DadosEvento.de(evento);
    }

    private Usuario organizadorAutorizado(UUID id) {
        if (id == null) throw new AcessoNegadoException();
        var usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida. Entre novamente.", null));
        if (!usuario.possuiPerfil(Perfil.ORGANIZADOR) && !usuario.possuiPerfil(Perfil.ADMINISTRADOR)) {
            throw new AcessoNegadoException();
        }
        return usuario;
    }

    private static String texto(String valor, String campo, int minimo, int maximo) {
        if (valor == null || valor.trim().length() < minimo || valor.trim().length() > maximo) {
            throw new IllegalArgumentException(campo + " deve ter entre " + minimo + " e " + maximo + " caracteres.");
        }
        return valor.trim();
    }
}
