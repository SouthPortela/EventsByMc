package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesInteracao;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.InteracaoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import java.util.List;
import java.util.UUID;

public final class InteracaoUseCase implements OperacoesInteracao {
    private final InteracaoRepository interacao;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    public InteracaoUseCase(InteracaoRepository interacao, EventoRepository eventos, UsuarioRepository usuarios) {
        this.interacao = interacao; this.eventos = eventos; this.usuarios = usuarios;
    }
    private void acessar(UUID usuarioId, UUID eventoId) {
        var usuario = usuarios.buscarPorId(usuarioId)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() == EstadoEvento.SUSPENSO || evento.getEstado() == EstadoEvento.EXCLUIDO)
            throw new RecursoNaoEncontradoException();
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !evento.getOrganizador().getId().equals(usuarioId)
                && !interacao.participanteAtivo(eventoId, usuarioId)) throw new AcessoNegadoException();
    }
    public List<DadosInteracao.Mensagem> listar(UUID usuarioId, UUID eventoId) {
        acessar(usuarioId, eventoId); return interacao.listar(eventoId);
    }
    public DadosInteracao.Mensagem publicar(UUID usuarioId, UUID eventoId, DadosInteracao.NovaMensagem dados) {
        acessar(usuarioId, eventoId);
        if (!interacao.participanteAtivo(eventoId, usuarioId)) throw new AcessoNegadoException();
        if (dados == null || dados.mensagem() == null || dados.mensagem().isBlank()
                || dados.mensagem().length() > 2000) throw new IllegalArgumentException("Mensagem deve ter até 2000 caracteres.");
        return interacao.publicar(eventoId, usuarioId, dados.mensagem().trim());
    }
}
