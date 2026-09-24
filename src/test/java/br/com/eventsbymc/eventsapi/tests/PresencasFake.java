package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.port.out.PresencaRepository;
import br.com.eventsbymc.eventsapi.application.usecase.DadosPresenca;
import br.com.eventsbymc.eventsapi.domain.model.*;
import java.time.Instant;
import java.util.*;

// Dublê de porta para testar aplicação/transporte. Não substitui os testes do SQL.
final class PresencasFake implements PresencaRepository {
    UUID eventoId;
    int tentativas;
    String hashRecebido;
    Atividade atividade;
    UUID consultaUsuarioId;
    DadosPresenca.Participacao participacao = new DadosPresenca.Participacao(List.of(), List.of(), 0);
    public DadosPresenca.Participacao consultarParticipacao(UUID usuarioId) {
        consultaUsuarioId = usuarioId;
        return participacao;
    }
    public DadosPresenca.Inscricao inscrever(UUID usuario, UUID evento) {
        return new DadosPresenca.Inscricao(UUID.randomUUID(), "ATIVA");
    }
    public DadosPresenca.Inscricao cancelarInscricao(UUID usuario, UUID evento) {
        return new DadosPresenca.Inscricao(UUID.randomUUID(), "CANCELADA");
    }
    public List<DadosPresenca.Atividade> listarAtividades(UUID evento) { return List.of(); }
    public void adicionarAtividade(UUID evento, Atividade valor) { atividade = valor; }
    public UUID eventoDaAtividade(UUID id) { return eventoId; }
    public void limitarTentativas(UUID id, String operacao) { tentativas++; }
    public DadosPresenca.Chamada gerarChamada(UUID usuario, UUID id, String hash) {
        hashRecebido = hash;
        return new DadosPresenca.Chamada(UUID.randomUUID(), "Palestra", Instant.now().plusSeconds(300));
    }
    public DadosPresenca.Confirmacao confirmar(UUID usuario, String hash, OrigemPresenca origem) {
        hashRecebido = hash;
        return new DadosPresenca.Confirmacao(UUID.randomUUID(), "Palestra", Instant.now(), false);
    }
}
