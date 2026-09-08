package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;

/**
 * Implementação da porta CodePass usando BCrypt puro (at.favre.lib:bcrypt), sem Spring
 * Security. Fator de custo 10 — o mesmo valor padrão que o antigo BCryptPasswordEncoder()
 * do Spring usava, então os hashes já gravados no banco (formato $2a$10$...) continuam
 * válidos.
 */
public class BcryptCodePassAdapter implements CodePass {

    private static final int FATOR_DE_CUSTO = 10;

    @Override
    public String gerarCodePass(String senha) {
        return BCrypt.withDefaults().hashToString(FATOR_DE_CUSTO, senha.toCharArray());
    }

    @Override
    public boolean matches(String senhaPura, String senhaCriptografada) {
        return BCrypt.verifyer().verify(senhaPura.toCharArray(), senhaCriptografada).verified;
    }
}
