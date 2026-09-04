package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import java.time.Instant;

public record ErroRespostaDTO(String mensagem, Instant instante) {
    public static ErroRespostaDTO criar(String mensagem) {
        return new ErroRespostaDTO(mensagem, Instant.now());
    }
}
//Aqui basicamente montamos uma padronização para as mensagens de erro em casos necessários