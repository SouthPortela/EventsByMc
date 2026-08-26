package br.com.eventsbymc.eventsapi.application.port.out;

import br.com.eventsbymc.eventsapi.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Usuario salvar(Usuario usuario);
    Optional<Usuario> buscarPorId(UUID id);
    List<Usuario> listar();
    void removerPorId(UUID id);
}
