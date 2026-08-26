package br.com.eventsbymc.eventsapi.adapter.out.jdbc;

import br.com.eventsbymc.eventsapi.application.port.out.EventoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.domain.model.Pessoa;
import br.com.eventsbymc.eventsapi.domain.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_USERNAME", matches = ".+")
@EnabledIfEnvironmentVariable(named = "DB_PASSWORD", matches = ".+")
class JdbcPersistenciaIntegrationTest {

    private final JdbcConnectionFactory connectionFactory =
            JdbcConnectionFactory.fromEnvironment();

    private final UsuarioRepository usuarioRepository =
            new JdbcUsuarioRepository(connectionFactory);

    private final EventoRepository eventoRepository =
            new JdbcEventoRepository(connectionFactory);

    private UUID usuarioId;
    private UUID eventoId;

    @Test
    void devePersistirUsuarioOrganizadorEEvento() {
        String identificador = UUID.randomUUID().toString();

        Usuario usuario = new Usuario(
                new Pessoa(
                        "Usuário de teste " + identificador,
                        "teste-" + identificador + "@example.com"
                ),
                "hash-de-teste"
        );
        usuario.adicionarPerfil(Perfil.ORGANIZADOR);

        usuarioRepository.salvar(usuario);
        usuarioId = usuario.getId();

        Usuario usuarioEncontrado = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow();

        assertEquals(usuario.getPessoa().getNome(), usuarioEncontrado.getPessoa().getNome());
        assertTrue(usuarioEncontrado.possuiPerfil(Perfil.ORGANIZADOR));

        Evento evento = new Evento(
                "Evento de teste " + identificador,
                "Evento criado pelo teste de integração.",
                usuarioEncontrado,
                LocalDateTime.of(2026, 10, 10, 8, 0),
                LocalDateTime.of(2026, 10, 10, 18, 0),
                "Laboratório de testes"
        );

        eventoRepository.salvar(evento);
        eventoId = evento.getId();

        Evento eventoEncontrado = eventoRepository.buscarPorId(eventoId)
                .orElseThrow();

        assertEquals(evento.getTitulo(), eventoEncontrado.getTitulo());
        assertEquals(usuarioId, eventoEncontrado.getOrganizador().getId());
    }

    @AfterEach
    void limparDadosDeTeste() {
        if (eventoId != null) {
            eventoRepository.removerPorId(eventoId);
        }

        if (usuarioId != null) {
            usuarioRepository.removerPorId(usuarioId);
        }
    }
}