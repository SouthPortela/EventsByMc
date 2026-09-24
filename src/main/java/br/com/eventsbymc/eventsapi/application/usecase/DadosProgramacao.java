package br.com.eventsbymc.eventsapi.application.usecase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public final class DadosProgramacao {
    private DadosProgramacao() {}

    public record Politica(boolean inscricoesAbertas, Instant inscricoesInicio,
                           Instant inscricoesFim, Integer limiteInscritos,
                           boolean permitirCancelamento, int frequenciaMinimaPercentual) {}
    public record Trilha(UUID id, String nome) {}
    public record Espaco(UUID id, String nome, Integer capacidade) {}
    public record Pessoa(UUID id, String nome, String email) {}
    public record Papel(UUID pessoaId, String nome, String papel) {}
    public record NovaTrilha(String nome) {}
    public record NovoEspaco(String nome, Integer capacidade) {}
    public record NovaPessoa(String nome, String email) {}
    public record NovoPapel(UUID pessoaId, String papel) {}
    public record NovaAtividade(String titulo, String descricao, LocalDateTime dataInicio,
                                LocalDateTime dataFim, String local, Integer capacidade,
                                UUID trilhaId, UUID espacoId, String tipo,
                                Boolean presencaObrigatoria, String politicaFrequencia,
                                Integer permanenciaMinimaPercentual) {}
    public record Filtros(UUID trilhaId, UUID espacoId, String tipo,
                          LocalDateTime de, LocalDateTime ate) {}
    public record Atividade(UUID id, UUID eventoId, String titulo, String descricao,
                            LocalDateTime dataInicio, LocalDateTime dataFim, String local,
                            Integer capacidade, long reservas, UUID trilhaId, String trilha,
                            UUID espacoId, String espaco, String tipo,
                            boolean presencaObrigatoria, String politicaFrequencia,
                            int permanenciaMinimaPercentual, List<Papel> pessoas) {
        public Atividade { pessoas = List.copyOf(pessoas); }
    }
    public record AgendaItem(Atividade atividade, Instant adicionadaEm) {}
}
