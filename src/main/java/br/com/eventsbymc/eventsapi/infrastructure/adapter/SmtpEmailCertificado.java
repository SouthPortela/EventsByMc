package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.port.out.EmailCertificado;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Envio SMTP com TLS implícito (porta 465), sem persistir credenciais. */
public final class SmtpEmailCertificado implements EmailCertificado {
    private final String host, usuario, senha, remetente;
    private final int porta;

    public SmtpEmailCertificado(String host, int porta, String usuario, String senha, String remetente) {
        this.host = host; this.porta = porta; this.usuario = usuario;
        this.senha = senha; this.remetente = remetente;
    }
    public static SmtpEmailCertificado fromEnvironment() {
        String host = System.getenv("SMTP_HOST");
        String porta = System.getenv().getOrDefault("SMTP_PORT", "465");
        int numero;
        try { numero = Integer.parseInt(porta); }
        catch (NumberFormatException e) { throw new IllegalStateException("SMTP_PORT inválida."); }
        if (numero < 1 || numero > 65535) throw new IllegalStateException("SMTP_PORT inválida.");
        return new SmtpEmailCertificado(host, numero, System.getenv("SMTP_USERNAME"),
                System.getenv("SMTP_PASSWORD"), System.getenv("SMTP_FROM"));
    }
    private static String endereco(String valor) {
        if (valor == null || !valor.matches("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
            throw new IllegalArgumentException("Endereço de e-mail inválido.");
        return valor;
    }
    private static String b64(String valor) {
        return Base64.getEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }
    private static void comando(BufferedWriter saida, BufferedReader entrada, String valor, int esperado) throws IOException {
        saida.write(valor); saida.write("\r\n"); saida.flush(); resposta(entrada, esperado);
    }
    private static void resposta(BufferedReader entrada, int esperado) throws IOException {
        String linha;
        do {
            linha = entrada.readLine();
            if (linha == null || linha.length() < 3 || !linha.substring(0, 3).matches("\\d{3}"))
                throw new IOException("Resposta SMTP inválida.");
        } while (linha.length() > 3 && linha.charAt(3) == '-');
        if (Integer.parseInt(linha.substring(0, 3)) != esperado)
            throw new IOException("Servidor SMTP recusou a operação.");
    }
    @Override public void enviar(String destinatario, String assunto, String nomeArquivo, byte[] pdf) {
        if (host == null || host.isBlank() || usuario == null || usuario.isBlank()
                || senha == null || senha.isBlank() || remetente == null || remetente.isBlank())
            throw new ConflitoOperacaoException("SMTP não configurado para envio de certificados.");
        String de = endereco(remetente), para = endereco(destinatario);
        if (pdf == null || pdf.length == 0) throw new IllegalArgumentException("PDF vazio.");
        String fronteira = "events-cert-" + java.util.UUID.randomUUID();
        try (SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket()) {
            socket.connect(new java.net.InetSocketAddress(host, porta), 10_000);
            socket.setSoTimeout(20_000);
            var parametros = socket.getSSLParameters();
            parametros.setEndpointIdentificationAlgorithm("HTTPS");
            socket.setSSLParameters(parametros);
            socket.startHandshake();
            var entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
            var saida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
            resposta(entrada, 220);
            comando(saida, entrada, "EHLO localhost", 250);
            comando(saida, entrada, "AUTH LOGIN", 334);
            comando(saida, entrada, b64(usuario), 334);
            comando(saida, entrada, b64(senha), 235);
            comando(saida, entrada, "MAIL FROM:<" + de + ">", 250);
            comando(saida, entrada, "RCPT TO:<" + para + ">", 250);
            comando(saida, entrada, "DATA", 354);
            saida.write("From: " + de + "\r\nTo: " + para + "\r\n");
            saida.write("Subject: =?UTF-8?B?" + b64(assunto) + "?=\r\n");
            saida.write("MIME-Version: 1.0\r\nContent-Type: multipart/mixed; boundary=\"" + fronteira + "\"\r\n\r\n");
            saida.write("--" + fronteira + "\r\nContent-Type: text/plain; charset=UTF-8\r\nContent-Transfer-Encoding: base64\r\n\r\n");
            saida.write(b64("Seu certificado está anexado.")); saida.write("\r\n");
            saida.write("--" + fronteira + "\r\nContent-Type: application/pdf\r\nContent-Disposition: attachment; filename=\"certificado.pdf\"\r\nContent-Transfer-Encoding: base64\r\n\r\n");
            saida.write(Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(pdf));
            saida.write("\r\n--" + fronteira + "--\r\n.\r\n"); saida.flush();
            resposta(entrada, 250);
            comando(saida, entrada, "QUIT", 221);
        } catch (IOException e) { throw new IllegalStateException("Falha ao enviar certificado por SMTP.", e); }
    }
}
