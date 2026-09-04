package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Senha;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.exception.EmailJaCadastradoException;

//Maestro que registra pessoa/usuario e envia para o banco de dados
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodePass codePass;
    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository, CodePass codePass) {
        this.usuarioRepository = usuarioRepository;
        this.codePass = codePass;
    }

    public void registrarUsuario(String nome, String email, String senhaUsuario) {
    if (usuarioRepository.buscarPorEmail(email).isPresent()) {
        throw new EmailJaCadastradoException(email);
    }
    if (senhaUsuario == null || senhaUsuario.length() < 8) {
        throw new IllegalArgumentException("Senha deve ter ao menos 8 caracteres.");
    }
    Pessoa pessoa = new Pessoa(nome, email);
    Senha senha = new Senha(codePass.gerarCodePass(senhaUsuario));
    Usuario usuario = new Usuario(pessoa, senha.getHash());
    usuarioRepository.salvar(usuario);
    }
}