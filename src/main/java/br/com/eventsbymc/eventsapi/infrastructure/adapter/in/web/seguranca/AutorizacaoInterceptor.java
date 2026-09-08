package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Optional;
import java.util.Set;

/**
 * Equivalente ao antigo HandlerInterceptor do Spring MVC: antes do Router chamar o handler
 * de verdade, olha se o método handle(HttpExchange) da classe concreta do handler tem
 * @RequerPerfil e, se tiver, confere se o usuário autenticado (guardado pelo
 * AutenticacaoFiltro) tem um dos perfis exigidos.
 */
public class AutorizacaoInterceptor {

    public void verificar(HttpHandler handler, HttpExchange exchange) {
        RequerPerfil anotacao = buscarAnotacao(handler);
        if (anotacao == null) {
            return;
        }

        Optional<TokenClaims> claimsOptional = ContextoAutenticacao.obter();
        if (claimsOptional.isEmpty()) {
            throw new AcessoNegadoException();
        }

        Set<Perfil> perfisExigidos = Set.of(anotacao.value());
        Set<Perfil> perfisDoUsuario = claimsOptional.get().perfis();
        if (perfisDoUsuario.stream().noneMatch(perfisExigidos::contains)) {
            throw new AcessoNegadoException();
        }
    }

    private RequerPerfil buscarAnotacao(HttpHandler handler) {
        try {
            return handler.getClass().getMethod("handle", HttpExchange.class).getAnnotation(RequerPerfil.class);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
