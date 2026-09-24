package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosCertificado;
import java.util.UUID;

public interface CertificadoRepository {
    DadosCertificado emitirParticipante(UUID eventoId, UUID usuarioId);
    DadosCertificado emitirPessoa(UUID eventoId, UUID pessoaId, String papel);
    void marcarEnviado(UUID certificadoId);
}
