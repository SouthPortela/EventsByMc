package br.com.eventsbymc.eventsapi.infrastructure.seed;

import at.favre.lib.crypto.bcrypt.BCrypt;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.SecureRandom;
import java.sql.*;
import java.time.*;
import java.util.*;

/** Ferramenta explicita de demonstracao. Nunca e chamada pela inicializacao da API. */
public final class DemoDataSeeder {
    public record Conta(UUID id, String nome, String email, String perfil) {}
    public record Evento(UUID id, UUID organizador, String titulo, String descricao, String local,
                         String categoria, LocalDateTime inicio, LocalDateTime fim, String estado,
                         String motivo, String estadoAnterior) {}
    public record Dados(List<Conta> contas, List<Evento> eventos) {
        public Dados { contas = List.copyOf(contas); eventos = List.copyOf(eventos); }
    }
    private static final String[] NOMES = {"Ana", "Bruno", "Carla", "Daniel", "Elisa", "Felipe", "Gabriela", "Henrique", "Isabela", "Joao"};
    private static final String[] SOBRENOMES = {"Almeida", "Barbosa", "Costa", "Dias", "Esteves", "Freitas", "Gomes", "Lima", "Mendes", "Pereira"};
    private static final String[] CATEGORIAS = {"TECNOLOGIA", "CURSOS_E_WORKSHOPS", "NEGOCIOS_E_CARREIRAS", "ACADEMICO", "OUTROS"};
    private static final String[][] TEMAS = {
        {"Encontro de Desenvolvimento Java", "Forum de Seguranca da Informacao", "Jornada de Banco de Dados", "Conferencia de Inteligencia Artificial", "Seminario de Redes e Infraestrutura", "Encontro de Software Livre", "Jornada de Computacao em Nuvem", "Forum de Engenharia de Software", "Encontro de Ciencia de Dados", "Conferencia de Automacao"},
        {"Oficina de Impressao 3D", "Workshop de Programacao Web", "Curso de Fotografia", "Oficina de Comunicacao", "Workshop de Excel", "Curso de Gestao de Projetos", "Oficina de Design", "Workshop de Robotica", "Curso de Ingles Tecnico", "Oficina de Prototipagem"},
        {"Forum de Empreendedorismo", "Encontro de Liderancas", "Jornada de Marketing", "Seminario de Gestao Financeira", "Forum de Carreiras", "Encontro de Pequenos Negocios", "Jornada de Vendas", "Seminario de Inovacao", "Forum de Gestao de Pessoas", "Encontro de Economia Criativa"},
        {"Semana de Iniciacao Cientifica", "Simposio de Educacao", "Jornada de Pesquisa", "Seminario de Extensao", "Encontro de Pos-Graduacao", "Forum de Metodologia Cientifica", "Jornada de Engenharia", "Seminario de Sustentabilidade", "Mostra de Projetos Integradores", "Congresso de Ciencias Aplicadas"},
        {"Encontro Cultural", "Feira de Projetos", "Festival de Conhecimento", "Encontro de Voluntariado", "Jornada de Bem-Estar", "Feira de Artes", "Encontro de Comunidades", "Seminario de Cidadania", "Mostra de Talentos", "Encontro de Integracao"}
    };
    private DemoDataSeeder() {}
    public static UUID uuid(String tipo, int numero) {
        return UUID.nameUUIDFromBytes(("EventsByMc/demo/v1/" + tipo + "/" + numero).getBytes(StandardCharsets.UTF_8));
    }
    public static Dados gerar(LocalDate referencia) {
        var contas = new ArrayList<Conta>();
        for (int i = 1; i <= 100; i++) contas.add(new Conta(uuid("participante", i),
            NOMES[(i - 1) % 10] + " " + SOBRENOMES[(i - 1) / 10] + " (Demo)",
            "demo.participante%03d@example.test".formatted(i), "PARTICIPANTE"));
        for (int i = 1; i <= 20; i++) contas.add(new Conta(uuid("organizador", i),
            "Organizador Demo %02d".formatted(i), "demo.organizador%02d@example.test".formatted(i), "ORGANIZADOR"));
        var eventos = new ArrayList<Evento>();
        for (int i = 1; i <= 50; i++) {
            String estado = i <= 20 ? "PUBLICADO" : i <= 35 ? "RASCUNHO" : i <= 45 ? "ENCERRADO" : "BLOQUEADO";
            int categoria = (i - 1) % 5;
            LocalDate dia = estado.equals("ENCERRADO") ? referencia.minusDays(i - 34) : referencia.plusDays((i - 1) % 15);
            String motivo = estado.equals("BLOQUEADO") ? "Demonstracao: corrigir as informacoes do local e a programacao antes de divulgar o evento." : null;
            eventos.add(new Evento(uuid("evento", i), uuid("organizador", (i - 1) % 20 + 1),
                TEMAS[categoria][(i - 1) / 5] + " (Demo)",
                "Evento ficticio de demonstracao para testar catalogo, administracao, programacao e controle de presenca. Nao corresponde a um evento real.",
                "Centro de Eventos Demo - Sala " + ((i - 1) % 8 + 1), CATEGORIAS[categoria],
                dia.atTime(9, 0), dia.atTime(18, 0), estado, motivo,
                motivo == null ? null : List.of("PUBLICADO", "RASCUNHO", "ENCERRADO").get((i - 46) % 3)));
        }
        return new Dados(contas, eventos);
    }
    public static Map<String, String> lerAmbiente(Path arquivo) throws IOException {
        var resultado = new HashMap<String, String>();
        if (!Files.exists(arquivo)) return resultado;
        for (String linha : Files.readAllLines(arquivo, StandardCharsets.UTF_8)) {
            linha = linha.strip().replaceFirst("^\\uFEFF", "");
            if (linha.isBlank() || linha.startsWith("#")) continue;
            if (linha.startsWith("export ")) linha = linha.substring(7).stripLeading();
            int separador = linha.indexOf('=');
            if (separador <= 0) continue;
            String valor = linha.substring(separador + 1).strip();
            if (valor.length() >= 2 && ((valor.startsWith("\"") && valor.endsWith("\"")) || (valor.startsWith("'") && valor.endsWith("'"))))
                valor = valor.substring(1, valor.length() - 1);
            resultado.put(linha.substring(0, separador).strip(), valor);
        }
        return resultado;
    }
    private static String exigir(Map<String, String> env, String chave) {
        String valor = env.get(chave);
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException("Configure " + chave + " no .env local.");
        return valor;
    }
    private static String senhaDemo(Path raiz) throws IOException {
        Path arquivo = raiz.resolve(".env.demo");
        if (!Files.exists(arquivo)) {
            byte[] aleatorio = new byte[18]; new SecureRandom().nextBytes(aleatorio);
            String senha = Base64.getUrlEncoder().withoutPadding().encodeToString(aleatorio);
            Files.writeString(arquivo, "# Somente contas ficticias do seed. Nao versionar.\nDEMO_PASSWORD=" + senha + "\n", StandardOpenOption.CREATE_NEW);
        }
        return exigir(lerAmbiente(arquivo), "DEMO_PASSWORD");
    }
    private static void conferirSchema(Connection c) throws SQLException {
        try (var s = c.prepareStatement("SELECT 1 FROM versoes_schema WHERE versao = ?")) {
            s.setInt(1, 10);
            try (var r = s.executeQuery()) {
                if (!r.next()) throw new IllegalStateException("Aplique a migracao 10 antes de popular os dados de demonstracao.");
            }
        }
    }
    private static boolean inserirConta(Connection c, Conta conta, String hash) throws SQLException {
        try (var s = c.prepareStatement("SELECT email FROM usuarios WHERE id = ?")) {
            s.setObject(1, conta.id());
            try (var r = s.executeQuery()) {
                if (r.next()) {
                    if (!conta.email().equals(r.getString(1))) throw new IllegalStateException("Um UUID reservado para demonstracao esta ocupado. Nenhum dado foi alterado.");
                    return false;
                }
            }
        }
        try (var s = c.prepareStatement("INSERT INTO usuarios(id, nome, email, senha_hash) VALUES (?, ?, ?, ?)")) {
            s.setObject(1, conta.id()); s.setString(2, conta.nome()); s.setString(3, conta.email()); s.setString(4, hash); s.executeUpdate();
        }
        try (var s = c.prepareStatement("INSERT INTO usuario_perfis(usuario_id, perfil) VALUES (?, ?)")) {
            s.setObject(1, conta.id()); s.setString(2, conta.perfil()); s.executeUpdate();
        }
        return true;
    }
    private static boolean inserirEvento(Connection c, Evento e) throws SQLException {
        String sql = """
            INSERT INTO eventos(id, organizador_id, titulo, descricao, local, categoria, inicio, fim, estado,
                motivo_bloqueio, estado_anterior_bloqueio)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING
            """;
        try (var s = c.prepareStatement(sql)) {
            s.setObject(1, e.id()); s.setObject(2, e.organizador()); s.setString(3, e.titulo());
            s.setString(4, e.descricao()); s.setString(5, e.local()); s.setString(6, e.categoria());
            s.setObject(7, e.inicio()); s.setObject(8, e.fim()); s.setString(9, (e.estado().equals("ENCERRADO") || e.estado().equals("BLOQUEADO")) ? "RASCUNHO" : e.estado());
            s.setString(10, null); s.setString(11, null);
            return s.executeUpdate() == 1;
        }
    }
    private static void inserirAtividades(Connection c, Evento e, int indice) throws SQLException {
        String sql = "INSERT INTO atividades(id, evento_id, titulo, descricao, inicio, fim, local, capacidade) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (var s = c.prepareStatement(sql)) {
            for (int n = 0; n < 2; n++) {
                s.setObject(1, uuid("atividade", indice * 2 + n)); s.setObject(2, e.id());
                s.setString(3, n == 0 ? "Abertura e palestra (Demo)" : "Oficina pratica (Demo)");
                s.setString(4, "Atividade ficticia para demonstracao do sistema.");
                s.setObject(5, e.inicio().plusMinutes(n * 90L)); s.setObject(6, e.inicio().plusMinutes(n * 90L + 60));
                s.setString(7, e.local()); s.setInt(8, 100); s.addBatch();
            }
            s.executeBatch();
        }
    }
    public static int[] popular(Connection c, Dados dados, String senha) throws SQLException {
        if (senha == null || senha.length() < 12) throw new IllegalArgumentException("A senha de demonstracao deve ter ao menos 12 caracteres.");
        String hash = BCrypt.withDefaults().hashToString(10, senha.toCharArray());
        c.setAutoCommit(false);
        try {
            conferirSchema(c);
            try (var s = c.prepareStatement("SELECT pg_advisory_xact_lock(20851102)")) { s.execute(); }
            int contas = 0, eventos = 0;
            for (var conta : dados.contas()) if (inserirConta(c, conta, hash)) contas++;
            for (int i = 0; i < dados.eventos().size(); i++) {
                var evento = dados.eventos().get(i);
                if (inserirEvento(c, evento)) { inserirAtividades(c, evento, i + 1);
                    if (evento.estado().equals("ENCERRADO") || evento.estado().equals("BLOQUEADO")) {
                        try (var fechar = c.prepareStatement("UPDATE eventos SET estado=?,motivo_bloqueio=?,estado_anterior_bloqueio=? WHERE id=?")) {
                            fechar.setString(1, evento.estado()); fechar.setString(2, evento.motivo());
                            fechar.setString(3, evento.estadoAnterior()); fechar.setObject(4, evento.id()); fechar.executeUpdate();
                        }
                    }
                    eventos++; }
            }
            c.commit();
            return new int[] {contas, eventos, eventos * 2};
        } catch (SQLException | RuntimeException e) { c.rollback(); throw e; }
    }
    public static void main(String[] args) {
        try {
            Path raiz = Path.of("").toAbsolutePath();
            var env = lerAmbiente(raiz.resolve(".env"));
            // A porta publicada do Docker e acessada pelo host; nao usar o hostname interno postgres-db.
            String porta = env.getOrDefault("DB_PORT", "5432");
            if (!porta.matches("[0-9]{1,5}")) throw new IllegalArgumentException("DB_PORT invalida.");
            String url = "jdbc:postgresql://127.0.0.1:" + porta + "/" + exigir(env, "DB_NAME");
            Properties propriedades = new Properties();
            propriedades.setProperty("user", exigir(env, "DB_USERNAME"));
            propriedades.setProperty("password", exigir(env, "DB_PASSWORD"));
            propriedades.setProperty("connectTimeout", "5");
            propriedades.setProperty("socketTimeout", "30");
            try (var c = DriverManager.getConnection(url, propriedades)) {
                conferirSchema(c);
                int[] novos = popular(c, gerar(LocalDate.now(ZoneId.of("America/Sao_Paulo"))), senhaDemo(raiz));
                System.out.printf("Seed concluido: %d contas, %d eventos e %d atividades adicionados.%n", novos[0], novos[1], novos[2]);
                System.out.println("Carga completa: 100 participantes, 20 organizadores, 50 eventos e 100 atividades. Senha em .env.demo.");
            }
        } catch (SQLException e) {
            // Nao imprimir mensagens JDBC: podem conter credenciais, emails ou dados do banco.
            System.err.println("Nao foi possivel concluir o seed no PostgreSQL local. SQLState: " + e.getSQLState() + ". Confira Docker, porta e configuracao .env. A transacao nao foi confirmada.");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Nao foi possivel ler/gravar a configuracao local do seed."); System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e instanceof IllegalArgumentException || e instanceof IllegalStateException ? e.getMessage() : "Falha ao preparar o seed.");
            System.exit(1);
        }
    }
}
