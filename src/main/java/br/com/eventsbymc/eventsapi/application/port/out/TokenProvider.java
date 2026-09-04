//passa por uma interface que define a assinatura do método de geração de token,
//  e a implementação concreta é fornecida por uma classe que implementa essa interface, basicamente se decidir trocar biblioteca de geração de token, basta criar uma nova implementação da interface TokenProvider e injetá-la no lugar da implementação atual.
package br.com.eventsbymc.eventsapi.application.port.out;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.Set;
import java.util.UUID;

public interface TokenProvider {
    String gerarToken(UUID usuarioId, String email, Set<Perfil> perfis);
    TokenClaims validarToken(String token);
}