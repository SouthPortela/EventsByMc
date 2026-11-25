package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AdministracaoRepository {
    Resumo resumo();
    List<Registro> auditoria();
    void alterarPerfis(UUID responsavel, UUID usuario, Set<Perfil> perfis);
    void alterarBloqueio(UUID responsavel, Evento evento, EstadoEvento esperado);
}
