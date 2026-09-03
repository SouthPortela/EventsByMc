package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.usecase.ResultadoAutenticacao;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;

import java.util.Set;
import java.util.UUID;

public record LoginRespostaDTO(UUID usuarioID, String nome, String email, Set<Perfil> perfis, String token, String tipoToken){

    public static LoginRespostaDTO from(ResultadoAutenticacao resultado) {
        //usado static para criar o DTO a partir do resultado do caso de uso, mantendo a imutabilidade e encapsulamento
        return new LoginRespostaDTO(
                resultado.usuarioId(),
                resultado.nome(),
                resultado.email(),
                resultado.perfis(),
                resultado.token(),
                "Bearer"
        );
    }
//diferente da outra classe vazia que aguarda o preenchimento pelo Jackson, este aqui faz o caminho reverso, pega o resultado do caso de uso e transforma em DTO para enviar para o front-end.
}