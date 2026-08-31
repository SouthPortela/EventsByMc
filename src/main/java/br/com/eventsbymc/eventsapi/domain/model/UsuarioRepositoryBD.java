package br.com.eventsbymc.eventsapi.infrastructure.adapter.out.persistence;

import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import org.springframework.stereotype.Component;

@Component 
public class UsuarioRepositoryBD implements UsuarioRepository {

    private final SpringDataUsuarioRepository springDataRepository;

    public UsuarioRepositoryBD(SpringDataUsuarioRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void salvar(Usuario usuario) {
        
        // Transforma o nosso Usuario "puro" em uma Entidade do banco
        UsuarioEntidade entidade = new UsuarioEntidade(
            usuario.getPessoa().getNome(),
            usuario.getPessoa().getEmail(),
            usuario.getPessoa().getSenha()
        );

        // Mandama o Spring Data salvar a entidade no banco usando o método padrão dele
        springDataRepository.save(entidade);
    }
}