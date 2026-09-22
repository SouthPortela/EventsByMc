package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import java.util.Objects;
import java.util.UUID;

public final class ConsultarMinhaContaUseCase implements ConsultarMinhaConta {
    private final UsuarioRepository usuarios;

    public ConsultarMinhaContaUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override
    public DadosMinhaConta executar(UUID usuarioAutenticadoId) {
        if (usuarioAutenticadoId == null) throw sessaoInvalida();
        var usuario = usuarios.buscarPorId(usuarioAutenticadoId)
                .orElseThrow(ConsultarMinhaContaUseCase::sessaoInvalida);
        return new DadosMinhaConta(usuario.getId(), usuario.getPessoa().getNome(),
                usuario.getPessoa().getEmail(), usuario.getPerfis());
    }

    private static TokenInvalidoException sessaoInvalida() {
        return new TokenInvalidoException("Sessão inválida. Entre novamente.", null);
    }
}
