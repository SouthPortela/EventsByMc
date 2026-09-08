package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.HttpRespostas;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Set;

/**
 * Equivalente ao antigo AutenticacaoFiltro (jakarta.servlet.Filter), agora sobre
 * com.sun.net.httpserver.Filter: registrado no único contexto raiz "/", roda antes do
 * Router pra toda requisição, exceto as rotas públicas.
 */
public class AutenticacaoFiltro extends Filter {

    private static final Set<String> ROTAS_PUBLICAS = Set.of("/usuarios", "/auth/login");

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper;

    public AutenticacaoFiltro(TokenProvider tokenProvider, ObjectMapper objectMapper) {
        this.tokenProvider = tokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public String description() {
        return "Filtro de autenticação JWT";
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String caminho = exchange.getRequestURI().getPath();
        if (ROTAS_PUBLICAS.contains(caminho)) {
            chain.doFilter(exchange);
            return;
        }

        List<String> cabecalhos = exchange.getRequestHeaders().get("Authorization");
        String cabecalhoAutorizacao = (cabecalhos == null || cabecalhos.isEmpty()) ? null : cabecalhos.get(0);
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_UNAUTHORIZED,
                    "Token de autenticação ausente ou inválido.", objectMapper);
            return;
        }

        String token = cabecalhoAutorizacao.substring("Bearer ".length());
        try {
            TokenClaims claims = tokenProvider.validarToken(token);
            ContextoAutenticacao.definir(claims);
            chain.doFilter(exchange);
        } catch (RuntimeException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_UNAUTHORIZED,
                    "Token de autenticação inválido.", objectMapper);
        } finally {
            // Essencial: sem isso, a claim de um usuário pode vazar pra próxima requisição
            // que reusar essa mesma thread do pool.
            ContextoAutenticacao.limpar();
        }
    }
}
