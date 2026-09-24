package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosFrequencia;
import java.util.List;
import java.util.UUID;

public interface OperacoesFrequencia {
    DadosFrequencia.Registro registrar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoRegistro dados);
    List<DadosFrequencia.Registro> listar(UUID responsavelId, UUID atividadeId);
}
