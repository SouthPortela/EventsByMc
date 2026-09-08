package br.com.eventsbymc.eventsapi.infrastructure.config;

import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcConnectionFactory;
import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcUsuarioRepository;
import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.application.usecase.AutenticarUsuarioUseCase;
import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.BcryptCodePassAdapter;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.JwtTokenProviderAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * "Nó"/coração do projeto — o único lugar que conhece todas as classes concretas ao mesmo
 * tempo. Substitui o antigo BeansConfig (@Configuration do Spring): mesma responsabilidade,
 * sem framework — a "fiação" agora é só construtor Java comum, resolvida em tempo de
 * compilação, não por reflection em tempo de execução.
 */
public final class CompositionRoot {

    public final UsuarioRepository usuarioRepository;
    public final CodePass codePass;
    public final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    public final TokenProvider tokenProvider;
    public final AutenticarUsuario autenticarUsuario;
    public final ObjectMapper objectMapper;

    private CompositionRoot() {
        JdbcConnectionFactory jdbcConnectionFactory = JdbcConnectionFactory.fromEnvironment();
        this.usuarioRepository = new JdbcUsuarioRepository(jdbcConnectionFactory);
        this.codePass = new BcryptCodePassAdapter();
        this.registrarUsuarioUseCase = new RegistrarUsuarioUseCase(usuarioRepository, codePass);
        this.tokenProvider = JwtTokenProviderAdapter.fromEnvironment();
        this.autenticarUsuario = new AutenticarUsuarioUseCase(usuarioRepository, codePass, tokenProvider);

        // Sem Spring Boot não existe mais o auto-config que registrava o JavaTimeModule
        // sozinho: sem isso, serializar ErroRespostaDTO (campo Instant) quebraria.
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static CompositionRoot montar() {
        return new CompositionRoot();
    }
}
