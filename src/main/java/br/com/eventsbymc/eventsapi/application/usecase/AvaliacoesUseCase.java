package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesAvaliacao;
import br.com.eventsbymc.eventsapi.application.port.out.AvaliacaoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.List;
import java.util.UUID;

public final class AvaliacoesUseCase implements OperacoesAvaliacao {
    private final AvaliacaoRepository avaliacoes;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    public AvaliacoesUseCase(AvaliacaoRepository avaliacoes, EventoRepository eventos, UsuarioRepository usuarios) {
        this.avaliacoes = avaliacoes; this.eventos = eventos; this.usuarios = usuarios;
    }

    private void autenticado(UUID usuarioId) {
        if (usuarioId == null || usuarios.buscarPorId(usuarioId).isEmpty())
            throw new TokenInvalidoException("Sessão inválida.", null);
    }
    private void gerenciar(UUID usuarioId, UUID eventoId) {
        autenticado(usuarioId);
        var usuario = usuarios.buscarPorId(usuarioId).orElseThrow();
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR) && evento.getOrganizador().getId().equals(usuarioId)))
            throw new AcessoNegadoException();
    }
    public DadosAvaliacao.Questionario consultar(UUID eventoId) {
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.ENCERRADO)
            throw new RecursoNaoEncontradoException();
        var questionario = avaliacoes.consultar(eventoId);
        if (questionario == null || !questionario.ativo()) throw new RecursoNaoEncontradoException();
        return questionario;
    }
    public DadosAvaliacao.Questionario consultarGestao(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        var questionario = avaliacoes.consultar(eventoId);
        if (questionario == null) throw new RecursoNaoEncontradoException();
        return questionario;
    }
    public DadosAvaliacao.Questionario criar(UUID usuarioId, UUID eventoId, String titulo) {
        gerenciar(usuarioId, eventoId);
        if (titulo == null || titulo.isBlank() || titulo.length() > 160) throw new IllegalArgumentException("Título inválido.");
        return avaliacoes.criar(eventoId, titulo.trim());
    }
    public DadosAvaliacao.Questao adicionarQuestao(UUID usuarioId, UUID eventoId, DadosAvaliacao.NovaQuestao questao) {
        gerenciar(usuarioId, eventoId);
        validarQuestao(questao);
        return avaliacoes.adicionarQuestao(eventoId, questao);
    }
    private static void validarQuestao(DadosAvaliacao.NovaQuestao questao) {
        if (questao == null || questao.enunciado() == null || questao.enunciado().isBlank()
                || questao.enunciado().length() > 500 || questao.tipo() == null)
            throw new IllegalArgumentException("Questão inválida.");
        switch (questao.tipo()) {
            case "TEXTO" -> {
                if (questao.opcoes() != null || questao.escalaMinima() != null || questao.escalaMaxima() != null)
                    throw new IllegalArgumentException("Configuração de texto inválida.");
            }
            case "ESCOLHA_UNICA" -> {
                List<String> opcoes = questao.opcoes();
                if (opcoes == null || opcoes.size() < 2 || opcoes.size() > 20
                        || opcoes.stream().anyMatch(o -> o == null || o.isBlank() || o.length() > 120)
                        || opcoes.stream().distinct().count() != opcoes.size()
                        || questao.escalaMinima() != null || questao.escalaMaxima() != null)
                    throw new IllegalArgumentException("Opções inválidas.");
            }
            case "ESCALA" -> {
                if (questao.opcoes() != null || questao.escalaMinima() == null || questao.escalaMaxima() == null
                        || questao.escalaMinima() < 0 || questao.escalaMaxima() > 10
                        || questao.escalaMaxima() <= questao.escalaMinima())
                    throw new IllegalArgumentException("Escala inválida.");
            }
            default -> throw new IllegalArgumentException("Tipo de questão inválido.");
        }
    }
    public DadosAvaliacao.Questionario publicar(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId); return avaliacoes.publicar(eventoId);
    }
    public UUID responder(UUID usuarioId, UUID eventoId, DadosAvaliacao.Envio envio) {
        autenticado(usuarioId);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.ENCERRADO)
            throw new RecursoNaoEncontradoException();
        if (envio == null || envio.respostas() == null) throw new IllegalArgumentException("Respostas obrigatórias.");
        return avaliacoes.responder(eventoId, usuarioId, envio.respostas());
    }
    public boolean respondeu(UUID usuarioId, UUID eventoId) {
        autenticado(usuarioId); return avaliacoes.respondeu(eventoId, usuarioId);
    }
    public DadosAvaliacao.Resultados resultados(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId); return avaliacoes.resultados(eventoId);
    }

    private UUID eventoDaAtividade(UUID atividadeId) {
        if (atividadeId == null) throw new IllegalArgumentException("Atividade obrigatória.");
        return avaliacoes.eventoDaAtividade(atividadeId);
    }
    public DadosAvaliacao.Questionario consultarAtividade(UUID atividadeId) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.ENCERRADO)
            throw new RecursoNaoEncontradoException();
        var q = avaliacoes.consultarAtividade(eventoId, atividadeId);
        if (q == null || !q.ativo()) throw new RecursoNaoEncontradoException();
        return q;
    }
    public DadosAvaliacao.Questionario consultarAtividadeGestao(UUID usuarioId, UUID atividadeId) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        gerenciar(usuarioId, eventoId);
        var q = avaliacoes.consultarAtividade(eventoId, atividadeId);
        if (q == null) throw new RecursoNaoEncontradoException();
        return q;
    }
    public DadosAvaliacao.Questionario criarAtividade(UUID usuarioId, UUID atividadeId, String titulo) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        gerenciar(usuarioId, eventoId);
        if (titulo == null || titulo.isBlank() || titulo.length() > 160) throw new IllegalArgumentException("Título inválido.");
        return avaliacoes.criarAtividade(eventoId, atividadeId, titulo.trim());
    }
    public DadosAvaliacao.Questao adicionarQuestaoAtividade(UUID usuarioId, UUID atividadeId, DadosAvaliacao.NovaQuestao questao) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        gerenciar(usuarioId, eventoId); validarQuestao(questao);
        return avaliacoes.adicionarQuestaoAtividade(eventoId, atividadeId, questao);
    }
    public DadosAvaliacao.Questionario publicarAtividade(UUID usuarioId, UUID atividadeId) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        gerenciar(usuarioId, eventoId); return avaliacoes.publicarAtividade(eventoId, atividadeId);
    }
    public UUID responderAtividade(UUID usuarioId, UUID atividadeId, DadosAvaliacao.Envio envio) {
        autenticado(usuarioId);
        UUID eventoId = eventoDaAtividade(atividadeId);
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.PUBLICADO && evento.getEstado() != EstadoEvento.ENCERRADO)
            throw new RecursoNaoEncontradoException();
        if (envio == null || envio.respostas() == null) throw new IllegalArgumentException("Respostas obrigatórias.");
        return avaliacoes.responderAtividade(eventoId, atividadeId, usuarioId, envio.respostas());
    }
    public boolean respondeuAtividade(UUID usuarioId, UUID atividadeId) {
        autenticado(usuarioId);
        UUID eventoId = eventoDaAtividade(atividadeId);
        return avaliacoes.respondeuAtividade(eventoId, atividadeId, usuarioId);
    }
    public DadosAvaliacao.Resultados resultadosAtividade(UUID usuarioId, UUID atividadeId) {
        UUID eventoId = eventoDaAtividade(atividadeId);
        gerenciar(usuarioId, eventoId); return avaliacoes.resultadosAtividade(eventoId, atividadeId);
    }
}
