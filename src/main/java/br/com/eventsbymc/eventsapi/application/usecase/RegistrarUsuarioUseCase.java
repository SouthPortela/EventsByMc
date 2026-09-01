package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Senha;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;

//Maestro que registra pessoa/usuario e envia para o banco de dados
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final CodePass codePass;
    public RegistrarUsuarioUseCase(UsuarioRepository usuarioRepository, CodePass codePass) {
        this.usuarioRepository = usuarioRepository;
        this.codePass = codePass;
    }

    public void registrarUsuario(String nome, String email, String senhaUsuario) {
        // Aqui você pode adicionar validações adicionais, se necessário
        Pessoa pessoa = new Pessoa(nome, email);
        //recebe a senha e transforma em um codepass
        Senha senha = new Senha(codePass.gerarCodePass(senhaUsuario));
        Usuario usuario = new Usuario(pessoa, senha.getHash());
        usuarioRepository.salvar(usuario);

    }
}