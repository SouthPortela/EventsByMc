package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** PDF textual de uma página por bloco, sem dependência externa. */
public final class PdfSimples {
    private static final Charset TEXTO = Charset.forName("windows-1252");
    private PdfSimples() {}

    public static byte[] gerar(String titulo, List<String> linhas) {
        List<List<String>> paginas = new ArrayList<>();
        List<String> pagina = new ArrayList<>();
        pagina.add(titulo);
        for (String linha : linhas) {
            for (String trecho : quebrar(linha)) {
                if (pagina.size() == 43) { paginas.add(pagina); pagina = new ArrayList<>(); pagina.add(titulo + " (continuação)"); }
                pagina.add(trecho);
            }
        }
        paginas.add(pagina);
        int catalogo = 1, paginasId = 2, fonte = 3;
        int quantidadeObjetos = 3 + paginas.size() * 2;
        byte[][] objetos = new byte[quantidadeObjetos + 1][];
        objetos[catalogo] = ascii("<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder filhos = new StringBuilder();
        for (int i = 0; i < paginas.size(); i++) filhos.append(4 + i * 2).append(" 0 R ");
        objetos[paginasId] = ascii("<< /Type /Pages /Kids [" + filhos + "] /Count " + paginas.size() + " >>");
        objetos[fonte] = ascii("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>");
        for (int i = 0; i < paginas.size(); i++) {
            int paginaId = 4 + i * 2, conteudoId = paginaId + 1;
            objetos[paginaId] = ascii("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R >> >> /Contents " + conteudoId + " 0 R >>");
            byte[] conteudo = conteudo(paginas.get(i));
            byte[] cabecalho = ascii("<< /Length " + conteudo.length + " >>\nstream\n");
            byte[] finalStream = ascii("\nendstream");
            ByteArrayOutputStream objeto = new ByteArrayOutputStream();
            objeto.writeBytes(cabecalho); objeto.writeBytes(conteudo); objeto.writeBytes(finalStream);
            objetos[conteudoId] = objeto.toByteArray();
        }
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        saida.writeBytes(ascii("%PDF-1.4\n"));
        int[] offsets = new int[quantidadeObjetos + 1];
        for (int id = 1; id <= quantidadeObjetos; id++) {
            offsets[id] = saida.size();
            saida.writeBytes(ascii(id + " 0 obj\n"));
            saida.writeBytes(objetos[id]);
            saida.writeBytes(ascii("\nendobj\n"));
        }
        int xref = saida.size();
        saida.writeBytes(ascii("xref\n0 " + (quantidadeObjetos + 1) + "\n0000000000 65535 f \n"));
        for (int id = 1; id <= quantidadeObjetos; id++)
            saida.writeBytes(ascii(String.format("%010d 00000 n \n", offsets[id])));
        saida.writeBytes(ascii("trailer\n<< /Size " + (quantidadeObjetos + 1)
                + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n"));
        return saida.toByteArray();
    }

    private static byte[] conteudo(List<String> linhas) {
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        saida.writeBytes(ascii("BT /F1 11 Tf 50 790 Td 14 TL\n"));
        for (String linha : linhas) {
            String valor = linha == null ? "" : linha.replace('\n', ' ').replace('\r', ' ');
            valor = valor.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            saida.writeBytes(ascii("(")); saida.writeBytes(valor.getBytes(TEXTO));
            saida.writeBytes(ascii(") Tj T*\n"));
        }
        saida.writeBytes(ascii("ET"));
        return saida.toByteArray();
    }

    private static List<String> quebrar(String linha) {
        String restante = linha == null ? "" : linha.replace('\n', ' ').replace('\r', ' ').trim();
        var partes = new ArrayList<String>();
        while (restante.length() > 95) {
            int corte = restante.lastIndexOf(' ', 95);
            if (corte < 1) corte = 95;
            partes.add(restante.substring(0, corte));
            restante = restante.substring(corte).trim();
        }
        partes.add(restante);
        return partes;
    }

    private static byte[] ascii(String valor) { return valor.getBytes(StandardCharsets.US_ASCII); }
}
