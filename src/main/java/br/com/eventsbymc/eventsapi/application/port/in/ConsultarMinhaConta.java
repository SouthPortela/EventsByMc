package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosMinhaConta;
import java.util.UUID;

public interface ConsultarMinhaConta {
    DadosMinhaConta executar(UUID usuarioAutenticadoId);
}
