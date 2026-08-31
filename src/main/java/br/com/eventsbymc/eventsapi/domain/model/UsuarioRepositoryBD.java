package br.com.eventsbymc.eventsapi.infrastructure.adapter.out.persistence;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;

public class UsuarioRepositoryBD implements UsuarioRepository {
    @Override
    public void salvar(Usuario usuario) {
        // Aqui você pode implementar a lógica para salvar o usuário no banco de dados
        // Por exemplo, usando JDBC, JPA, Hibernate, etc.
        // Este é apenas um exemplo simples de como você poderia fazer isso.
        System.out.println("Salvando usuário no banco de dados: " + usuario.getPessoa().getNome());
    }
}