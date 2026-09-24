package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosProgramacao;
import java.util.List;
import java.util.UUID;

public interface ProgramacaoRepository {
    DadosProgramacao.Politica consultarPolitica(UUID eventoId);
    DadosProgramacao.Politica atualizarPolitica(UUID eventoId, DadosProgramacao.Politica politica);
    DadosProgramacao.Trilha criarTrilha(UUID eventoId, String nome);
    DadosProgramacao.Espaco criarEspaco(UUID eventoId, String nome, Integer capacidade);
    DadosProgramacao.Pessoa criarPessoa(UUID eventoId, String nome, String email);
    void vincularPessoa(UUID eventoId, UUID atividadeId, UUID pessoaId, String papel);
    List<DadosProgramacao.Trilha> listarTrilhas(UUID eventoId);
    List<DadosProgramacao.Espaco> listarEspacos(UUID eventoId);
    List<DadosProgramacao.Pessoa> listarPessoas(UUID eventoId);
    List<DadosProgramacao.Atividade> listarAtividades(UUID eventoId, DadosProgramacao.Filtros filtros);
    DadosProgramacao.Atividade criarAtividade(UUID eventoId, DadosProgramacao.NovaAtividade dados);
    List<DadosProgramacao.AgendaItem> consultarAgenda(UUID usuarioId);
    DadosProgramacao.AgendaItem adicionarAgenda(UUID usuarioId, UUID atividadeId);
    void removerAgenda(UUID usuarioId, UUID atividadeId);
}
