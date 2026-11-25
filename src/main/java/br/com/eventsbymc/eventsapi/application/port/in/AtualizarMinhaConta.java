package br.com.eventsbymc.eventsapi.application.port.in;
import java.util.UUID;
import br.com.eventsbymc.eventsapi.application.usecase.DadosMinhaConta;
public interface AtualizarMinhaConta {
    DadosMinhaConta atualizar(UUID usuarioId, String nome, String email);
}
