package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import static br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.GestaoEventosHandler.Acao.*;

public final class RotasApi {
    private RotasApi() {}

    public static Router adicionarAvaliacoesAtividade(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesAvaliacao caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/atividades/{id}/questionario", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.CONSULTAR))
                .registrar("GET", "/atividades/{id}/questionario/gestao", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.CONSULTAR_GESTAO))
                .registrar("POST", "/atividades/{id}/questionario", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.CRIAR))
                .registrar("POST", "/atividades/{id}/questionario/questoes", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.QUESTAO))
                .registrar("POST", "/atividades/{id}/questionario/publicacao", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.PUBLICAR))
                .registrar("POST", "/atividades/{id}/avaliacoes", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.RESPONDER))
                .registrar("GET", "/atividades/{id}/avaliacoes/me", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.RESPONDEU))
                .registrar("GET", "/atividades/{id}/avaliacoes/resultados", new AvaliacaoAtividadeHandler(caso, mapper, AvaliacaoAtividadeHandler.Acao.RESULTADOS));
    }

    public static Router adicionarFrequencia(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesFrequencia caso, ObjectMapper mapper) {
        var handler = new FrequenciaHandler(caso, mapper);
        return router.registrar("GET", "/atividades/{id}/frequencia", handler)
                .registrar("POST", "/atividades/{id}/frequencia", handler);
    }

    public static Router adicionarCertificados(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesCertificado caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/eventos/{id}/certificados/me.pdf", new CertificadoHandler(caso, mapper, CertificadoHandler.Acao.PARTICIPANTE_PDF))
                .registrar("POST", "/eventos/{id}/certificados/me/envio", new CertificadoHandler(caso, mapper, CertificadoHandler.Acao.PARTICIPANTE_ENVIO))
                .registrar("GET", "/eventos/{id}/certificados/pessoas/{pessoaId}/{papel}/pdf", new CertificadoHandler(caso, mapper, CertificadoHandler.Acao.PESSOA_PDF))
                .registrar("POST", "/eventos/{id}/certificados/pessoas/{pessoaId}/{papel}/envio", new CertificadoHandler(caso, mapper, CertificadoHandler.Acao.PESSOA_ENVIO));
    }

    public static Router adicionarInteracao(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesInteracao caso, ObjectMapper mapper) {
        var handler = new InteracaoHandler(caso, mapper);
        return router.registrar("GET", "/eventos/{id}/mensagens", handler)
                .registrar("POST", "/eventos/{id}/mensagens", handler);
    }

    public static Router adicionarAvaliacoes(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesAvaliacao caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/eventos/{id}/questionario", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.CONSULTAR))
                .registrar("GET", "/eventos/{id}/questionario/gestao", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.CONSULTAR_GESTAO))
                .registrar("POST", "/eventos/{id}/questionario", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.CRIAR))
                .registrar("POST", "/eventos/{id}/questionario/questoes", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.QUESTAO))
                .registrar("POST", "/eventos/{id}/questionario/publicacao", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.PUBLICAR))
                .registrar("POST", "/eventos/{id}/avaliacoes", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.RESPONDER))
                .registrar("GET", "/eventos/{id}/avaliacoes/me", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.RESPONDEU))
                .registrar("GET", "/eventos/{id}/avaliacoes/resultados", new AvaliacaoHandler(caso, mapper, AvaliacaoHandler.Acao.RESULTADOS));
    }

    public static Router adicionarRelatorios(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.ConsultarRelatorios caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/eventos/{id}/relatorios/inscritos", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.INSCRITOS))
                .registrar("GET", "/eventos/{id}/relatorios/frequencia", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.FREQUENCIA))
                .registrar("GET", "/eventos/{id}/relatorios/inscritos.csv", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.INSCRITOS_CSV))
                .registrar("GET", "/eventos/{id}/relatorios/frequencia.csv", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.FREQUENCIA_CSV))
                .registrar("GET", "/eventos/{id}/relatorios/inscritos.pdf", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.INSCRITOS_PDF))
                .registrar("GET", "/eventos/{id}/relatorios/frequencia.pdf", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.FREQUENCIA_PDF))
                .registrar("GET", "/eventos/{id}/frequencia/me", new RelatoriosHandler(caso, mapper, RelatoriosHandler.Acao.MINHA_FREQUENCIA));
    }

    public static Router adicionarProgramacao(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesProgramacao caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/eventos/{id}/programacao", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.PROGRAMACAO))
                .registrar("GET", "/eventos/{id}/programacao/gestao", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.PROGRAMACAO_GESTAO))
                .registrar("GET", "/eventos/{id}/politica", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CONSULTAR_POLITICA))
                .registrar("PUT", "/eventos/{id}/politica", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.ALTERAR_POLITICA))
                .registrar("GET", "/eventos/{id}/trilhas", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.LISTAR_TRILHAS))
                .registrar("POST", "/eventos/{id}/trilhas", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CRIAR_TRILHA))
                .registrar("GET", "/eventos/{id}/espacos", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.LISTAR_ESPACOS))
                .registrar("POST", "/eventos/{id}/espacos", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CRIAR_ESPACO))
                .registrar("GET", "/eventos/{id}/pessoas", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.LISTAR_PESSOAS))
                .registrar("POST", "/eventos/{id}/pessoas", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CRIAR_PESSOA))
                .registrar("POST", "/eventos/{id}/programacao", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CRIAR_ATIVIDADE))
                .registrar("POST", "/eventos/{id}/atividades/{atividadeId}/pessoas", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.VINCULAR_PESSOA))
                .registrar("GET", "/usuarios/me/agenda", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.CONSULTAR_AGENDA))
                .registrar("POST", "/atividades/{id}/agenda", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.ADICIONAR_AGENDA))
                .registrar("DELETE", "/atividades/{id}/agenda", new ProgramacaoHandler(caso, mapper, ProgramacaoHandler.Acao.REMOVER_AGENDA));
    }

    public static Router adicionarPresenca(Router router,
            br.com.eventsbymc.eventsapi.application.port.in.OperacoesPresenca caso, ObjectMapper mapper) {
        return router
                .registrar("GET", "/usuarios/me/participacao", new PresencaHandler(caso, mapper, PresencaHandler.Acao.CONSULTAR_PARTICIPACAO))
                .registrar("POST", "/eventos/{id}/inscricoes", new PresencaHandler(caso, mapper, PresencaHandler.Acao.INSCREVER))
                .registrar("DELETE", "/eventos/{id}/inscricoes/me", new PresencaHandler(caso, mapper, PresencaHandler.Acao.CANCELAR_INSCRICAO))
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
                .registrar("PATCH", "/eventos/{id}/categoria", new GestaoEventosHandler(eventos, mapper, ALTERAR_CATEGORIA))
                .registrar("POST", "/eventos/{id}/publicacao", new GestaoEventosHandler(eventos, mapper, PUBLICAR))
                .registrar("POST", "/eventos/{id}/encerramento", new GestaoEventosHandler(eventos, mapper, ENCERRAR));
    }
}
