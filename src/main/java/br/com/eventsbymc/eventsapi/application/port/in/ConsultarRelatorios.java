package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosRelatorios;
import java.util.List;
import java.util.UUID;

public interface ConsultarRelatorios {
    List<DadosRelatorios.Inscrito> inscritos(UUID usuarioId, UUID eventoId);
    List<DadosRelatorios.Frequencia> frequencias(UUID usuarioId, UUID eventoId);
    DadosRelatorios.Frequencia minhaFrequencia(UUID usuarioId, UUID eventoId);
}
