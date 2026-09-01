package br.com.eventsbymc.eventsapi.infrastructure.config;

import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcConnectionFactory;
import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcUsuarioRepository;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.BcryptCodePassAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfig {

    @Bean
    public JdbcConnectionFactory jdbcConnectionFactory() {
        return JdbcConnectionFactory.fromEnvironment();
    }

    @Bean
    public UsuarioRepository usuarioRepository(JdbcConnectionFactory jdbcConnectionFactory) {
        return new JdbcUsuarioRepository(jdbcConnectionFactory);
    }

    @Bean
    public CodePass codePass() {
        return new BcryptCodePassAdapter();
    }

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(UsuarioRepository usuarioRepository, CodePass codePass) {
        return new RegistrarUsuarioUseCase(usuarioRepository, codePass);
    }
}
