package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesProgramacao;
import br.com.eventsbymc.eventsapi.application.usecase.DadosProgramacao;
import br.com.eventsbymc.eventsapi.domain.model.TipoAtividade;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ProgramacaoHandler implements HttpHandler {
    public enum Acao {
        PROGRAMACAO, PROGRAMACAO_GESTAO, CONSULTAR_POLITICA, ALTERAR_POLITICA,
        LISTAR_TRILHAS, CRIAR_TRILHA, LISTAR_ESPACOS, CRIAR_ESPACO,
        LISTAR_PESSOAS, CRIAR_PESSOA, VINCULAR_PESSOA, CRIAR_ATIVIDADE,
        CONSULTAR_AGENDA, ADICIONAR_AGENDA, REMOVER_AGENDA
    }
    private final OperacoesProgramacao caso;
    private final ObjectMapper mapper;
    private final Acao acao;

    public ProgramacaoHandler(OperacoesProgramacao caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso;
        this.mapper = mapper;
        this.acao = acao;
    }

    private static UUID usuario() {
        return ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException("Sessão inválida.", null)).usuarioId();
    }

    private static UUID atividadeVinculada(HttpExchange exchange) {
        String[] partes = exchange.getRequestURI().getPath().split("/", -1);
        if (partes.length < 5) throw new IllegalArgumentException("Atividade inválida.");
        try { return UUID.fromString(partes[4]); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Atividade inválida."); }
    }

    private static Map<String, String> parametros(HttpExchange exchange) {
        var parametros = new HashMap<String, String>();
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null || query.isBlank()) return parametros;
        for (String par : query.split("&")) {
            String[] partes = par.split("=", 2);
            String nome = URLDecoder.decode(partes[0], StandardCharsets.UTF_8);
            String valor = partes.length == 2 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "";
            parametros.put(nome, valor);
        }
        return parametros;
    }

    private static DadosProgramacao.Filtros filtros(HttpExchange exchange) {
        var p = parametros(exchange);
        String tipo = p.get("tipo");
        if (tipo != null && !tipo.isBlank()) tipo = TipoAtividade.valueOf(tipo).name();
        else tipo = null;
        try {
            return new DadosProgramacao.Filtros(
                    p.containsKey("trilhaId") && !p.get("trilhaId").isBlank() ? UUID.fromString(p.get("trilhaId")) : null,
                    p.containsKey("espacoId") && !p.get("espacoId").isBlank() ? UUID.fromString(p.get("espacoId")) : null,
                    tipo,
                    p.containsKey("de") && !p.get("de").isBlank() ? LocalDateTime.parse(p.get("de")) : null,
                    p.containsKey("ate") && !p.get("ate").isBlank() ? LocalDateTime.parse(p.get("ate")) : null);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Data de filtro inválida.");
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        UUID eventoId = switch (acao) {
            case CONSULTAR_AGENDA, ADICIONAR_AGENDA, REMOVER_AGENDA -> null;
            default -> Router.uuidEvento(exchange);
        };
        Object resposta = switch (acao) {
            case PROGRAMACAO -> caso.listarAtividades(eventoId, filtros(exchange));
            case PROGRAMACAO_GESTAO -> caso.listarAtividadesDoOrganizador(usuario(), eventoId);
            case CONSULTAR_POLITICA -> caso.consultarPolitica(usuario(), eventoId);
            case ALTERAR_POLITICA -> caso.atualizarPolitica(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosProgramacao.Politica.class, mapper));
            case LISTAR_TRILHAS -> caso.listarTrilhas(usuario(), eventoId);
            case CRIAR_TRILHA -> caso.criarTrilha(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosProgramacao.NovaTrilha.class, mapper));
            case LISTAR_ESPACOS -> caso.listarEspacos(usuario(), eventoId);
            case CRIAR_ESPACO -> caso.criarEspaco(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosProgramacao.NovoEspaco.class, mapper));
            case LISTAR_PESSOAS -> caso.listarPessoas(usuario(), eventoId);
            case CRIAR_PESSOA -> caso.criarPessoa(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosProgramacao.NovaPessoa.class, mapper));
            case VINCULAR_PESSOA -> {
                caso.vincularPessoa(usuario(), eventoId, atividadeVinculada(exchange),
                        HttpRespostas.lerJson(exchange, DadosProgramacao.NovoPapel.class, mapper));
                yield null;
            }
            case CRIAR_ATIVIDADE -> caso.criarAtividade(usuario(), eventoId,
                    HttpRespostas.lerJson(exchange, DadosProgramacao.NovaAtividade.class, mapper));
            case CONSULTAR_AGENDA -> caso.consultarAgenda(usuario());
            case ADICIONAR_AGENDA -> caso.adicionarAgenda(usuario(), Router.uuidRecurso(exchange));
            case REMOVER_AGENDA -> {
                caso.removerAgenda(usuario(), Router.uuidRecurso(exchange));
                yield null;
            }
        };
        if (acao != Acao.PROGRAMACAO) exchange.getResponseHeaders().set("Cache-Control", "no-store");
        if (resposta == null) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        } else {
            int status = switch (acao) {
                case CRIAR_TRILHA, CRIAR_ESPACO, CRIAR_PESSOA, CRIAR_ATIVIDADE, ADICIONAR_AGENDA -> 201;
                default -> 200;
            };
            HttpRespostas.enviarJson(exchange, status, resposta, mapper);
        }
    }
}
