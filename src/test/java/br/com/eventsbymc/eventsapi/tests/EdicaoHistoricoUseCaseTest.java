package br.com.eventsbymc.eventsapi.tests;
import br.com.eventsbymc.eventsapi.application.exception.*;
import br.com.eventsbymc.eventsapi.application.port.out.*;
import br.com.eventsbymc.eventsapi.application.usecase.*;
import br.com.eventsbymc.eventsapi.domain.model.*;
import br.com.eventsbymc.eventsapi.domain.model.exception.EmailJaCadastradoException;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EdicaoHistoricoUseCaseTest {
    final RepositoriosEmMemoria.Usuarios usuarios = new RepositoriosEmMemoria.Usuarios();
    final RepositoriosEmMemoria.Eventos eventos = new RepositoriosEmMemoria.Eventos();
    final Usuario dono = RepositoriosEmMemoria.usuario(usuarios, Perfil.ORGANIZADOR);
    final Usuario participante = RepositoriosEmMemoria.usuario(usuarios, Perfil.PARTICIPANTE);
    Evento evento() {
        var inicio = LocalDateTime.of(2026,11,25,9,0);
        var e = new Evento("Evento original", "Descrição original para os testes", dono, inicio, inicio.plusHours(8), "Auditório");
        eventos.salvar(e); return e;
    }
    DadosNovoEvento dados(Evento e) { return new DadosNovoEvento("Evento alterado", "Descrição atualizada para os testes", "Sala nova",e.getInicio(),e.getFim(),CategoriaEvento.ACADEMICO); }
    @Test void espacosSimultaneosConflitamMasSalasDiferentesEIntervalosAdjacentesNao() {
        var inicio=LocalDateTime.of(2026,11,25,9,0);
        var a=new OcupacaoEspaco(null," Sala  1 ",inicio,inicio.plusHours(1));
        assertTrue(a.conflitaCom(new OcupacaoEspaco(null,"SALA 1",inicio.plusMinutes(30),inicio.plusHours(2))));
        assertFalse(a.conflitaCom(new OcupacaoEspaco(null,"Sala 2",inicio,inicio.plusHours(1))));
        assertFalse(a.conflitaCom(new OcupacaoEspaco(null,"Sala 1",inicio.plusHours(1),inicio.plusHours(2))));
        var espaco=UUID.randomUUID();
        assertTrue(new OcupacaoEspaco(espaco,"Nome antigo",inicio,inicio.plusHours(1))
                .conflitaCom(new OcupacaoEspaco(espaco,"Nome novo",inicio,inicio.plusHours(1))));
    }
    @Test void atualizaSomenteDadosBasicosSemMudarSenhaOuPerfis() {
        var caso = new AtualizarMinhaContaUseCase(usuarios);
        var perfis = Set.copyOf(dono.getPerfis()); var hash = dono.getSenhaHash();
        var resposta = caso.atualizar(dono.getId(), "  Nome atualizado  ", "NOVA@EXAMPLE.TEST");
        assertEquals("Nome atualizado",resposta.nome()); assertEquals("nova@example.test",resposta.email());
        assertEquals(perfis,dono.getPerfis()); assertEquals(hash,dono.getSenhaHash());
    }
    @Test void cadastroRecusaDuplicidadeEmailInvalidoESessaoAusente() {
        var caso = new AtualizarMinhaContaUseCase(usuarios);
        assertThrows(EmailJaCadastradoException.class,()->caso.atualizar(dono.getId(),"Nome",participante.getPessoa().getEmail()));
        assertThrows(IllegalArgumentException.class,()->caso.atualizar(dono.getId(),"Nome","sem-arroba"));
        assertThrows(TokenInvalidoException.class,()->caso.atualizar(null,"Nome","novo@example.test"));
        assertThrows(TokenInvalidoException.class,()->caso.atualizar(UUID.randomUUID(),"Nome","novo@example.test"));
    }
    @Test void edicaoExigeDonoOuAdministradorEPreservaEstado() {
        var e = evento(); e.publicar(); eventos.salvar(e);
        var caso = new EventosUseCase(eventos,usuarios);
        assertThrows(AcessoNegadoException.class,()->caso.editar(participante.getId(),e.getId(),dados(e)));
        var outro = RepositoriosEmMemoria.usuario(usuarios,Perfil.ORGANIZADOR);
        assertThrows(AcessoNegadoException.class,()->caso.editar(outro.getId(),e.getId(),dados(e)));
        assertEquals("Evento alterado",caso.editar(dono.getId(),e.getId(),dados(e)).titulo());
        assertEquals(EstadoEvento.PUBLICADO,eventos.buscarPorId(e.getId()).orElseThrow().getEstado());
        var admin = RepositoriosEmMemoria.usuario(usuarios,Perfil.ADMINISTRADOR);
        assertNotNull(caso.editar(admin.getId(),e.getId(),dados(e)));
    }
    @Test void eventoEncerradoOuBloqueadoNaoPodeSerEditado() {
        var e = evento(); e.publicar(); e.encerrar(); eventos.salvar(e);
        var caso = new EventosUseCase(eventos,usuarios);
        assertThrows(ConflitoOperacaoException.class,()->caso.editar(dono.getId(),e.getId(),dados(e)));
        e.bloquear("Motivo de bloqueio"); eventos.salvar(e);
        assertThrows(ConflitoOperacaoException.class,()->caso.editar(dono.getId(),e.getId(),dados(e)));
    }
    @Test void reduzirPeriodoNaoPodeDeixarAtividadeForaDoEvento() {
        var e = evento();
        var atividade = new Atividade("Oficina", "Descrição",e.getInicio().plusHours(2),e.getInicio().plusHours(3),"Sala",10);
        e.adicionarAtividade(atividade); eventos.salvar(e);
        var caso = new EventosUseCase(eventos,usuarios);
        var curto = new DadosNovoEvento("Evento novo","Descrição atualizada para os testes","Sala",e.getInicio(),e.getInicio().plusHours(1),CategoriaEvento.OUTROS);
        assertThrows(IllegalArgumentException.class,()->caso.editar(dono.getId(),e.getId(),curto));
        assertEquals(1,eventos.buscarPorId(e.getId()).orElseThrow().getProgramacao().consultarAtividades().size());
    }
    @Test void alteracaoDeRegraConsolidadaERecusada() {
        var e=evento();e.publicar();e.encerrar();eventos.salvar(e);
        var politica = new DadosProgramacao.Politica(true,null,null,null,true,75);
        var repo=(ProgramacaoRepository)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{ProgramacaoRepository.class},(p,m,a)-> {
            if(m.getName().equals("consultarPolitica"))return politica;
            fail("Não deve persistir política consolidada");return null;
        });
        var caso=new ProgramacaoUseCase(repo,eventos,usuarios);
        assertThrows(ConflitoOperacaoException.class,()->caso.atualizarPolitica(dono.getId(),e.getId(),new DadosProgramacao.Politica(true,null,null,null,true,50)));
    }
    @Test void ajusteManualExigePermissaoMotivoESituacaoExplicita() {
        var e=evento();var atividade=UUID.randomUUID();var gravados=new ArrayList<DadosFrequencia.Ajuste>();
        var repo=new FrequenciaRepository(){
            public UUID eventoDaAtividade(UUID id){return e.getId();}
            public DadosFrequencia.Registro registrar(UUID a,UUID u,UUID r,String m){throw new UnsupportedOperationException();}
            public List<DadosFrequencia.Registro> listar(UUID id){return List.of();}
            public List<DadosFrequencia.Ajuste> ajustes(UUID id){return List.copyOf(gravados);}
            public DadosFrequencia.Ajuste ajustar(UUID id,UUID r,DadosFrequencia.NovoAjuste a){
                var ajuste=new DadosFrequencia.Ajuste(a.id(),id,a.usuarioId(),a.confirmada(),a.motivo(),Instant.now(),r);gravados.add(ajuste);return ajuste;
            }
        };
        var caso=new FrequenciaUseCase(repo,eventos,usuarios);
        var ajuste=new DadosFrequencia.NovoAjuste(UUID.randomUUID(),participante.getId(),false,"Correção solicitada pelo participante");
        assertThrows(AcessoNegadoException.class,()->caso.ajustar(participante.getId(),atividade,ajuste));
        assertThrows(IllegalArgumentException.class,()->caso.ajustar(dono.getId(),atividade,new DadosFrequencia.NovoAjuste(UUID.randomUUID(),participante.getId(),true," ")));
        assertThrows(IllegalArgumentException.class,()->caso.ajustar(dono.getId(),atividade,new DadosFrequencia.NovoAjuste(UUID.randomUUID(),participante.getId(),null,"Motivo válido")));
        assertFalse(caso.ajustar(dono.getId(),atividade,ajuste).confirmada());
        assertEquals(dono.getId(),caso.ajustes(dono.getId(),atividade).getFirst().responsavelId());
    }
    @Test void relatorioDetalhadoRespeitaPropriedade() {
        var e=evento();
        var repo=(RelatoriosRepository)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[]{RelatoriosRepository.class},(p,m,a)->List.of());
        var caso=new RelatoriosUseCase(repo,eventos,usuarios);
        assertThrows(AcessoNegadoException.class,()->caso.porAtividade(participante.getId(),e.getId()));
        assertEquals(List.of(),caso.porAtividade(dono.getId(),e.getId()));
    }
}
