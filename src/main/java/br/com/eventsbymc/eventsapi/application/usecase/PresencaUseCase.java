package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesPresenca;
import br.com.eventsbymc.eventsapi.application.port.out.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.util.*;

public final class PresencaUseCase implements OperacoesPresenca {
    private final PresencaRepository presencas;
    private final UsuarioRepository usuarios;
    private final EventoRepository eventos;
    private final GeradorCodigoPresenca codigos;
    public PresencaUseCase(PresencaRepository presencas, UsuarioRepository usuarios,
                           EventoRepository eventos, GeradorCodigoPresenca codigos) {
        this.presencas = Objects.requireNonNull(presencas);
        this.usuarios = Objects.requireNonNull(usuarios);
        this.eventos = Objects.requireNonNull(eventos);
        this.codigos = Objects.requireNonNull(codigos);
    }
    private Usuario usuario(UUID id) {
        return usuarios.buscarPorId(id).orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
    }
    private Evento gerenciar(UUID usuarioId, UUID eventoId) {
        var usuario = usuario(usuarioId);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() == EstadoEvento.SUSPENSO || evento.getEstado() == EstadoEvento.EXCLUIDO)
            throw new RecursoNaoEncontradoException();
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR) && evento.getOrganizador().getId().equals(usuarioId)))
            throw new AcessoNegadoException();
        return evento;
    }
    public DadosPresenca.Participacao consultarParticipacao(UUID usuarioId) {
        usuario(usuarioId);
        return presencas.consultarParticipacao(usuarioId);
    }
    public DadosPresenca.Inscricao inscrever(UUID usuarioId, UUID eventoId) {
        usuario(usuarioId);
        return presencas.inscrever(usuarioId, eventoId);
    }
    public DadosPresenca.Inscricao cancelarInscricao(UUID usuarioId, UUID eventoId) {
        usuario(usuarioId);
        return presencas.cancelarInscricao(usuarioId, eventoId);
    }
    public List<DadosPresenca.Atividade> listarAtividades(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return presencas.listarAtividades(eventoId);
    }
    public DadosPresenca.Atividade criarAtividade(UUID usuarioId, UUID eventoId, DadosPresenca.NovaAtividade dados) {
        var evento = gerenciar(usuarioId, eventoId);
        if (dados == null || dados.dataInicio() == null || dados.dataFim() == null)
            throw new IllegalArgumentException("Informe o período da atividade.");
        if (evento.getInicio() == null) throw new ConflitoOperacaoException("Defina primeiro o período do evento.");
        var atividade = new Atividade(texto(dados.titulo(), 200), dados.descricao(),
                dados.dataInicio(), dados.dataFim(), texto(dados.local(), 200), null);
        if (dados.descricao() != null && dados.descricao().length() > 10000) throw new IllegalArgumentException("Descrição muito longa.");
        try { evento.adicionarAtividade(atividade); }
        catch (IllegalStateException e) { throw new ConflitoOperacaoException(e.getMessage()); }
        presencas.adicionarAtividade(eventoId, atividade);
        return new DadosPresenca.Atividade(atividade.getId(), atividade.getTitulo(), atividade.getInicio(), atividade.getFim(), atividade.getLocal());
    }
    public DadosPresenca.ChamadaGerada gerar(UUID usuarioId, UUID atividadeId) {
        gerenciar(usuarioId, presencas.eventoDaAtividade(atividadeId));
        presencas.limitarTentativas(usuarioId, "GERAR");
        String codigo = codigos.gerar();
        var chamada = presencas.gerarChamada(usuarioId, atividadeId, codigos.hash(codigo));
        return new DadosPresenca.ChamadaGerada(chamada.id(), chamada.atividade(), codigo, chamada.expiraEm());
    }
    public DadosPresenca.Confirmacao confirmar(UUID usuarioId, String codigo, OrigemPresenca origem) {
        usuario(usuarioId);
        // Registrar tentativa antes de validar formato/código; falhas também contam.
        presencas.limitarTentativas(usuarioId, "CONFIRMAR");
        if (origem == null) throw new IllegalArgumentException("Informe a origem da confirmação.");
        return presencas.confirmar(usuarioId, codigos.hash(codigo), origem);
    }
    private static String texto(String valor, int limite) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite)
            throw new IllegalArgumentException("Informe título/local com até " + limite + " caracteres.");
        return valor.trim();
    }
}
