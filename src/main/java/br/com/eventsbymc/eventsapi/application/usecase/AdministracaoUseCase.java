package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesAdministracao;
import br.com.eventsbymc.eventsapi.application.port.out.*;
import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.util.*;

public final class AdministracaoUseCase implements OperacoesAdministracao {
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final AdministracaoRepository administracao;
    public AdministracaoUseCase(UsuarioRepository usuarios, EventoRepository eventos, AdministracaoRepository administracao) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.eventos = Objects.requireNonNull(eventos);
        this.administracao = Objects.requireNonNull(administracao);
    }
    private void autorizar(UUID id) {
        if (id == null || !usuarios.buscarPorId(id).map(u -> u.possuiPerfil(Perfil.ADMINISTRADOR)).orElse(false))
            throw new AcessoNegadoException();
    }
    private static Conta conta(Usuario u) {
        return new Conta(u.getId(), u.getPessoa().getNome(), u.getPessoa().getEmail(), u.getPerfis());
    }
    private static EventoAdmin dados(Evento e) {
        return new EventoAdmin(DadosEvento.de(e), e.getOrganizador().getId(), e.getOrganizador().getPessoa().getNome());
    }
    public Resumo resumo(UUID id) { autorizar(id); return administracao.resumo(); }
    public List<Conta> usuarios(UUID id) { autorizar(id); return usuarios.listar().stream().map(AdministracaoUseCase::conta).toList(); }
    public List<EventoAdmin> eventos(UUID id) { autorizar(id); return eventos.listar().stream().map(AdministracaoUseCase::dados).toList(); }
    public List<Registro> auditoria(UUID id) { autorizar(id); return administracao.auditoria(); }
    public Conta alterarPerfis(UUID id, UUID alvo, Set<Perfil> perfis) {
        autorizar(id);
        PoliticaPerfis.validar(perfis);
        administracao.alterarPerfis(id, alvo, Set.copyOf(perfis));
        return conta(usuarios.buscarPorId(alvo).orElseThrow(RecursoNaoEncontradoException::new));
    }
    public EventoAdmin bloquear(UUID id, UUID eventoId, String motivo) { return moderar(id, eventoId, motivo, true); }
    public EventoAdmin desbloquear(UUID id, UUID eventoId) { return moderar(id, eventoId, null, false); }
    private EventoAdmin moderar(UUID id, UUID eventoId, String motivo, boolean bloquear) {
        autorizar(id);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        var anterior = evento.getEstado();
        try { if (bloquear) evento.bloquear(motivo); else evento.desbloquear(); }
        catch (IllegalStateException e) { throw new ConflitoOperacaoException(e.getMessage()); }
        administracao.alterarBloqueio(id, evento, anterior);
        return dados(evento);
    }
}
