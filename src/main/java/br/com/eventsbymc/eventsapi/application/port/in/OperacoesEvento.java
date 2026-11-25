package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosEvento;
import br.com.eventsbymc.eventsapi.application.usecase.DadosNovoEvento;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;
import java.util.List;
import java.util.UUID;

public interface OperacoesEvento {
    List<DadosEvento> listarPublicados();
    DadosEvento consultarPublicado(UUID eventoId);
    List<DadosEvento> listarDoOrganizador(UUID usuarioId);
    DadosEvento criar(UUID usuarioId, DadosNovoEvento dados);
    java.util.List<br.com.eventsbymc.eventsapi.application.usecase.DadosHistoricoEvento> historico(UUID usuarioId, UUID eventoId);
    DadosEvento consultarGestao(UUID usuarioId, UUID eventoId);
    DadosEvento editar(UUID usuarioId, UUID eventoId, DadosNovoEvento dados);
    DadosEvento alterarCategoria(UUID usuarioId, UUID eventoId, CategoriaEvento categoria);
    DadosEvento alterarEstado(UUID usuarioId, UUID eventoId, EstadoEvento destino);
}
