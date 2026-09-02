package br.com.eventsbymc.eventsapi.application.usecase;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.UUID;
import java.util.Set;
//aqui vamos montar o "pacotinho" com o resultado, mostrando quem é e qual token recebeu, para que o front-end possa usar esse token para acessar recursos protegidos.

public record ResultadoAutenticacao(UUID usuarioId, String nome, String email, Set<Perfil> perfis, String token) {
}