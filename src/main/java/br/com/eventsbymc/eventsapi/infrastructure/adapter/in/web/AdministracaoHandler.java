package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.port.in.OperacoesAdministracao;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.*;
import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class AdministracaoHandler implements HttpHandler {
    public enum Acao { RESUMO, USUARIOS, EVENTOS, AUDITORIA, EXPORTAR, PERFIS, BLOQUEAR, DESBLOQUEAR }
    public record EntradaPerfis(Set<Perfil> perfis) {}
    public record EntradaBloqueio(String motivo) {}
    private final OperacoesAdministracao caso;
    private final ObjectMapper mapper;
    private final Acao acao;
    public AdministracaoHandler(OperacoesAdministracao caso, ObjectMapper mapper, Acao acao) {
        this.caso = caso; this.mapper = mapper; this.acao = acao;
    }
    public static void registrar(Router router, OperacoesAdministracao caso, ObjectMapper mapper) {
        String[] metodos = {"GET", "GET", "GET", "GET", "GET", "PUT", "POST", "DELETE"};
        String[] caminhos = {"/admin/resumo", "/admin/usuarios", "/admin/eventos", "/admin/auditoria", "/admin/auditoria.csv",
            "/admin/usuarios/{id}/perfis", "/admin/eventos/{id}/bloqueio", "/admin/eventos/{id}/bloqueio"};
        for (int i = 0; i < caminhos.length; i++) router.registrar(metodos[i], caminhos[i], new AdministracaoHandler(caso, mapper, Acao.values()[i]));
    }
    private UUID recurso(HttpExchange e) {
        String valor = e.getRequestURI().getPath().split("/", -1)[3];
        if (!valor.matches("(?i)[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))
            throw new IllegalArgumentException("Identificador UUID invalido.");
        return UUID.fromString(valor);
    }
    @Override @RequerPerfil({Perfil.ADMINISTRADOR})
    public void handle(HttpExchange e) throws IOException {
        UUID id = ContextoAutenticacao.obter().orElseThrow(() -> new TokenInvalidoException("Sessao invalida.", null)).usuarioId();
        e.getResponseHeaders().set("Cache-Control", "no-store");
        if (acao == Acao.EXPORTAR) { exportar(e, id); return; }
        Object resultado = switch (acao) {
            case RESUMO -> caso.resumo(id);
            case USUARIOS -> caso.usuarios(id);
            case EVENTOS -> caso.eventos(id);
            case AUDITORIA -> caso.auditoria(id);
            case PERFIS -> caso.alterarPerfis(id, recurso(e), HttpRespostas.lerJson(e, EntradaPerfis.class, mapper).perfis());
            case BLOQUEAR -> caso.bloquear(id, recurso(e), HttpRespostas.lerJson(e, EntradaBloqueio.class, mapper).motivo());
            case DESBLOQUEAR -> caso.desbloquear(id, recurso(e));
            default -> throw new IllegalStateException("Acao invalida.");
        };
        HttpRespostas.enviarJson(e, 200, resultado, mapper);
    }
    private static String csv(String valor) {
        String texto = valor == null ? "" : valor;
        String inicio = texto.stripLeading();
        if (!inicio.isEmpty() && "=+-@".indexOf(inicio.charAt(0)) >= 0) texto = "'" + texto;
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
    private void exportar(HttpExchange e, UUID id) throws IOException {
        var texto = new StringBuilder("\uFEFFid;data;responsavel_id;acao;recurso_id;detalhe\r\n");
        for (var r : caso.auditoria(id)) texto.append(csv(r.id().toString())).append(';').append(csv(r.registradoEm().toString()))
            .append(';').append(csv(r.responsavelId().toString())).append(';').append(csv(r.acao()))
            .append(';').append(csv(r.recursoId().toString())).append(';').append(csv(r.detalhe())).append("\r\n");
        byte[] bytes = texto.toString().getBytes(StandardCharsets.UTF_8);
        e.getResponseHeaders().set("Content-Type", "text/csv; charset=utf-8");
        e.getResponseHeaders().set("Content-Disposition", "attachment; filename=auditoria.csv");
        e.sendResponseHeaders(200, bytes.length);
        try (var out = e.getResponseBody()) { out.write(bytes); }
    }
}
