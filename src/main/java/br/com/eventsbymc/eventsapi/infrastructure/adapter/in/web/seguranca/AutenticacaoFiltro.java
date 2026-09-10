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

//filter é uma interface do java que permite interceptar requisições e respostas HTTP em um servidor java.
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
            //se a rota for pública, o filtro não faz nada e deixa a requisição passar para o próximo filtro ou para o recurso solicitado.
        }
        List<String> cabecalhos = exchange.getRequestHeaders().get("Authorization");
        String cabecalhoAutorizacao = (cabecalhos == null || cabecalhos.isEmpty()) ? null : cabecalhos.get(0);
        if (cabecalhoAutorizacao == null || !cabecalhoAutorizacao.startsWith("Bearer ")) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_UNAUTHORIZED,
                    "Token de autenticação ausente ou inválido.", objectMapper);
            return;
            //sistema não pode exigir tokem, pois é nesta rota que faremos a autenticação
        }
        String token = cabecalhoAutorizacao.substring("Bearer ".length());
        //por padrão bearer é utilizado como prefixo para tokens de autenticação.
        //aqui ele é cortado sobrando só o token puro
        try {
            TokenClaims claims = tokenProvider.validarToken(token);
            ContextoAutenticacao.definir(claims);
            //aqui as claims são validadas e guardadas na thread, pois o filter roda antes do handler.
            chain.doFilter(exchange);
        } catch (RuntimeException exception) {
            HttpRespostas.enviarErro(exchange, HttpURLConnection.HTTP_UNAUTHORIZED,
                    "Token de autenticação inválido.", objectMapper);
        } finally {
            //limpa a claim guardada, senão pode vazar pra próxima requisição que cair na mesma thread do pool.
            ContextoAutenticacao.limpar();
        }
    }
}
