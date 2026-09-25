package br.com.eventsbymc.eventsapi.tests;

import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EventosUseCaseTest {
    private final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    private final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    private final Usuario dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
    private final EventosUseCase caso = new EventosUseCase(eventos, usuarios);
    private final LocalDateTime inicio = LocalDateTime.of(2026, 10, 10, 9, 0);
    private DadosNovoEvento dados() {
        return new DadosNovoEvento(" Simpósio ", "Descrição suficientemente longa do evento.",
                " Auditório ", inicio, inicio.plusHours(2));
    }
    @Test void cicloDeVidaEVisibilidadePublica() {
        var criado = caso.criar(dono.getId(), dados());
        assertEquals("Simpósio", criado.titulo());
        assertEquals(EstadoEvento.RASCUNHO, criado.estado());
        assertTrue(caso.listarPublicados().isEmpty());
        assertThrows(RecursoNaoEncontradoException.class, () -> caso.consultarPublicado(criado.id()));
        assertEquals(1, caso.listarDoOrganizador(dono.getId()).size());
        caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO);
        assertEquals(criado.id(), caso.listarPublicados().getFirst().id());
        assertEquals(EstadoEvento.PUBLICADO, caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO).estado());
        caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.ENCERRADO);
        assertTrue(caso.listarPublicados().isEmpty());
        assertThrows(ConflitoOperacaoException.class,
                () -> caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO));
    }
    @Test void protegePropriedadeENaoConfiaSomenteNoPerfilDoToken() {
        var criado = caso.criar(dono.getId(), dados());
        var outro = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        assertTrue(caso.listarDoOrganizador(outro.getId()).isEmpty());
        assertThrows(AcessoNegadoException.class, () -> caso.alterarEstado(outro.getId(), criado.id(), EstadoEvento.PUBLICADO));
        dono.removerPerfil(Perfil.ORGANIZADOR);
        assertThrows(AcessoNegadoException.class, () -> caso.criar(dono.getId(), dados()));
        assertThrows(TokenInvalidoException.class, () -> caso.criar(UUID.randomUUID(), dados()));
    }
    @Test void administradorPodeModerarMasSomenteOrganizadorPodeCriar() {
        var admin = RepositoriosEmMemoria.usuario(usuarios, Perfil.ADMINISTRADOR);
        var participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var criado = caso.criar(dono.getId(), dados());
        assertEquals(EstadoEvento.PUBLICADO, caso.alterarEstado(admin.getId(), criado.id(), EstadoEvento.PUBLICADO).estado());
        assertThrows(AcessoNegadoException.class, () -> caso.criar(admin.getId(), dados()));
        admin.adicionarPerfil(Perfil.ORGANIZADOR);
        assertNotNull(caso.criar(admin.getId(), dados()));
        assertThrows(AcessoNegadoException.class, () -> caso.criar(participante.getId(), dados()));
    }
    @Test void validaCamposPeriodoETransicao() {
        assertThrows(IllegalArgumentException.class, () -> caso.criar(dono.getId(), null));
        assertThrows(IllegalArgumentException.class, () -> caso.criar(dono.getId(),
                new DadosNovoEvento("abc", dados().descricao(), "Local", inicio, inicio.plusHours(1))));
        assertThrows(IllegalArgumentException.class, () -> caso.criar(dono.getId(),
                new DadosNovoEvento("Evento válido", dados().descricao(), "Local", inicio, inicio)));
        var criado = caso.criar(dono.getId(), dados());
        assertThrows(ConflitoOperacaoException.class, () -> caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.ENCERRADO));
        assertThrows(IllegalArgumentException.class, () -> caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.RASCUNHO));
        assertThrows(RecursoNaoEncontradoException.class, () -> caso.consultarPublicado(UUID.randomUUID()));
    }
    @Test void conflitoConcorrenteNaoSobrescreveOEstadoPersistido() {
        var criado = caso.criar(dono.getId(), dados());
        eventos.conflito = true;
        assertThrows(ConflitoOperacaoException.class, () -> caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO));
        assertEquals(EstadoEvento.RASCUNHO, eventos.buscarPorId(criado.id()).orElseThrow().getEstado());
    }
    @Test void reconstituiProgramacaoDeEventoEncerradoSemPermitirEdicao() {
        var atividade = new Atividade("Palestra", "Descrição", inicio, inicio.plusHours(1), "Sala", 10);
        var evento = Evento.reconstituir(UUID.randomUUID(), "Evento", "Descrição", dono,
                inicio, inicio.plusHours(2), "Local", EstadoEvento.ENCERRADO, List.of(atividade));
        assertEquals(1, evento.getProgramacao().consultarAtividades().size());
        evento.getProgramacao().adicionarAtividade(new Atividade("Outra", "Descrição"));
        assertEquals(1, evento.getProgramacao().consultarAtividades().size());
        assertThrows(IllegalStateException.class, () -> evento.adicionarAtividade(atividade));
    }
    @Test void contaAceitaParticipanteEBuscaDadosAtuais() {
        var visitante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
        var conta = new ConsultarMinhaContaUseCase(usuarios);
        assertEquals(visitante.getId(), conta.executar(visitante.getId()).usuarioId());
        assertThrows(UnsupportedOperationException.class, () -> conta.executar(visitante.getId()).perfis().clear());
        usuarios.removerPorId(visitante.getId());
        assertThrows(TokenInvalidoException.class, () -> conta.executar(visitante.getId()));
    }
    @Test void perfilRevogadoNaoImpedeConsultaDeEventoJaPublicado() {
        var criado = caso.criar(dono.getId(), dados());
        caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.PUBLICADO);
        dono.removerPerfil(Perfil.ORGANIZADOR);
        assertEquals(criado.id(), caso.consultarPublicado(criado.id()).id());
        assertThrows(AcessoNegadoException.class,
                () -> caso.alterarEstado(dono.getId(), criado.id(), EstadoEvento.ENCERRADO));
    }
    @Test void categoriaPersisteEOrganizadorPodeClassificarEventoExistente() {
        var dadosComCategoria = new DadosNovoEvento("Simpósio", dados().descricao(), "Auditório",
                inicio, inicio.plusHours(2), CategoriaEvento.ACADEMICO);
        var criado = caso.criar(dono.getId(), dadosComCategoria);
        assertEquals(CategoriaEvento.ACADEMICO, criado.categoria());
        assertEquals(CategoriaEvento.ACADEMICO, caso.listarDoOrganizador(dono.getId()).getFirst().categoria());
        var alterado = caso.alterarCategoria(dono.getId(), criado.id(), CategoriaEvento.TECNOLOGIA);
        assertEquals(CategoriaEvento.TECNOLOGIA, alterado.categoria());
        assertEquals(CategoriaEvento.TECNOLOGIA, caso.listarDoOrganizador(dono.getId()).getFirst().categoria());
        var outro = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
        assertThrows(AcessoNegadoException.class,
                () -> caso.alterarCategoria(outro.getId(), criado.id(), CategoriaEvento.OUTROS));
        assertThrows(IllegalArgumentException.class,
                () -> caso.alterarCategoria(dono.getId(), criado.id(), null));
        assertEquals(CategoriaEvento.OUTROS, caso.criar(dono.getId(), dados()).categoria());
    }
}
