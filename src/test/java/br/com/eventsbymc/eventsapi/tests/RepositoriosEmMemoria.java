package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.util.*;

final class RepositoriosEmMemoria {
    static final class Usuarios implements UsuarioRepository {
        private final Map<UUID, Usuario> dados = new HashMap<>();
        public Usuario salvar(Usuario usuario) { dados.put(usuario.getId(), usuario); return usuario; }
        public Optional<Usuario> buscarPorId(UUID id) { return Optional.ofNullable(dados.get(id)); }
        public Optional<Usuario> buscarPorEmail(String email) {
            return dados.values().stream().filter(u -> u.getPessoa().getEmail().equals(email)).findFirst();
        }
        public List<Usuario> listar() { return List.copyOf(dados.values()); }
        public void removerPorId(UUID id) { dados.remove(id); }
    }
    static final class Eventos implements EventoRepository {
        private final Map<UUID, Evento> dados = new HashMap<>();
        boolean conflito;
        private Evento copiar(Evento e) {
            return Evento.reconstituir(e.getId(), e.getTitulo(), e.getDescricao(), e.getOrganizador(),
                    e.getInicio(), e.getFim(), e.getLocal(), e.getEstado(), e.getProgramacao().consultarAtividades());
        }
        public Evento salvar(Evento e) { dados.put(e.getId(), copiar(e)); return e; }
        public Optional<Evento> buscarPorId(UUID id) { return Optional.ofNullable(dados.get(id)).map(this::copiar); }
        public List<Evento> listar() { return dados.values().stream().map(this::copiar).toList(); }
        public void removerPorId(UUID id) { dados.remove(id); }
        public synchronized boolean alterarEstado(UUID id, EstadoEvento esperado, EstadoEvento destino) {
            Evento e = dados.get(id);
            if (conflito || e == null || e.getEstado() != esperado) return false;
            if (destino == EstadoEvento.PUBLICADO) e.publicar(); else e.encerrar();
            return true;
        }
    }
    static Usuario usuario(Usuarios repo, Perfil perfil) {
        Usuario usuario = new Usuario(new Pessoa("Usuário de teste", UUID.randomUUID() + "@example.test"), "hash-apenas-teste");
        usuario.adicionarPerfil(perfil);
        return repo.salvar(usuario);
    }
}
