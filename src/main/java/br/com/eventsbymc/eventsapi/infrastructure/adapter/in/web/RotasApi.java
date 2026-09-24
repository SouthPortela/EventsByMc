package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import static br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.GestaoEventosHandler.Acao.*;

public final class RotasApi {
    private RotasApi() {}

    public static Router adicionarPresenca(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesPresenca caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/usuarios/me/participacao", new PresencaHandler(caso, mapper, PresencaHandler.Acao.CONSULTAR_PARTICIPACAO))
                .registrar("POST", "/eventos/{id}/inscricoes", new PresencaHandler(caso, mapper, PresencaHandler.Acao.INSCREVER))
                .registrar("GET", "/eventos/{id}/atividades", new PresencaHandler(caso, mapper, PresencaHandler.Acao.LISTAR_ATIVIDADES))
                .registrar("POST", "/eventos/{id}/atividades", new PresencaHandler(caso, mapper, PresencaHandler.Acao.CRIAR_ATIVIDADE))
                .registrar("POST", "/atividades/{id}/chamadas", new PresencaHandler(caso, mapper, PresencaHandler.Acao.GERAR))
                .registrar("POST", "/presencas/confirmacoes", new PresencaHandler(caso, mapper, PresencaHandler.Acao.CONFIRMAR));
    }

    public static Router criar(RegistrarUsuarioUseCase registrar, AutenticarUsuario autenticar,
                               ConsultarMinhaConta conta, OperacoesEvento eventos, ObjectMapper mapper) {
        return new Router()
                .registrar("POST", "/usuarios", new UsuarioHandler(registrar, mapper))
                .registrar("POST", "/auth/login", new AutenticacaoHandler(autenticar, mapper))
                .registrar("GET", "/usuarios/me", new MinhaContaHandler(conta, mapper))
                .registrar("GET", "/eventos", new CatalogoHandler(eventos, mapper, false))
                .registrar("GET", "/eventos/{id}", new CatalogoHandler(eventos, mapper, true))
                .registrar("GET", "/usuarios/me/eventos", new GestaoEventosHandler(eventos, mapper, LISTAR))
                .registrar("POST", "/eventos", new GestaoEventosHandler(eventos, mapper, CRIAR))
                .registrar("POST", "/eventos/{id}/publicacao", new GestaoEventosHandler(eventos, mapper, PUBLICAR))
                .registrar("POST", "/eventos/{id}/encerramento", new GestaoEventosHandler(eventos, mapper, ENCERRAR));
    }
}
