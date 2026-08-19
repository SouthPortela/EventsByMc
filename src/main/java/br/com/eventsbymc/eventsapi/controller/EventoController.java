package br.com.eventsbymc.eventsapi.controller;

import br.com.eventsbymc.eventsapi.domain.model.evento.Evento;
import br.com.eventsbymc.eventsapi.domain.model.evento.EventoRequestDTO;
import br.com.eventsbymc.eventsapi.service.EventoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evento")
public class EventoController {

    @Autowired
    private EventoService eventoService;

    @PostMapping
    public ResponseEntity<Evento> create(@RequestBody EventoRequestDTO body) {
        Evento novoEvento = this.eventoService.criarEvento(body);
        return ResponseEntity.ok(novoEvento);
    }
}
