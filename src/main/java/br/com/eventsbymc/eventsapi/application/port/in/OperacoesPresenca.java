package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosPresenca;
import br.com.eventsbymc.eventsapi.domain.model.OrigemPresenca;
import java.util.List;
import java.util.UUID;

public interface OperacoesPresenca {
    DadosPresenca.Inscricao inscrever(UUID usuarioId, UUID eventoId);
    List<DadosPresenca.Atividade> listarAtividades(UUID usuarioId, UUID eventoId);
    DadosPresenca.Atividade criarAtividade(UUID usuarioId, UUID eventoId, DadosPresenca.NovaAtividade dados);
    DadosPresenca.ChamadaGerada gerar(UUID usuarioId, UUID atividadeId);
    DadosPresenca.Confirmacao confirmar(UUID usuarioId, String codigo, OrigemPresenca origem);
}
