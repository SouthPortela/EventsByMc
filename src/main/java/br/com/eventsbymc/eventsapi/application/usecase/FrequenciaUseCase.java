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
    private void gerenciar(UUID responsavelId, UUID atividadeId) { gerenciar(responsavelId, atividadeId, true); }
    private void gerenciar(UUID responsavelId, UUID atividadeId, boolean escrita) {
        var usuario = usuarios.buscarPorId(responsavelId)
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        var evento = eventos.buscarPorId(frequencia.eventoDaAtividade(atividadeId))
                .orElseThrow(RecursoNaoEncontradoException::new);
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR) && evento.getOrganizador().getId().equals(responsavelId)))
            throw new AcessoNegadoException();
        if (escrita) AcessoEvento.exigirDisponivel(evento);
    }
    public DadosFrequencia.Ajuste ajustar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoAjuste dados) {
        gerenciar(responsavelId, atividadeId);
        if (dados == null || dados.id() == null || dados.usuarioId() == null || dados.confirmada() == null
                || dados.motivo() == null || dados.motivo().trim().length() < 5 || dados.motivo().trim().length() > 1000)
            throw new IllegalArgumentException("Informe participante, situação e motivo entre 5 e 1000 caracteres.");
        return frequencia.ajustar(atividadeId, responsavelId,
                new DadosFrequencia.NovoAjuste(dados.id(), dados.usuarioId(), dados.confirmada(), dados.motivo().trim()));
    }
    public List<DadosFrequencia.Ajuste> ajustes(UUID responsavelId, UUID atividadeId) {
        gerenciar(responsavelId, atividadeId, false); return frequencia.ajustes(atividadeId);
    }
    public DadosFrequencia.Registro registrar(UUID responsavelId, UUID atividadeId, DadosFrequencia.NovoRegistro dados) {
        gerenciar(responsavelId, atividadeId);
        if (dados == null || dados.usuarioId() == null || dados.marcacao() == null
                || !List.of("CONFIRMACAO", "ENTRADA", "SAIDA").contains(dados.marcacao()))
            throw new IllegalArgumentException("Marcação inválida.");
        return frequencia.registrar(atividadeId, dados.usuarioId(), responsavelId, dados.marcacao());
    }
    public List<DadosFrequencia.Registro> listar(UUID responsavelId, UUID atividadeId) {
        gerenciar(responsavelId, atividadeId, false); return frequencia.listar(atividadeId);
    }
}
