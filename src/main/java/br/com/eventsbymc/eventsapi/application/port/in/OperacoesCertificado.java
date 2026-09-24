package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosCertificado;
import java.util.UUID;

public interface OperacoesCertificado {
    DadosCertificado participante(UUID usuarioId, UUID eventoId);
    DadosCertificado pessoa(UUID usuarioId, UUID eventoId, UUID pessoaId, String papel);
    DadosCertificado enviarParticipante(UUID usuarioId, UUID eventoId, byte[] pdf);
    DadosCertificado enviarPessoa(UUID usuarioId, UUID eventoId, UUID pessoaId, String papel, byte[] pdf);
}
