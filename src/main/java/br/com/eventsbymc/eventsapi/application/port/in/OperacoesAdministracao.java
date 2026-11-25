package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao.*;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface OperacoesAdministracao {
    Resumo resumo(UUID responsavel);
    List<Conta> usuarios(UUID responsavel);
    List<EventoAdmin> eventos(UUID responsavel);
    List<Registro> auditoria(UUID responsavel);
    Conta alterarPerfis(UUID responsavel, UUID usuario, Set<Perfil> perfis);
    EventoAdmin bloquear(UUID responsavel, UUID evento, String motivo);
    EventoAdmin desbloquear(UUID responsavel, UUID evento);
}
