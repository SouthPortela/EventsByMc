package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;
import java.util.Set;

public class AutorizacaoInterceptor implements HandlerInterceptor {
//Handler Interceptor é uma interface do Spring que permite interceptar requisições HTTP antes que elas cheguem ao controlador, é útil para implementar lógica de autorização, autenticação, loggin etc...
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        //se o handler não for um controlador, retorna true e não faz absolutamenet nem uma checagem
        RequerPerfil anotacao = handlerMethod.getMethodAnnotation(RequerPerfil.class);
        if (anotacao == null) {
            return true;
        }//se o controlador não tiver a anotação RequerPerfil, retorna true e não faz absolutamente nenhuma checagem

        Optional<TokenClaims> claimsOptional = ContextoAutenticacao.obter(request);
        if (claimsOptional.isEmpty()) {
            throw new AcessoNegadoException();
        }
        //se não houver claims na requisição, lança exceção de acesso negado
        //claim é literalmente "afirmação", é um pedaço de informação sobre o usuário que está autenticado como seu ID, email, perfis etc
        //é padrão da JWT

        Set<Perfil> perfisExigidos = Set.of(anotacao.value());
        Set<Perfil> perfisDoUsuario = claimsOptional.get().perfis();

        boolean temPermissao = perfisDoUsuario.stream().anyMatch(perfisExigidos::contains);
        if (!temPermissao) {
            throw new AcessoNegadoException();
        }
        //pra cada perfil que o usuário tem, pergunta esse perfil está na lista de perfis exigidos pela anotação?um bate e retorna true
        return true;
    }
}