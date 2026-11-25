package br.com.eventsbymc.eventsapi.domain.model;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
/** Intervalo semiaberto: uma atividade pode começar no instante em que outra termina. */
public record OcupacaoEspaco(UUID espacoId, String nome, LocalDateTime inicio, LocalDateTime fim) {
    public boolean conflitaCom(OcupacaoEspaco outra) {
        if (inicio == null || fim == null || outra.inicio == null || outra.fim == null) return false;
        boolean mesmoEspaco = espacoId != null && espacoId.equals(outra.espacoId)
                || nome != null && outra.nome != null && normalizar(nome).equals(normalizar(outra.nome));
        return mesmoEspaco && inicio.isBefore(outra.fim) && outra.inicio.isBefore(fim);
    }
    private static String normalizar(String nome) { return nome.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT); }
}
