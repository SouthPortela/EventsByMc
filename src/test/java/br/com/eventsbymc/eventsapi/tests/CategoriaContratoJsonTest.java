package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.usecase.DadosNovoEvento;
import br.com.eventsbymc.eventsapi.domain.model.CategoriaEvento;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.GestaoEventosHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CategoriaContratoJsonTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test void leCategoriaNaCriacaoENaAtualizacao() throws Exception {
        String criacao = """
                {"titulo":"Simpósio","descricao":"Descrição suficiente para o evento.",
                 "local":"Auditório","dataInicio":"2026-10-10T09:00",
                 "dataFim":"2026-10-10T18:00","categoria":"ACADEMICO"}
                """;
        assertEquals(CategoriaEvento.ACADEMICO,
                mapper.readValue(criacao, DadosNovoEvento.class).categoria());
        assertEquals(CategoriaEvento.TECNOLOGIA,
                mapper.readValue("{\"categoria\":\"TECNOLOGIA\"}",
                        GestaoEventosHandler.DadosCategoria.class).categoria());
        assertNull(mapper.readValue(criacao.replace(",\"categoria\":\"ACADEMICO\"", ""),
                DadosNovoEvento.class).categoria());
        assertThrows(com.fasterxml.jackson.core.JsonProcessingException.class,
                () -> mapper.readValue("{\"categoria\":\"INVALIDA\"}",
                        GestaoEventosHandler.DadosCategoria.class));
    }
}
