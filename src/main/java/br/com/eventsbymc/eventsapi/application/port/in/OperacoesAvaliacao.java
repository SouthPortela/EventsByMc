package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosAvaliacao;
import java.util.UUID;

public interface OperacoesAvaliacao {
    DadosAvaliacao.Questionario consultar(UUID eventoId);
    DadosAvaliacao.Questionario consultarGestao(UUID usuarioId, UUID eventoId);
    DadosAvaliacao.Questionario criar(UUID usuarioId, UUID eventoId, String titulo);
    DadosAvaliacao.Questao adicionarQuestao(UUID usuarioId, UUID eventoId, DadosAvaliacao.NovaQuestao questao);
    DadosAvaliacao.Questionario publicar(UUID usuarioId, UUID eventoId);
    UUID responder(UUID usuarioId, UUID eventoId, DadosAvaliacao.Envio envio);
    boolean respondeu(UUID usuarioId, UUID eventoId);
    DadosAvaliacao.Resultados resultados(UUID usuarioId, UUID eventoId);
    DadosAvaliacao.Questionario consultarAtividade(UUID atividadeId);
    DadosAvaliacao.Questionario consultarAtividadeGestao(UUID usuarioId, UUID atividadeId);
    DadosAvaliacao.Questionario criarAtividade(UUID usuarioId, UUID atividadeId, String titulo);
    DadosAvaliacao.Questao adicionarQuestaoAtividade(UUID usuarioId, UUID atividadeId, DadosAvaliacao.NovaQuestao questao);
    DadosAvaliacao.Questionario publicarAtividade(UUID usuarioId, UUID atividadeId);
    UUID responderAtividade(UUID usuarioId, UUID atividadeId, DadosAvaliacao.Envio envio);
    boolean respondeuAtividade(UUID usuarioId, UUID atividadeId);
    DadosAvaliacao.Resultados resultadosAtividade(UUID usuarioId, UUID atividadeId);
}
