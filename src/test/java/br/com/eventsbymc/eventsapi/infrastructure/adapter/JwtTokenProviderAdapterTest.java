package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderAdapterTest {

    private static final String SEGREDO_VALIDO = "0123456789abcdef0123456789abcdef";

    @Test
    void deveGerarEValidarTokenComOsMesmosDados() {
        JwtTokenProviderAdapter provider = new JwtTokenProviderAdapter(SEGREDO_VALIDO, 60);
        UUID usuarioId = UUID.randomUUID();
        Set<Perfil> perfis = Set.of(Perfil.ORGANIZADOR, Perfil.VISITANTE);

        String token = provider.gerarToken(usuarioId, "ana@exemplo.com", perfis);
        TokenClaims claims = provider.validarToken(token);

        assertEquals(usuarioId, claims.usuarioId());
        assertEquals("ana@exemplo.com", claims.email());
        assertEquals(perfis, claims.perfis());
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtTokenProviderAdapter provider = new JwtTokenProviderAdapter(SEGREDO_VALIDO, -1);
        String tokenExpirado = provider.gerarToken(UUID.randomUUID(), "ana@exemplo.com", Set.of(Perfil.VISITANTE));

        assertThrows(TokenInvalidoException.class, () -> provider.validarToken(tokenExpirado));
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        JwtTokenProviderAdapter provider = new JwtTokenProviderAdapter(SEGREDO_VALIDO, 60);

        assertThrows(TokenInvalidoException.class, () -> provider.validarToken("isso.nao.eh.um.token.valido"));
    }

    @Test
    void deveRejeitarSegredoCurtoNoConstrutor() {
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProviderAdapter("segredo-curto", 60));
    }
}
