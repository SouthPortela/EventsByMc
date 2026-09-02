// implementation of the CodePass interface using Bcrypt for password hashing.
package br.com.eventsbymc.eventsapi.infrastructure.adapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;

public class BcryptCodePassAdapter implements CodePass {
    @Override
    public String gerarCodePass(String senha) {
        // Implementação do Bcrypt para gerar o hash da senha
        // Aqui usamos a biblioteca BCrypt para gerar o hash da senha fornecida.
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean matches(String senhaPura, String senhaCriptografada) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return passwordEncoder.matches(senhaPura, senhaCriptografada);
    }
}