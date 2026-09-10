// implementation of the CodePass interface using Bcrypt for password hashing.
package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;

public class BcryptCodePassAdapter implements CodePass {

    private static final int FATOR_DE_CUSTO = 10;

    @Override
    public String gerarCodePass(String senha) {
        // gera o hash da senha com custo 10 (mesmo padrão que o BCryptPasswordEncoder usava)
        return BCrypt.withDefaults().hashToString(FATOR_DE_CUSTO, senha.toCharArray());
    }

    @Override
    public boolean matches(String senhaPura, String senhaCriptografada) {
        return BCrypt.verifyer().verify(senhaPura.toCharArray(), senhaCriptografada).verified;
    }
}
