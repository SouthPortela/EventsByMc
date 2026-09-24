package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosInteracao;
import java.util.List;
import java.util.UUID;

public interface OperacoesInteracao {
    List<DadosInteracao.Mensagem> listar(UUID usuarioId, UUID eventoId);
    DadosInteracao.Mensagem publicar(UUID usuarioId, UUID eventoId, DadosInteracao.NovaMensagem dados);
}
