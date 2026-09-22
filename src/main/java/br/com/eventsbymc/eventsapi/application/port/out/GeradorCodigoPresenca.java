package br.com.eventsbymc.eventsapi.application.port.out;
public interface GeradorCodigoPresenca {
    String gerar();
    String hash(String codigo);
}
