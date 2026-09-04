package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

public class AutenticacaoFiltro implements Filter {
    //filter é uma interface do java que permite interceptar requisições e respostas HTTP em um servidor java.
    private static final Set<String> ROTAS_PUBLICAS = Set.of("/usuarios", "/auth/login");
    private final TokenProvider tokenProvider;

    public AutenticacaoFiltro(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (ROTAS_PUBLICAS.contains(httpRequest.getRequestURI())) {
            chain.doFilter(request, response);
            return;
            //se a rota for pública, o filtro não faz nada e deixa a requisição passar para o próximo filtro ou para o recurso solicitado.
        }
        String cabecalhoAutorizacao = httpRequest.getHeader("Authorization");
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticação ausente ou inválido.");
            return;
            //sistema não pode exigir tokem, pois é nesta rota que faremos a autenticação
        }
        String token = cabecalhoAutorizacao.substring("Bearer ".length());
        //por padrão bearer é utilizado como prefixo para tokens de autenticação.
        //aqui ele é cortado sobrando só o token puro
        try {
            TokenClaims claims = tokenProvider.validarToken(token);
            ContextoAutenticacao.definir(httpRequest, claims);
            //aqui as claims são validadas e guardadas na requisição, pois o filter roda antes do controller.
            chain.doFilter(request, response);
        } catch (RuntimeException exception) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticação inválido.");
        }
    }
}
