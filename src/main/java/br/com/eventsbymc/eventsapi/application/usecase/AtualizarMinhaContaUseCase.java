package br.com.eventsbymc.eventsapi.application.usecase;
import br.com.eventsbymc.eventsapi.application.port.in.AtualizarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import java.util.Objects;
import java.util.UUID;
public final class AtualizarMinhaContaUseCase implements AtualizarMinhaConta {
    private final UsuarioRepository usuarios;
    public AtualizarMinhaContaUseCase(UsuarioRepository usuarios) { this.usuarios = Objects.requireNonNull(usuarios); }
    public DadosMinhaConta atualizar(UUID id, String nome, String email) {
        if (id == null) throw new TokenInvalidoException("Sessão inválida.", null);
        usuarios.buscarPorId(id).orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null));
        if (nome == null || nome.isBlank() || nome.trim().length() > 150)
            throw new IllegalArgumentException("Informe um nome com até 150 caracteres.");
        if (email == null || email.trim().length() > 255 || !email.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new IllegalArgumentException("Informe um e-mail válido.");
        var pessoa = new Pessoa(nome.trim(), email.trim().toLowerCase(java.util.Locale.ROOT));
        usuarios.atualizarDadosBasicos(id, pessoa);
        return new ConsultarMinhaContaUseCase(usuarios).executar(id);
    }
}
