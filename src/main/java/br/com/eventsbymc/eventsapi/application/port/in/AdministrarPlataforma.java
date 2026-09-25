package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosAdministracao;
import br.com.eventsbymc.eventsapi.domain.model.RegistroModeracaoEvento;
import java.util.List;
import java.util.UUID;

public interface AdministrarPlataforma {
    List<DadosAdministracao.Evento> listarEventos(UUID administradorId);
    DadosAdministracao.Evento consultarEvento(UUID administradorId, UUID eventoId);
    List<DadosAdministracao.Usuario> listarUsuarios(UUID administradorId);
    List<RegistroModeracaoEvento> listarModeracoes(UUID administradorId);
    DadosAdministracao.Evento suspender(UUID administradorId, UUID eventoId, String motivo);
    DadosAdministracao.Evento restaurar(UUID administradorId, UUID eventoId, String motivo);
    DadosAdministracao.Evento excluir(UUID administradorId, UUID eventoId, String motivo);
}
