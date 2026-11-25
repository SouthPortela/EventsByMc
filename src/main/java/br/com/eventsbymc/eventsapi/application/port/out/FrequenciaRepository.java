package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosFrequencia;
import java.util.List;
import java.util.UUID;

public interface FrequenciaRepository {
    DadosFrequencia.Ajuste ajustar(UUID atividadeId, UUID responsavelId, DadosFrequencia.NovoAjuste ajuste);
    List<DadosFrequencia.Ajuste> ajustes(UUID atividadeId);
    UUID eventoDaAtividade(UUID atividadeId);
    DadosFrequencia.Registro registrar(UUID atividadeId, UUID usuarioId, UUID responsavelId, String marcacao);
    List<DadosFrequencia.Registro> listar(UUID atividadeId);
}
