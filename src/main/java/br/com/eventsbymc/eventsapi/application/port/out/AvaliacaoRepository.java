package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosAvaliacao;
import java.util.List;
import java.util.UUID;

public interface AvaliacaoRepository {
    DadosAvaliacao.Questionario consultar(UUID eventoId);
    DadosAvaliacao.Questionario criar(UUID eventoId, String titulo);
    DadosAvaliacao.Questao adicionarQuestao(UUID eventoId, DadosAvaliacao.NovaQuestao questao);
    DadosAvaliacao.Questionario publicar(UUID eventoId);
    UUID responder(UUID eventoId, UUID usuarioId, List<DadosAvaliacao.Resposta> respostas);
    boolean respondeu(UUID eventoId, UUID usuarioId);
    DadosAvaliacao.Resultados resultados(UUID eventoId);
    default UUID eventoDaAtividade(UUID atividadeId) { throw new UnsupportedOperationException(); }
    default DadosAvaliacao.Questionario consultarAtividade(UUID eventoId, UUID atividadeId) { throw new UnsupportedOperationException(); }
    default DadosAvaliacao.Questionario criarAtividade(UUID eventoId, UUID atividadeId, String titulo) { throw new UnsupportedOperationException(); }
    default DadosAvaliacao.Questao adicionarQuestaoAtividade(UUID eventoId, UUID atividadeId, DadosAvaliacao.NovaQuestao questao) { throw new UnsupportedOperationException(); }
    default DadosAvaliacao.Questionario publicarAtividade(UUID eventoId, UUID atividadeId) { throw new UnsupportedOperationException(); }
    default UUID responderAtividade(UUID eventoId, UUID atividadeId, UUID usuarioId, List<DadosAvaliacao.Resposta> respostas) { throw new UnsupportedOperationException(); }
    default boolean respondeuAtividade(UUID eventoId, UUID atividadeId, UUID usuarioId) { throw new UnsupportedOperationException(); }
    default DadosAvaliacao.Resultados resultadosAtividade(UUID eventoId, UUID atividadeId) { throw new UnsupportedOperationException(); }
}
