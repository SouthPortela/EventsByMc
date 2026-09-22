package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosPresenca;
import br.com.eventsbymc.eventsapi.domain.model.Atividade;
import br.com.eventsbymc.eventsapi.domain.model.OrigemPresenca;
import java.util.List;
import java.util.UUID;

// Operações atômicas: inscrição, emissão/revogação e confirmação com unicidade.
public interface PresencaRepository {
    DadosPresenca.Inscricao inscrever(UUID usuarioId, UUID eventoId);
    List<DadosPresenca.Atividade> listarAtividades(UUID eventoId);
    void adicionarAtividade(UUID eventoId, Atividade atividade);
    UUID eventoDaAtividade(UUID atividadeId);
    void limitarTentativas(UUID usuarioId, String operacao);
    DadosPresenca.Chamada gerarChamada(UUID organizadorId, UUID atividadeId, String codigoHash);
    DadosPresenca.Confirmacao confirmar(UUID usuarioId, String codigoHash, OrigemPresenca origem);
}
