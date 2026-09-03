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
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutenticacaoFiltro;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.AutorizacaoInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// BeansConfig é o nó "ou o coração" do projeto: é o único lugar que conhece
// todas as classes concretas ao mesmo tempo.
// Em arquitetura hexagonal isso tem nome: "composition root"
// Cada caso de uso/controller só pede uma porta (interface) no construtor, sem saber
// qual implementação vai receber; é aqui, e só aqui, que decidimos "quando alguém
// pedir TokenProvider, entregue um JwtTokenProviderAdapter" — e assim por diante.
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

    @Bean
    public TokenProvider tokenProvider() {
        return JwtTokenProviderAdapter.fromEnvironment();
    }

    @Bean
    public AutenticarUsuario autenticarUsuario(UsuarioRepository usuarioRepository, CodePass codePass, TokenProvider tokenProvider) {
        return new AutenticarUsuarioUseCase(usuarioRepository, codePass, tokenProvider);
    }

    @Bean
    public FilterRegistrationBean<AutenticacaoFiltro> autenticacaoFiltroRegistration(TokenProvider tokenProvider) {
        FilterRegistrationBean<AutenticacaoFiltro> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AutenticacaoFiltro(tokenProvider));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public AutorizacaoInterceptor autorizacaoInterceptor() {
        return new AutorizacaoInterceptor();
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer(AutorizacaoInterceptor autorizacaoInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(autorizacaoInterceptor);
            }
        };
    }
}
