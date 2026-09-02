package br.com.eventsbymc.eventsapi.application.usecase;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;

public class VerificacaoCredencialUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodePass codePass;

    public VerificacaoCredencialUseCase(UsuarioRepository usuarioRepository, CodePass codePass) {
        this.usuarioRepository = usuarioRepository;
        this.codePass = codePass;
    }

    public boolean verificarCredenciais(String email, String senha) {
        // Aqui você pode adicionar validações
        var usuario = usuarioRepository.buscarPorEmail(email);
        if (usuario.isEmpty()) {
            return false; // Usuário não encontrado
        }
        // Verifica se a senha fornecida corresponde ao hash armazenado
        return codePass.matches(senha, usuario.get().getSenhaHash());
    }
}