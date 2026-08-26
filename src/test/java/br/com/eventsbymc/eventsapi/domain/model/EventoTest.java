package br.com.eventsbymc.eventsapi.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventoTest {
    private static final LocalDateTime INICIO = LocalDateTime.of(2026, 10, 10, 8, 0);
    private static final LocalDateTime FIM = LocalDateTime.of(2026, 10, 10, 18, 0);

    @Test
    void criaEventoEmRascunhoEAdicionaAtividadeNoPeriodo() {
        Evento evento = new Evento("Semana de Tecnologia", "Conteúdo técnico", organizador(), INICIO, FIM, "Campus");
        Atividade atividade = new Atividade("Palestra", "Abertura", INICIO.plusHours(1), INICIO.plusHours(2), "Auditório", 100);

        evento.adicionarAtividade(atividade);

        assertEquals(EstadoEvento.RASCUNHO, evento.getEstado());
        assertEquals(1, evento.getProgramacao().consultarAtividades().size());
    }

    @Test
    void rejeitaAtividadeForaDoPeriodoDoEvento() {
        Evento evento = new Evento("Semana de Tecnologia", null, organizador(), INICIO, FIM, "Campus");
        Atividade atividade = new Atividade("Palestra", null, INICIO.minusMinutes(1), INICIO.plusHours(1), "Auditório", null);

        assertThrows(IllegalArgumentException.class, () -> evento.adicionarAtividade(atividade));
    }

    @Test
    void somenteOrganizadorPodeCriarEvento() {
        Usuario usuario = new Usuario(new Pessoa("Ana", "ana@example.com"));

        assertThrows(IllegalArgumentException.class, () -> new Evento("Evento", null, usuario));
    }

    private Usuario organizador() {
        Usuario usuario = new Usuario(new Pessoa("Ana", "ana@example.com"));
        usuario.adicionarPerfil(Perfil.ORGANIZADOR);
        return usuario;
    }
}
