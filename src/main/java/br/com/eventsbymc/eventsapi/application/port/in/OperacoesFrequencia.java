package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosFrequencia;
import java.util.List;
import java.util.UUID;

public interface OperacoesFrequencia {
    DadosFrequencia.Ajuste ajustar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoAjuste dados);
    List<DadosFrequencia.Ajuste> ajustes(UUID responsavelId, UUID atividadeId);
    DadosFrequencia.Registro registrar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoRegistro dados);
    List<DadosFrequencia.Registro> listar(UUID responsavelId, UUID atividadeId);
}
