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

Restrição atual: **não utilizar Spring/Spring Boot**. O núcleo permanece em Java 21,
com domínio e aplicação separados dos adaptadores HTTP e JDBC.

## Decisão

Adotamos **JWT (biblioteca `jjwt`) para emissão/validação de token**, BCrypt
(`at.favre.lib:bcrypt`) para hash de senha e o `HttpServer` do JDK para receber HTTP.
O `AutenticacaoFiltro` estende `com.sun.net.httpserver.Filter`. O
`AutorizacaoInterceptor` é uma classe própria chamada pelo `Router`, sem dependência
do Spring ou de Servlet. A `CompositionRoot` monta as dependências pelos construtores.

O webapp Vue consome a API pelo prefixo externo `/api`. No backend, as rotas atuais
são cadastro, login, conta e operações de eventos, conforme os
[contratos atuais](../api/contratos-webapp.md). O proxy encaminha as chamadas removendo
o prefixo quando se conecta diretamente ao servidor Java.

## Alternativas consideradas

| Alternativa | Motivo de não ter sido escolhida |
|---|---|
| Spring/Spring Boot | Incompatível com a restrição atual da disciplina; o projeto usa o servidor HTTP do JDK. |
| Sessão de servidor (cookie + estado no backend) | É uma alternativa válida para o webapp. Foi mantido JWT para aproveitar o contrato de login existente; essa escolha exige expiração e tratamento de sessão inválida no cliente. |
| Implementar JWT "na mão" (assinatura HMAC manual) | Reinventar criptografia é desnecessário e arriscado; `jjwt` é uma biblioteca pequena e focada, não um framework de aplicação. |

## Consequências

- Dependências de segurança: `jjwt-api`, `jjwt-impl`, `jjwt-jackson` e `bcrypt`.
  Jackson serializa e desserializa os contratos HTTP. Nenhuma delas requer Spring.
- A store atual guarda a sessão em `sessionStorage`. Isso não substitui validação
  do token e autorização no servidor; não expor tokens em logs ou no HTML.
- A infraestrutura consulta `@RequerPerfil` por reflexão. `GestaoEventosHandler`
  exige ORGANIZADOR ou ADMINISTRADOR; o caso de uso consulta os perfis atuais no banco
  e verifica a propriedade do evento. Um organizador não publica eventos de outro.
- O contexto autenticado é guardado em `ThreadLocal` e removido no `finally` do filtro.
  O fluxo atual é síncrono; operações assíncronas não devem presumir propagação desse contexto.
- O JWT de login autentica geração e confirmação da chamada de presença.
  O QR usa um código aleatório separado, com validade de cinco minutos e hash
  persistido. Não contém JWT de login, senha ou identidade de participante.
  Consulte a [decisão de frequência](decisao-002-frequencia.md).
- Recuperação de senha, login social e autenticação multifator ficam fora desta etapa.
  Refresh token e OAuth2 não estão previstos no primeiro fluxo de integração.

## Integração implementada

O [guia do webapp](../api/integracao-webapp.md) apresenta a consulta
`GET /api/usuarios/me`, o envio de Bearer pelo cliente HTTP e o tratamento de 401.
A rota usa o ID do token validado e consulta os dados atuais no banco. Não aceita
um ID escolhido pelo navegador e não retorna o hash de senha.

Nesta consulta basta estar autenticado. `VISITANTE` representa quem navega sem
sessão, sem registro em `usuario_perfis`. O cadastro público cria PARTICIPANTE
por padrão ou ORGANIZADOR mediante escolha explícita. A migração V3 converte
contas antigas com VISITANTE. ADMINISTRADOR só é concedido por operação
controlada no banco, conforme o [guia](../../db/README.md).

O filtro libera as rotas públicas por método e caminho. `GET /eventos` é público,
mas `POST /eventos` exige JWT e autorização. Respostas privadas usam `no-store`.

## Padrões de projeto aplicados

- **Adapter**: `JdbcUsuarioRepository` (porta `UsuarioRepository`), `BcryptCodePassAdapter`
  (porta `CodePass`) e `JwtTokenProviderAdapter` (porta `TokenProvider`) adaptam
  tecnologia externa (JDBC, Bcrypt, JWT) aos contratos definidos pela aplicação — trocar
  Postgres, o algoritmo de hash ou a biblioteca de token não exige alterar nenhum caso de
  uso. Os handlers HTTP adaptam a entrada web aos casos de uso.
- **Strategy**: `CodePass` e `TokenProvider` são estratégias intercambiáveis, escolhidas
  na composição (`CompositionRoot`), não pelo caso de uso que as utiliza. As políticas
  configuráveis do domínio ainda precisam demonstrar suas próprias variações de comportamento.
