package br.com.eventsbymc.eventsapi.application.port.out;

public interface EmailCertificado {
    void enviar(String destinatario, String assunto, String nomeArquivo, byte[] pdf);
}
