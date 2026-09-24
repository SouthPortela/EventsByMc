package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosProgramacao;
import java.util.List;
import java.util.UUID;

public interface OperacoesProgramacao {
    DadosProgramacao.Politica consultarPolitica(UUID usuarioId, UUID eventoId);
    DadosProgramacao.Politica atualizarPolitica(UUID usuarioId, UUID eventoId, DadosProgramacao.Politica politica);
    DadosProgramacao.Trilha criarTrilha(UUID usuarioId, UUID eventoId, DadosProgramacao.NovaTrilha dados);
    DadosProgramacao.Espaco criarEspaco(UUID usuarioId, UUID eventoId, DadosProgramacao.NovoEspaco dados);
    DadosProgramacao.Pessoa criarPessoa(UUID usuarioId, UUID eventoId, DadosProgramacao.NovaPessoa dados);
    void vincularPessoa(UUID usuarioId, UUID eventoId, UUID atividadeId, DadosProgramacao.NovoPapel dados);
    List<DadosProgramacao.Trilha> listarTrilhas(UUID usuarioId, UUID eventoId);
    List<DadosProgramacao.Espaco> listarEspacos(UUID usuarioId, UUID eventoId);
    List<DadosProgramacao.Pessoa> listarPessoas(UUID usuarioId, UUID eventoId);
    List<DadosProgramacao.Atividade> listarAtividades(UUID eventoId, DadosProgramacao.Filtros filtros);
    List<DadosProgramacao.Atividade> listarAtividadesDoOrganizador(UUID usuarioId, UUID eventoId);
    DadosProgramacao.Atividade criarAtividade(UUID usuarioId, UUID eventoId, DadosProgramacao.NovaAtividade dados);
    List<DadosProgramacao.AgendaItem> consultarAgenda(UUID usuarioId);
    DadosProgramacao.AgendaItem adicionarAgenda(UUID usuarioId, UUID atividadeId);
    void removerAgenda(UUID usuarioId, UUID atividadeId);
}
