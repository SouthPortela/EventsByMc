package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Evento;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;

final class AcessoEvento {
    private AcessoEvento() {}
    static void exigirDisponivel(Evento evento) {
        try { evento.exigirNaoBloqueado(); }
        catch (IllegalStateException e) { throw new ConflitoOperacaoException(e.getMessage()); }
    }
}
