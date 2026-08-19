package br.com.eventsbymc.eventsapi.domain.model;

import br.com.eventsbymc.eventsapi.domain.model.evento.Evento;

public class Inscricao {

    private final Participante participante;
    private final Evento evento;

    public Inscricao(Participante participante, Evento evento) {

        if (participante == null) {
            throw new IllegalArgumentException(
                    "O participante é obrigatório."
            );
        }

        if (evento == null) {
            throw new IllegalArgumentException(
                    "O evento é obrigatório."
            );
        }

        this.participante = participante;
        this.evento = evento;
    }

    public Participante getParticipante() {
        return participante;
    }

    public Evento getEvento() {
        return evento;
    }
}