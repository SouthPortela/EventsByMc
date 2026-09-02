package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import br.com.eventsbymc.eventsapi.domain.model.exception.CredenciaisInvalidasException;

import java.util.Optional;

public class AutenticarUsuarioUseCase implements AutenticarUsuario {

    private final UsuarioRepository usuarioRepository;
    private final CodePass codePass;
    private final TokenProvider tokenProvider;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository, CodePass codePass, TokenProvider tokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.codePass = codePass;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public ResultadoAutenticacao autenticar(String email, String senha) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            throw new CredenciaisInvalidasException();
        }
        Optional<Usuario> usuarioOptional = usuarioRepository.buscarPorEmail(email);
        if (usuarioOptional.isEmpty()) {
            throw new CredenciaisInvalidasException();
        }

        Usuario usuario = usuarioOptional.get();
        if (!codePass.matches(senha, usuario.getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        

        String token = tokenProvider.gerarToken(usuario.getId(), usuario.getPessoa().getEmail(), usuario.getPerfis());
        return new ResultadoAutenticacao(usuario.getId(), usuario.getPessoa().getNome(), usuario.getPessoa().getEmail(), usuario.getPerfis(), token);

    }   
}