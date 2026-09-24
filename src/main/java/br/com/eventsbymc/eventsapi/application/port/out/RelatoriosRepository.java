package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosRelatorios;
import java.util.List;
import java.util.UUID;

public interface RelatoriosRepository {
    List<DadosRelatorios.Inscrito> inscritos(UUID eventoId);
    List<DadosRelatorios.Frequencia> frequencias(UUID eventoId);
    DadosRelatorios.Frequencia frequenciaDoParticipante(UUID usuarioId, UUID eventoId);
}
