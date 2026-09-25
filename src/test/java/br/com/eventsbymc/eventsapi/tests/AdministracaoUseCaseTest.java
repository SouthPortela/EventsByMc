package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.exception.AcessoNegadoException;
import br.com.eventsbymc.eventsapi.application.exception.ConflitoOperacaoException;
import br.com.eventsbymc.eventsapi.application.usecase.AdministracaoUseCase;
import br.com.eventsbymc.eventsapi.application.usecase.DadosNovoEvento;
import br.com.eventsbymc.eventsapi.application.usecase.EventosUseCase;
import br.com.eventsbymc.eventsapi.domain.model.EstadoEvento;
import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AdministracaoUseCaseTest {
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();

    private DadosNovoEvento dados() {
        var inicio = LocalDateTime.of(2026, 10, 10, 9, 0);
        return new DadosNovoEvento("Simpósio de teste", "Descrição suficientemente longa.",
                "Auditório", inicio, inicio.plusHours(2));
    }

    @Test void somenteAdminVeDadosGlobaisEPerfisSaoRevalidados() {
        var dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        var admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var caso = new AdministracaoUseCase(eventos, usuarios);
        var criado = new EventosUseCase(eventos, usuarios).criar(dono.getId(), dados());
        assertEquals(criado.id(), caso.listarEventos(admin.getId()).getFirst().evento().id());
        assertEquals(criado.id(), caso.consultarEvento(admin.getId(), criado.id()).evento().id());
        assertTrue(caso.listarUsuarios(admin.getId()).stream().allMatch(u -> u.emailMascarado().contains("***")));
        assertThrows(AcessoNegadoException.class, () -> caso.listarEventos(dono.getId()));
        assertThrows(AcessoNegadoException.class,
                () -> caso.suspender(dono.getId(), criado.id(), "Tentativa indevida."));
        assertThrows(AcessoNegadoException.class, () -> caso.listarUsuarios(participante.getId()));
        admin.adicionarPerfil(Perfil.PARTICIPANTE);
        admin.removerPerfil(Perfil.ADMINISTRADOR);
        assertThrows(AcessoNegadoException.class, () -> caso.listarEventos(admin.getId()));
    }

    @Test void suspensaoRestauracaoEExclusaoSaoAuditadasEExclusaoEhTerminal() {
        var dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        var admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
        var gestao = new EventosUseCase(eventos, usuarios);
        var caso = new AdministracaoUseCase(eventos, usuarios);
        var criado = gestao.criar(dono.getId(), dados());
        gestao.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO);
        assertThrows(IllegalArgumentException.class, () -> caso.suspender(admin.getId(), criado.id(), "curto"));
        assertEquals(EstadoEvento.SUSPENSO, caso.suspender(admin.getId(), criado.id(), "Conteúdo sob análise.").evento().estado());
        assertTrue(gestao.listarPublicados().isEmpty());
        assertThrows(ConflitoOperacaoException.class,
                () -> gestao.alterarEstado(dono.getId(), criado.id(), EstadoEvento.ENCERRADO));
        assertEquals(EstadoEvento.RASCUNHO, caso.restaurar(admin.getId(), criado.id(), "Revisão concluída.").evento().estado());
        assertEquals(EstadoEvento.EXCLUIDO, caso.excluir(admin.getId(), criado.id(), "Conteúdo impróprio.").evento().estado());
        assertEquals("[Evento removido]", caso.consultarEvento(admin.getId(), criado.id()).evento().titulo());
        assertTrue(gestao.listarDoOrganizador(dono.getId()).isEmpty());
        assertEquals(3, caso.listarModeracoes(admin.getId()).size());
        assertThrows(ConflitoOperacaoException.class,
                () -> caso.restaurar(admin.getId(), criado.id(), "Tentativa indevida."));
    }

    @Test void conflitoConcorrenteNaoRegistraAcao() {
        var dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        var admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
        var criado = new EventosUseCase(eventos, usuarios).criar(dono.getId(), dados());
        var caso = new AdministracaoUseCase(eventos, usuarios);
        eventos.conflito = true;
        assertThrows(ConflitoOperacaoException.class,
                () -> caso.excluir(admin.getId(), criado.id(), "Conteúdo impróprio."));
        assertTrue(caso.listarModeracoes(admin.getId()).isEmpty());
    }
}
