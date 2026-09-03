# Decisão 004 — Estratégia de Autenticação e Autorização

## Contexto

O documento oficial de especificação de requisitos (item D-02, seção 8.2) não prescreve
a tecnologia de autenticação/autorização a ser usada — cabe à equipe escolher e justificar.
Os requisitos que essa decisão precisa atender são:

- **RF-01/RN-01**: cadastro com nome, e-mail e senha; e-mail duplicado deve ser rejeitado
  com mensagem clara.
- **RF-02**: autenticar usuários e aplicar permissões conforme o perfil (Administrador,
  Organizador, Participante, Visitante); funções protegidas indisponíveis a perfis sem
  autorização.
- **RNF-05**: senhas sempre em hash, nunca em texto puro.
- **RNF-06**: autorização deve ser verificada no servidor/API, nunca apenas ocultada na
  interface.
- **RNF-07/RNF-14**: mensagens de erro compreensíveis, sem expor senha ou dado sensível em
  log.

Restrição adicional definida pela equipe: **minimizar o uso de frameworks**, já que o
objetivo central da disciplina (POO II) é aprender a modelar e implementar corretamente
em Java puro, não configurar bibliotecas prontas.

## Decisão

Adotamos **JWT (biblioteca `jjwt`) para emissão/validação de token, com o mecanismo de
autenticação e autorização escrito manualmente** (um `jakarta.servlet.Filter` para
autenticação e um `org.springframework.web.servlet.HandlerInterceptor` para autorização
por perfil) — **sem adicionar `spring-boot-starter-security`**.

## Alternativas consideradas

| Alternativa | Motivo de não ter sido escolhida |
|---|---|
| `spring-boot-starter-security` completo | Traz superfície de configuração grande (`SecurityFilterChain`, `UserDetailsService`, `AuthenticationManager`, CSRF, sessão) desproporcional para 4 perfis fixos e poucos endpoints protegidos; delega a lógica de autorização ao framework, reduzindo o que a equipe efetivamente projeta e implementa em POO. |
| Sessão de servidor (cookie + estado no backend) | A API é consumida tanto pelo desktop quanto pelo site público (RNF-02), então um mecanismo *stateless* (token autocontido) evita depender de armazenamento de sessão compartilhado entre clientes diferentes. |
| Implementar JWT "na mão" (assinatura HMAC manual) | Reinventar criptografia é desnecessário e arriscado; `jjwt` é uma biblioteca pequena e focada, não um framework de aplicação. |

## Consequências

- Duas dependências novas no `pom.xml`: `jjwt-api`, `jjwt-impl`, `jjwt-jackson`. Nenhuma
  outra dependência de segurança foi adicionada.
- O mesmo mecanismo de token (`TokenProvider`/`JwtTokenProviderAdapter`) já usado para
  login também está disponível para as rotas de QR Code (exigido pelo `AGENTS.md`),
  evitando duas soluções de token diferentes no mesmo projeto.
- Autorização por perfil é aplicada via anotação própria (`@RequerPerfil`), lida por
  reflexão no `AutorizacaoInterceptor` — qualquer novo endpoint (ex.: criação de evento,
  responsabilidade de outro membro da equipe) só precisa da anotação; nenhuma mudança é
  necessária nos casos de uso.
- Fora de escopo (já excluído pelo documento oficial, seção 2.1): recuperação de senha,
  login social, autenticação multifator, refresh token, OAuth2.

## Padrões de projeto aplicados

- **Adapter**: `JdbcUsuarioRepository` (porta `UsuarioRepository`), `BcryptCodePassAdapter`
  (porta `CodePass`) e `JwtTokenProviderAdapter` (porta `TokenProvider`) adaptam
  tecnologia externa (JDBC, Bcrypt, JWT) aos contratos definidos pela aplicação — trocar
  Postgres, o algoritmo de hash ou a biblioteca de token não exige alterar nenhum caso de
  uso ou controller.
- **Strategy**: `CodePass` e `TokenProvider` são estratégias intercambiáveis, escolhidas
  na composição (`BeansConfig`), não pelo caso de uso que as utiliza.
