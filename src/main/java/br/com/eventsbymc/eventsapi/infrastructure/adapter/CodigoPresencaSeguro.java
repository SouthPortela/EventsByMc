package br.com.eventsbymc.eventsapi.infrastructure.adapter;

import br.com.eventsbymc.eventsapi.application.port.out.GeradorCodigoPresenca;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.HexFormat;
import java.util.Locale;

public final class CodigoPresencaSeguro implements GeradorCodigoPresenca {
    private static final String ALFABETO = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private final SecureRandom random = new SecureRandom();
    public String gerar() {
        var codigo = new StringBuilder(12);
        for (int i = 0; i < 12; i++) codigo.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
        return codigo.toString();
    }
    public String hash(String entrada) {
        if (entrada == null || entrada.length() > 32) throw new IllegalArgumentException("Informe um código válido.");
        String codigo = entrada.replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
        if (!codigo.matches("[0-9A-HJKMNP-TV-Z]{12}")) throw new IllegalArgumentException("Informe os 12 caracteres do código.");
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(codigo.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException("SHA-256 indisponível.", e); }
    }
}
