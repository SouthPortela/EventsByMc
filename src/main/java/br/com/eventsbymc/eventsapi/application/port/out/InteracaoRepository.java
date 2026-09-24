package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosInteracao;
import java.util.List;
import java.util.UUID;

public interface InteracaoRepository {
    boolean participanteAtivo(UUID eventoId, UUID usuarioId);
    List<DadosInteracao.Mensagem> listar(UUID eventoId);
    DadosInteracao.Mensagem publicar(UUID eventoId, UUID usuarioId, String mensagem);
}
