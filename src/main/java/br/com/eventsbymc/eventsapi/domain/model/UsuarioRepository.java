package br.com.eventsbymc.eventsapi.application.port.out;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;


public interface UsuarioRepository {
    void salvar(Usuario usuario);
   
}