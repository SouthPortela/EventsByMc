package br.com.eventsbymc.eventsapi.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioTest {
    @Test
    void possuiPerfilParticipanteAoSerCriado() {
        Usuario usuario = new Usuario(new Pessoa("Ana", "ana@example.com"));

        assertTrue(usuario.possuiPerfil(Perfil.PARTICIPANTE));
        assertFalse(usuario.possuiPerfil(Perfil.VISITANTE));
    }

    @Test
    void naoPermiteRemoverPerfilBasico() {
        Usuario usuario = new Usuario(new Pessoa("Ana", "ana@example.com"));

        assertThrows(IllegalArgumentException.class, () -> usuario.removerPerfil(Perfil.PARTICIPANTE));
        assertThrows(IllegalArgumentException.class, () -> usuario.adicionarPerfil(Perfil.VISITANTE));
        assertFalse(usuario.possuiPerfil(Perfil.ORGANIZADOR));
    }
}
