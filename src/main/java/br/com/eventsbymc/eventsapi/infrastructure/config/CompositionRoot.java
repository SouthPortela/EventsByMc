package br.com.eventsbymc.eventsapi.infrastructure.config;

import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcConnectionFactory;
import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcUsuarioRepository;
import br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcEventoRepository;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.in.OperacoesEvento;
import br.com.eventsbymc.eventsapi.application.usecase.ConsultarMinhaContaUseCase;
import br.com.eventsbymc.eventsapi.application.usecase.EventosUseCase;
import br.com.eventsbymc.eventsapi.application.port.in.AutenticarUsuario;
import br.com.eventsbymc.eventsapi.application.port.out.CodePass;
import br.com.eventsbymc.eventsapi.application.port.out.TokenProvider;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import br.com.eventsbymc.eventsapi.application.usecase.AutenticarUsuarioUseCase;
import br.com.eventsbymc.eventsapi.application.usecase.RegistrarUsuarioUseCase;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.BcryptCodePassAdapter;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.JwtTokenProviderAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

// CompositionRoot é o nó "ou o coração" do projeto: é o único lugar que conhece
// todas as classes concretas ao mesmo tempo.
// Em arquitetura hexagonal isso tem nome: "composition root"
// Cada caso de uso/handler só pede uma porta (interface) no construtor, sem saber
// qual implementação vai receber; é aqui, e só aqui, que decidimos "quando alguém
// pedir TokenProvider, entregue um JwtTokenProviderAdapter" — e assim por diante.
public final class CompositionRoot {

    public final UsuarioRepository usuarioRepository;
    public final CodePass codePass;
    public final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    public final TokenProvider tokenProvider;
    public final AutenticarUsuario autenticarUsuario;
    public final ObjectMapper objectMapper;
    public final ConsultarMinhaConta consultarMinhaConta;
    public final OperacoesEvento eventos;
    public final br.com.eventsbymc.eventsapi.application.port.in.AdministrarPlataforma administracao;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesPresenca presencas;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesProgramacao programacao;
    public final br.com.eventsbymc.eventsapi.application.port.in.ConsultarRelatorios relatorios;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesAvaliacao avaliacoes;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesInteracao interacao;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesCertificado certificados;
    public final br.com.eventsbymc.eventsapi.application.port.in.OperacoesFrequencia frequencia;

    private CompositionRoot() {
        JdbcConnectionFactory jdbcConnectionFactory = JdbcConnectionFactory.fromEnvironment();
        this.usuarioRepository = new JdbcUsuarioRepository(jdbcConnectionFactory);
        this.consultarMinhaConta = new ConsultarMinhaContaUseCase(usuarioRepository);
        this.eventos = new EventosUseCase(new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.administracao = new br.com.eventsbymc.eventsapi.application.usecase.AdministracaoUseCase(
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.presencas = new br.com.eventsbymc.eventsapi.application.usecase.PresencaUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcPresencaRepository(jdbcConnectionFactory),
                usuarioRepository, new JdbcEventoRepository(jdbcConnectionFactory),
                new br.com.eventsbymc.eventsapi.infrastructure.adapter.CodigoPresencaSeguro());
        this.programacao = new br.com.eventsbymc.eventsapi.application.usecase.ProgramacaoUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcProgramacaoRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.relatorios = new br.com.eventsbymc.eventsapi.application.usecase.RelatoriosUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcRelatoriosRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.avaliacoes = new br.com.eventsbymc.eventsapi.application.usecase.AvaliacoesUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcAvaliacaoRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.interacao = new br.com.eventsbymc.eventsapi.application.usecase.InteracaoUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcInteracaoRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.certificados = new br.com.eventsbymc.eventsapi.application.usecase.CertificadosUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcCertificadoRepository(jdbcConnectionFactory),
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcRelatoriosRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository,
                br.com.eventsbymc.eventsapi.infrastructure.adapter.SmtpEmailCertificado.fromEnvironment());
        this.frequencia = new br.com.eventsbymc.eventsapi.application.usecase.FrequenciaUseCase(
                new br.com.eventsbymc.eventsapi.adapter.out.jdbc.JdbcFrequenciaRepository(jdbcConnectionFactory),
                new JdbcEventoRepository(jdbcConnectionFactory), usuarioRepository);
        this.codePass = new BcryptCodePassAdapter();
        this.registrarUsuarioUseCase = new RegistrarUsuarioUseCase(usuarioRepository, codePass);
        this.tokenProvider = JwtTokenProviderAdapter.fromEnvironment();
        this.autenticarUsuario = new AutenticarUsuarioUseCase(usuarioRepository, codePass, tokenProvider);

        //sem esse module o Jackson não sabe serializar o Instant do ErroRespostaDTO
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static CompositionRoot montar() {
        return new CompositionRoot();
    }
}
