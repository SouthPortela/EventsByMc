package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    //define que a senha pode ser buscada por ID ou email, e que o retorno é um Optional, que pode ou não conter um usuário.
    //Optional é uma classe que representa um valor que pode estar presente ou ausente, evitando o uso de null.
    Optional<Usuario> buscarPorEmail(String email);
    Optional<Usuario> buscarPorId(UUID id);
    List<Usuario> listar();
    void removerPorId(UUID id);
}