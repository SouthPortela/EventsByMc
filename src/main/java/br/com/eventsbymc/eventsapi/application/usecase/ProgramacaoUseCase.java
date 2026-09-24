package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesProgramacao;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.ProgramacaoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.PapelAtividade;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.PoliticaFrequencia;
import br.com.eventsbymc.eventsapi.domain.model.TipoAtividade;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ProgramacaoUseCase implements OperacoesProgramacao {
    private final ProgramacaoRepository programacao;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;

    public ProgramacaoUseCase(ProgramacaoRepository programacao, EventoRepository eventos,
                             UsuarioRepository usuarios) {
        this.programacao = Objects.requireNonNull(programacao);
        this.eventos = Objects.requireNonNull(eventos);
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    private void usuarioAutenticado(UUID usuarioId) {
        if (usuarioId == null || usuarios.buscarPorId(usuarioId).isEmpty()) {
            throw new TokenInvalidoException("Sessão inválida.", null);
        }
    }

    private Evento gerenciar(UUID usuarioId, UUID eventoId) {
        usuarioAutenticado(usuarioId);
        var usuario = usuarios.buscarPorId(usuarioId).orElseThrow();
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR)
                && evento.getOrganizador().getId().equals(usuarioId))) {
            throw new AcessoNegadoException();
        }
        return evento;
    }

    private void publico(UUID eventoId) {
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.PUBLICADO) throw new RecursoNaoEncontradoException();
    }

    private static String texto(String valor, int limite, String campo) {
        if (valor == null || valor.isBlank() || valor.trim().length() > limite) {
            throw new IllegalArgumentException(campo + " deve ter entre 1 e " + limite + " caracteres.");
        }
        return valor.trim();
    }

    public DadosProgramacao.Politica consultarPolitica(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return programacao.consultarPolitica(eventoId);
    }

    public DadosProgramacao.Politica atualizarPolitica(UUID usuarioId, UUID eventoId,
                                                       DadosProgramacao.Politica politica) {
        gerenciar(usuarioId, eventoId);
        if (politica == null || politica.frequenciaMinimaPercentual() < 0
                || politica.frequenciaMinimaPercentual() > 100
                || politica.limiteInscritos() != null && politica.limiteInscritos() < 1
                || politica.inscricoesInicio() != null && politica.inscricoesFim() != null
                    && !politica.inscricoesFim().isAfter(politica.inscricoesInicio())) {
            throw new IllegalArgumentException("Política do evento inválida.");
        }
        return programacao.atualizarPolitica(eventoId, politica);
    }

    public DadosProgramacao.Trilha criarTrilha(UUID usuarioId, UUID eventoId, DadosProgramacao.NovaTrilha dados) {
        gerenciar(usuarioId, eventoId);
        return programacao.criarTrilha(eventoId, texto(dados == null ? null : dados.nome(), 120, "Trilha"));
    }

    public DadosProgramacao.Espaco criarEspaco(UUID usuarioId, UUID eventoId, DadosProgramacao.NovoEspaco dados) {
        gerenciar(usuarioId, eventoId);
        if (dados == null || dados.capacidade() != null && dados.capacidade() < 1) {
            throw new IllegalArgumentException("Capacidade do espaço inválida.");
        }
        return programacao.criarEspaco(eventoId, texto(dados.nome(), 120, "Espaço"), dados.capacidade());
    }

    public DadosProgramacao.Pessoa criarPessoa(UUID usuarioId, UUID eventoId, DadosProgramacao.NovaPessoa dados) {
        gerenciar(usuarioId, eventoId);
        String nome = texto(dados == null ? null : dados.nome(), 150, "Nome");
        String email = dados.email() == null || dados.email().isBlank() ? null : dados.email().trim();
        if (email != null && (email.length() > 255 || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        return programacao.criarPessoa(eventoId, nome, email);
    }

    public void vincularPessoa(UUID usuarioId, UUID eventoId, UUID atividadeId, DadosProgramacao.NovoPapel dados) {
        gerenciar(usuarioId, eventoId);
        if (dados == null || dados.pessoaId() == null || dados.papel() == null) {
            throw new IllegalArgumentException("Pessoa e papel são obrigatórios.");
        }
        PapelAtividade papel = PapelAtividade.valueOf(dados.papel());
        programacao.vincularPessoa(eventoId, atividadeId, dados.pessoaId(), papel.name());
    }

    public List<DadosProgramacao.Trilha> listarTrilhas(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return programacao.listarTrilhas(eventoId);
    }
    public List<DadosProgramacao.Espaco> listarEspacos(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return programacao.listarEspacos(eventoId);
    }
    public List<DadosProgramacao.Pessoa> listarPessoas(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return programacao.listarPessoas(eventoId);
    }
    public List<DadosProgramacao.Atividade> listarAtividades(UUID eventoId, DadosProgramacao.Filtros filtros) {
        publico(eventoId);
        return programacao.listarAtividades(eventoId, filtros);
    }

    public List<DadosProgramacao.Atividade> listarAtividadesDoOrganizador(UUID usuarioId, UUID eventoId) {
        gerenciar(usuarioId, eventoId);
        return programacao.listarAtividades(eventoId, null);
    }

    public DadosProgramacao.Atividade criarAtividade(UUID usuarioId, UUID eventoId,
                                                    DadosProgramacao.NovaAtividade dados) {
        var evento = gerenciar(usuarioId, eventoId);
        if (evento.getEstado() == EstadoEvento.ENCERRADO)
            throw new ConflitoOperacaoException("Evento encerrado não aceita novas atividades.");
        if (dados == null || dados.dataInicio() == null || dados.dataFim() == null
                || !dados.dataFim().isAfter(dados.dataInicio())
                || evento.getInicio() == null || dados.dataInicio().isBefore(evento.getInicio())
                || dados.dataFim().isAfter(evento.getFim())
                || dados.capacidade() != null && dados.capacidade() < 1) {
            throw new IllegalArgumentException("Período ou capacidade da atividade inválido.");
        }
        String titulo = texto(dados.titulo(), 200, "Título");
        String local = texto(dados.local(), 200, "Local");
        if (dados.descricao() != null && dados.descricao().length() > 10000) {
            throw new IllegalArgumentException("Descrição muito longa.");
        }
        String tipo = dados.tipo() == null ? TipoAtividade.PALESTRA.name()
                : TipoAtividade.valueOf(dados.tipo()).name();
        String politica = dados.politicaFrequencia() == null ? PoliticaFrequencia.CHECKIN_UNICO.name()
                : PoliticaFrequencia.valueOf(dados.politicaFrequencia()).name();
        int permanencia = dados.permanenciaMinimaPercentual() == null ? 75 : dados.permanenciaMinimaPercentual();
        if (permanencia < 1 || permanencia > 100)
            throw new IllegalArgumentException("Permanência mínima deve estar entre 1% e 100%.");
        var validos = new DadosProgramacao.NovaAtividade(titulo, dados.descricao(),
                dados.dataInicio(), dados.dataFim(), local, dados.capacidade(),
                dados.trilhaId(), dados.espacoId(), tipo,
                dados.presencaObrigatoria() == null || dados.presencaObrigatoria(),
                politica, permanencia);
        return programacao.criarAtividade(eventoId, validos);
    }

    public List<DadosProgramacao.AgendaItem> consultarAgenda(UUID usuarioId) {
        usuarioAutenticado(usuarioId);
        return programacao.consultarAgenda(usuarioId);
    }
    public DadosProgramacao.AgendaItem adicionarAgenda(UUID usuarioId, UUID atividadeId) {
        usuarioAutenticado(usuarioId);
        return programacao.adicionarAgenda(usuarioId, atividadeId);
    }
    public void removerAgenda(UUID usuarioId, UUID atividadeId) {
        usuarioAutenticado(usuarioId);
        programacao.removerAgenda(usuarioId, atividadeId);
    }
}
