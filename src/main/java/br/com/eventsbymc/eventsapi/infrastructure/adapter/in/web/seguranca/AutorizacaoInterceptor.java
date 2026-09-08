package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.port.out.TokenClaims;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.Optional;
import java.util.Set;

//antes do Router chamar o handler de verdade, checa se o método handle() dele tem @RequerPerfil
//e, se tiver, confere se o usuário autenticado tem um dos perfis exigidos.
public class AutorizacaoInterceptor {

    public void verificar(HttpHandler handler, HttpExchange exchange) {
        RequerPerfil anotacao = buscarAnotacao(handler);
        if (anotacao == null) {
            return;
        }
        //se o handler não tiver a anotação RequerPerfil, retorna e não faz nenhuma checagem

        Optional<TokenClaims> claimsOptional = ContextoAutenticacao.obter();
        if (claimsOptional.isEmpty()) {
            throw new AcessoNegadoException();
        }
        //se não houver claims na requisição, lança exceção de acesso negado

        Set<Perfil> perfisExigidos = Set.of(anotacao.value());
        Set<Perfil> perfisDoUsuario = claimsOptional.get().perfis();
        if (perfisDoUsuario.stream().noneMatch(perfisExigidos::contains)) {
            throw new AcessoNegadoException();
        }
        //pra cada perfil que o usuário tem, pergunta se esse perfil está na lista exigida pela anotação — um bate e libera
    }

    private RequerPerfil buscarAnotacao(HttpHandler handler) {
        try {
            return handler.getClass().getMethod("handle", HttpExchange.class).getAnnotation(RequerPerfil.class);
        } catch (NoSuchMethodException exception) {
            return null;
        }
    }
}
