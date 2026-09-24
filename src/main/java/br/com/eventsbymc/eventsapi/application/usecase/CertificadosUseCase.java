package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.exception.RecursoNaoEncontradoException;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesCertificado;
import br.com.eventsbymc.eventsapi.application.port.out.CertificadoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.EmailCertificado;
import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.RelatoriosRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.UUID;

public final class CertificadosUseCase implements OperacoesCertificado {
    private final CertificadoRepository certificados;
    private final RelatoriosRepository relatorios;
    private final EventoRepository eventos;
    private final UsuarioRepository usuarios;
    private final EmailCertificado email;
    public CertificadosUseCase(CertificadoRepository certificados, RelatoriosRepository relatorios,
                               EventoRepository eventos, UsuarioRepository usuarios, EmailCertificado email) {
        this.certificados = certificados; this.relatorios = relatorios; this.eventos = eventos;
        this.usuarios = usuarios; this.email = email;
    }
    private void autenticado(UUID usuarioId) {
        if (usuarioId == null || usuarios.buscarPorId(usuarioId).isEmpty())
            throw new TokenInvalidoException("Sessão inválida.", null);
    }
    private void eventoEncerrado(UUID eventoId) {
        var evento = eventos.buscarPorId(eventoId).orElseThrow(RecursoNaoEncontradoException::new);
        if (evento.getEstado() != EstadoEvento.ENCERRADO)
            throw new ConflitoOperacaoException("Certificados disponíveis após o encerramento do evento.");
    }
    public DadosCertificado participante(UUID usuarioId, UUID eventoId) {
        autenticado(usuarioId); eventoEncerrado(eventoId);
        var frequencia = relatorios.frequenciaDoParticipante(usuarioId, eventoId);
        if (!frequencia.elegivelCertificado())
            throw new ConflitoOperacaoException("Frequência insuficiente ou inscrição cancelada.");
        return certificados.emitirParticipante(eventoId, usuarioId);
    }
    public DadosCertificado pessoa(UUID usuarioId, UUID eventoId, UUID pessoaId, String papel) {
        autenticado(usuarioId); eventoEncerrado(eventoId);
        var usuario = usuarios.buscarPorId(usuarioId).orElseThrow();
        var evento = eventos.buscarPorId(eventoId).orElseThrow();
        if (!usuario.possuiPerfil(Perfil.ADMINISTRADOR)
                && !(usuario.possuiPerfil(Perfil.ORGANIZADOR) && evento.getOrganizador().getId().equals(usuarioId)))
            throw new AcessoNegadoException();
        if (!"PALESTRANTE".equals(papel) && !"APRESENTADOR".equals(papel))
            throw new IllegalArgumentException("Papel inválido.");
        return certificados.emitirPessoa(eventoId, pessoaId, papel);
    }
    public DadosCertificado enviarParticipante(UUID usuarioId, UUID eventoId, byte[] pdf) {
        var certificado = participante(usuarioId, eventoId);
        enviar(certificado, pdf); return certificados.emitirParticipante(eventoId, usuarioId);
    }
    public DadosCertificado enviarPessoa(UUID usuarioId, UUID eventoId, UUID pessoaId, String papel, byte[] pdf) {
        var certificado = pessoa(usuarioId, eventoId, pessoaId, papel);
        enviar(certificado, pdf); return certificados.emitirPessoa(eventoId, pessoaId, papel);
    }
    private void enviar(DadosCertificado certificado, byte[] pdf) {
        if (certificado.email() == null || certificado.email().isBlank())
            throw new ConflitoOperacaoException("Destinatário sem e-mail cadastrado.");
        email.enviar(certificado.email(), "Certificado - " + certificado.eventoTitulo(),
                "certificado.pdf", pdf);
        certificados.marcarEnviado(certificado.id());
    }
}
