package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JwtTokenProviderAdapter implements TokenProvider {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_PERFIS = "perfis";

    private final SecretKey chave;
    private final long expiracaoEmMinutos;

    public JwtTokenProviderAdapter(String segredo, long expiracaoEmMinutos) {
        if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET deve ter ao menos 32 caracteres.");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoEmMinutos = expiracaoEmMinutos;
    }

    public static JwtTokenProviderAdapter fromEnvironment() {
        String segredo = System.getenv("JWT_SECRET");
        String expiracaoTexto = System.getenv("JWT_EXPIRATION_MINUTES");
        long expiracao = (expiracaoTexto == null || expiracaoTexto.isBlank()) ? 60L : Long.parseLong(expiracaoTexto);
        return new JwtTokenProviderAdapter(segredo, expiracao);
    }

    @Override
    public String gerarToken(UUID usuarioId, String email, Set<Perfil> perfis) {
        Instant agora = Instant.now();
        String perfisConcatenados = perfis.stream().map(Enum::name).collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(usuarioId.toString())
                .claim(CLAIM_EMAIL, email)
                .claim(CLAIM_PERFIS, perfisConcatenados)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plus(expiracaoEmMinutos, ChronoUnit.MINUTES)))
                .signWith(chave)
                .compact();
    }

    @Override
    public TokenClaims validarToken(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(chave).build().parseSignedClaims(token).getPayload();
            UUID usuarioId = UUID.fromString(claims.getSubject());
            String email = claims.get(CLAIM_EMAIL, String.class);
            Set<Perfil> perfis = Stream.of(claims.get(CLAIM_PERFIS, String.class).split(","))
                    .map(Perfil::valueOf)
                    .collect(Collectors.toSet());
            return new TokenClaims(usuarioId, email, perfis);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new TokenInvalidoException("Token inválido ou expirado.", exception);
        }
    }
}
