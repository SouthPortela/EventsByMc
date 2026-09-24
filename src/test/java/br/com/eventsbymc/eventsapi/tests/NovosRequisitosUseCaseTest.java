package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.port.out.AvaliacaoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.CertificadoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.FrequenciaRepository;
import br.com.eventsbymc.eventsapi.application.port.out.InteracaoRepository;
import br.com.eventsbymc.eventsapi.application.port.out.RelatoriosRepository;
import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.PdfSimples;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class NovosRequisitosUseCaseTest {
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    private final Usuario organizador = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
    private final Usuario participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);

    private Evento evento() {
        var inicio = LocalDateTime.of(2026, 10, 10, 9, 0);
        var evento = new Evento("Simpósio", "Descrição", organizador, inicio, inicio.plusHours(4), "Sala");
        eventos.salvar(evento);
        return evento;
    }

    @Test void questionarioValidaTipoEImpedeCriacaoPorParticipante() {
        var evento = evento();
        var repo = new AvaliacaoRepository() {
            public DadosAvaliacao.Questionario consultar(UUID id) { return null; }
            public DadosAvaliacao.Questionario criar(UUID id, String titulo) { return new DadosAvaliacao.Questionario(UUID.randomUUID(), id, titulo, false, List.of()); }
            public DadosAvaliacao.Questao adicionarQuestao(UUID id, DadosAvaliacao.NovaQuestao q) { return new DadosAvaliacao.Questao(UUID.randomUUID(), 1, q.enunciado(), q.tipo(), q.opcoes(), q.escalaMinima(), q.escalaMaxima(), true); }
            public DadosAvaliacao.Questionario publicar(UUID id) { return null; }
            public UUID responder(UUID id, UUID usuario, List<DadosAvaliacao.Resposta> respostas) { return UUID.randomUUID(); }
            public boolean respondeu(UUID id, UUID usuario) { return false; }
            public DadosAvaliacao.Resultados resultados(UUID id) { return new DadosAvaliacao.Resultados(0, List.of()); }
        };
        var caso = new AvaliacoesUseCase(repo, eventos, usuarios);
        assertThrows(AcessoNegadoException.class, () -> caso.criar(participante.getId(), evento.getId(), "Avaliação"));
        assertEquals("Avaliação", caso.criar(organizador.getId(), evento.getId(), " Avaliação ").titulo());
        assertThrows(IllegalArgumentException.class, () -> caso.adicionarQuestao(organizador.getId(), evento.getId(),
                new DadosAvaliacao.NovaQuestao("Nota", "ESCALA", null, 8, 5, true)));
        assertThrows(IllegalArgumentException.class, () -> caso.adicionarQuestao(organizador.getId(), evento.getId(),
                new DadosAvaliacao.NovaQuestao("Opção", "ESCOLHA_UNICA", List.of("Sim", "Sim"), null, null, true)));
        assertEquals("TEXTO", caso.adicionarQuestao(organizador.getId(), evento.getId(),
                new DadosAvaliacao.NovaQuestao("Comentário", "TEXTO", null, null, null, true)).tipo());
    }

    @Test void relatóriosExigemPropriedadeDoEvento() {
        var evento = evento();
        var repo = new RelatoriosRepository() {
            public List<DadosRelatorios.Inscrito> inscritos(UUID id) { return List.of(); }
            public List<DadosRelatorios.Frequencia> frequencias(UUID id) { return List.of(); }
            public DadosRelatorios.Frequencia frequenciaDoParticipante(UUID usuario, UUID id) { return new DadosRelatorios.Frequencia(usuario, "Teste", "teste@example.test", 1, 1, 100, 75, "APROVADO", true); }
        };
        var caso = new RelatoriosUseCase(repo, eventos, usuarios);
        assertThrows(AcessoNegadoException.class, () -> caso.inscritos(participante.getId(), evento.getId()));
        assertEquals(List.of(), caso.inscritos(organizador.getId(), evento.getId()));
    }

    @Test void comunidadeExigeInscricaoParaPublicar() {
        var evento = evento();
        var repo = new InteracaoRepository() {
            public boolean participanteAtivo(UUID id, UUID usuario) { return false; }
            public List<DadosInteracao.Mensagem> listar(UUID id) { return List.of(); }
            public DadosInteracao.Mensagem publicar(UUID id, UUID usuario, String mensagem) { fail("Não deve gravar"); return null; }
        };
        var caso = new InteracaoUseCase(repo, eventos, usuarios);
        assertThrows(AcessoNegadoException.class, () -> caso.publicar(participante.getId(), evento.getId(),
                new DadosInteracao.NovaMensagem("Olá")));
        assertEquals(List.of(), caso.listar(organizador.getId(), evento.getId()));
    }

    @Test void certificadoDependeDeEventoEncerradoEFrequenciaElegivel() {
        var evento = evento();
        evento.publicar(); evento.encerrar(); eventos.salvar(evento);
        var repo = new CertificadoRepository() {
            public DadosCertificado emitirParticipante(UUID id, UUID usuario) { return new DadosCertificado(UUID.randomUUID(), id, "Simpósio", "Teste", "teste@example.test", "PARTICIPANTE", Instant.now(), null); }
            public DadosCertificado emitirPessoa(UUID id, UUID pessoa, String papel) { return null; }
            public void marcarEnviado(UUID id) {}
        };
        var frequencias = new RelatoriosRepository() {
            public List<DadosRelatorios.Inscrito> inscritos(UUID id) { return List.of(); }
            public List<DadosRelatorios.Frequencia> frequencias(UUID id) { return List.of(); }
            public DadosRelatorios.Frequencia frequenciaDoParticipante(UUID usuario, UUID id) { return new DadosRelatorios.Frequencia(usuario, "Teste", "teste@example.test", 1, 0, 0, 75, "INSUFICIENTE", false); }
        };
        var caso = new CertificadosUseCase(repo, frequencias, eventos, usuarios,
                (para, assunto, nome, pdf) -> fail("Não deve enviar"));
        assertThrows(ConflitoOperacaoException.class, () -> caso.participante(participante.getId(), evento.getId()));
    }

    @Test void pdfTemEstruturaValidaECodigoIdentificador() {
        byte[] pdf = PdfSimples.gerar("Certificado", List.of("Código: ABC-123", "Evento: Simpósio", "X".repeat(210)));
        String conteudo = new String(pdf, java.nio.charset.Charset.forName("windows-1252"));
        assertTrue(conteudo.startsWith("%PDF-1.4"));
        assertTrue(conteudo.contains("Código: ABC-123"));
        assertTrue(conteudo.contains("xref"));
        assertTrue(conteudo.contains("X".repeat(20)));
    }

    @Test void somenteDonoOuAdminRegistraFrequenciaManual() {
        var evento = evento();
        UUID atividadeId = UUID.randomUUID();
        var repo = new FrequenciaRepository() {
            public UUID eventoDaAtividade(UUID id) { return evento.getId(); }
            public DadosFrequencia.Registro registrar(UUID id, UUID usuario, UUID responsavel, String marcacao) {
                return new DadosFrequencia.Registro(UUID.randomUUID(), id, usuario, marcacao, Instant.now(), responsavel);
            }
            public List<DadosFrequencia.Registro> listar(UUID id) { return List.of(); }
        };
        var caso = new FrequenciaUseCase(repo, eventos, usuarios);
        var dados = new DadosFrequencia.NovoRegistro(participante.getId(), "ENTRADA");
        assertThrows(AcessoNegadoException.class, () -> caso.registrar(participante.getId(), atividadeId, dados));
        assertThrows(IllegalArgumentException.class, () -> caso.registrar(organizador.getId(), atividadeId,
                new DadosFrequencia.NovoRegistro(participante.getId(), "INVENTADA")));
        assertEquals(organizador.getId(), caso.registrar(organizador.getId(), atividadeId, dados).registradaPor());
    }
}
