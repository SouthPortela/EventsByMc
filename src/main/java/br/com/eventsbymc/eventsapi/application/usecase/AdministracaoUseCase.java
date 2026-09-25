package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.AdministrarPlataforma;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.RegistroModeracaoEvento;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AdministracaoUseCase implements AdministrarPlataforma {
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;

    public AdministracaoUseCase(EventoRepository eventos, UsuarioRepository usuarios) {
        this.eventos = Objects.requireNonNull(eventos);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    private void exigirAdministrador(UUID id) {
        var usuario = usuarios.buscarPorId(id)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida. Entre novamente.", null));
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)) throw new AcessoNegadoException();
    }

    private static DadosAdministracao.Evento apresentar(Evento evento) {
        return new DadosAdministracao.Evento(DadosEvento.de(evento), evento.getOrganizador().getId(),
                evento.getOrganizador().getPessoa().getNome());
    }

    public List<DadosAdministracao.Evento> listarEventos(UUID administradorId) {
        exigirAdministrador(administradorId);
        return eventos.listar().stream().map(AdministracaoUseCase::apresentar).toList();
    }

    public DadosAdministracao.Evento consultarEvento(UUID administradorId, UUID eventoId) {
        exigirAdministrador(administradorId);
        return apresentar(eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new));
    }

    public List<DadosAdministracao.Usuario> listarUsuarios(UUID administradorId) {
        exigirAdministrador(administradorId);
        return usuarios.listar().stream().map(u -> new DadosAdministracao.Usuario(u.getId(),
                u.getPessoa().getNome(), mascararEmail(u.getPessoa().getEmail()), u.getPerfis())).toList();
    }

    public List<RegistroModeracaoEvento> listarModeracoes(UUID administradorId) {
        exigirAdministrador(administradorId);
        return eventos.listarModeracoes();
    }

    public DadosAdministracao.Evento suspender(UUID administradorId, UUID eventoId, String motivo) {
        return moderar(administradorId, eventoId, motivo, EstadoEvento.SUSPENSO);
    }

    public DadosAdministracao.Evento restaurar(UUID administradorId, UUID eventoId, String motivo) {
        return moderar(administradorId, eventoId, motivo, EstadoEvento.RASCUNHO);
    }

    public DadosAdministracao.Evento excluir(UUID administradorId, UUID eventoId, String motivo) {
        return moderar(administradorId, eventoId, motivo, EstadoEvento.EXCLUIDO);
    }

    private DadosAdministracao.Evento moderar(UUID administradorId, UUID eventoId, String motivo,
                                              EstadoEvento destino) {
        exigirAdministrador(administradorId);
        if (motivo == null || motivo.trim().length() < 10 || motivo.trim().length() > 500) {
            throw new IllegalArgumentException("O motivo deve ter entre 10 e 500 caracteres.");
        }
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        EstadoEvento anterior = evento.getEstado();
        try {
            switch (destino) {
                case SUSPENSO -> evento.suspender();
                case RASCUNHO -> evento.restaurarComoRascunho();
                case EXCLUIDO -> evento.excluir();
                default -> throw new IllegalArgumentException("Ação de moderação inválida.");
            }
        } catch (IllegalStateException exception) {
            throw new ConflitoOperacaoException(exception.getMessage());
        }
        if (!eventos.moderar(eventoId, anterior, destino, administradorId, motivo.trim())) {
            throw new ConflitoOperacaoException("O evento mudou. Atualize a lista antes de tentar novamente.");
        }
        return consultarEvento(administradorId, eventoId);
    }

    private static String mascararEmail(String email) {
        int arroba = email.indexOf('@');
        return arroba < 1 ? "***" : email.substring(0, 1) + "***" + email.substring(arroba);
    }
}
