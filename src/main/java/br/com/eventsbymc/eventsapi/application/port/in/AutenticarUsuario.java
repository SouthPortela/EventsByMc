package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.ResultadoAutenticacao;

public interface AutenticarUsuario {
    ResultadoAutenticacao autenticar(String email, String senha);
}
