package br.com.eventsbymc.eventsapi.application.port.out;

public interface CodePass {
    String gerarCodePass(String senha);
    boolean matches(String senhaPura, String senhaCriptografada);
}