package br.com.eventsbymc.eventsapi.application.usecase;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class DadosAvaliacao {
    private DadosAvaliacao() {}
    public record Questao(UUID id, int ordem, String enunciado, String tipo, List<String> opcoes,
                          Integer escalaMinima, Integer escalaMaxima, boolean obrigatoria) {}
    public record Questionario(UUID id, UUID eventoId, UUID atividadeId, String titulo, boolean ativo,
                               List<Questao> questoes) {
        public Questionario(UUID id, UUID eventoId, String titulo, boolean ativo, List<Questao> questoes) {
            this(id, eventoId, null, titulo, ativo, questoes);
        }
    }
    public record NovaQuestao(String enunciado, String tipo, List<String> opcoes,
                              Integer escalaMinima, Integer escalaMaxima, Boolean obrigatoria) {}
    public record Resposta(UUID questaoId, String valor) {}
    public record Envio(List<Resposta> respostas) {}
    public record ResultadoQuestao(UUID questaoId, String enunciado, String tipo,
                                   Map<String, Long> distribuicao, List<String> textos) {}
    public record Resultados(long totalRespostas, List<ResultadoQuestao> questoes) {}
}
