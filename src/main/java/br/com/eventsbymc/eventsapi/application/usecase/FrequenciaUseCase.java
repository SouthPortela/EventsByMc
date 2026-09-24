package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesFrequencia;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.FrequenciaRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.List;
import java.util.UUID;

public final class FrequenciaUseCase implements OperacoesFrequencia {
    private final FrequenciaRepository frequencia;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    public FrequenciaUseCase(FrequenciaRepository frequencia, EventoRepository eventos, UsuarioRepository usuarios) {
        this.frequencia = frequencia; this.eventos = eventos; this.usuarios = usuarios;
    }
    private void gerenciar(UUID responsavelId, UUID atividadeId) {
        var usuario = usuarios.buscarPorId(responsavelId)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        var evento = eventos.buscarPorId(frequencia.eventoDaAtividade(atividadeId))
                .orElseThrow(RecursoNaoEncontradoException::new);
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR) && evento.getOrganizador().getId().equals(responsavelId)))
            throw new AcessoNegadoException();
    }
    public DadosFrequencia.Registro registrar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoRegistro dados) {
        gerenciar(responsavelId, atividadeId);
        if (dados == null || dados.usuarioId() == null || dados.marcacao() == null
                || !List.of("CONFIRMACAO", "ENTRADA", "SAIDA").contains(dados.marcacao()))
            throw new IllegalArgumentException("Marcação inválida.");
        return frequencia.registrar(atividadeId, dados.usuarioId(), responsavelId, dados.marcacao());
    }
    public List<DadosFrequencia.Registro> listar(UUID responsavelId, UUID atividadeId) {
        gerenciar(responsavelId, atividadeId); return frequencia.listar(atividadeId);
    }
}
