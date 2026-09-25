package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarRelatorios;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.RelatoriosRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class RelatoriosUseCase implements ConsultarRelatorios {
    private final RelatoriosRepository relatorios;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;

    public RelatoriosUseCase(RelatoriosRepository relatorios, EventoRepository eventos,
                             UsuarioRepository usuarios) {
        this.relatorios = Objects.requireNonNull(relatorios);
        this.eventos = Objects.requireNonNull(eventos);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    private void gerenciar(UUID usuarioId, UUID eventoId) {
        var usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if ((evento.getEstado() == EstadoEvento.SUSPENSO || evento.getEstado() == EstadoEvento.EXCLUIDO)
                && !usuario.possuiPerfil(Perfil.ADMINISTRADOR)) throw new RecursoNaoEncontradoException();
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR)
                && evento.getOrganizador().getId().equals(usuarioId))) {
            throw new AcessoNegadoException();
        }
    }

    public List<DadosRelatorios.Inscrito> inscritos(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return relatorios.inscritos(eventoId);
    }
    public List<DadosRelatorios.Frequencia> frequencias(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return relatorios.frequencias(eventoId);
    }
    public DadosRelatorios.Frequencia minhaFrequencia(UUID usuarioId, UUID eventoId) {
        if (usuarios.buscarPorId(usuarioId).isEmpty())
            throw new TokenInvalidoException("Sessão inválida.", null);
        if (eventos.buscarPorId(eventoId).isEmpty()) throw new RecursoNaoEncontradoException();
        return relatorios.frequenciaDoParticipante(usuarioId, eventoId);
    }
}
