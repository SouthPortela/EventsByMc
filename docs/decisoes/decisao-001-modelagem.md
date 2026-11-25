# Decisão 001 — Modelagem e fronteiras do domínio

## Decisão

O núcleo utiliza Java 21 sem dependências de Spring ou de persistência. Evento,
Atividade, Usuario e os objetos de valor protegem suas invariantes. Programacao
compõe atividades; tipos de atividade são categorias, não subclasses sem comportamento.

A aplicação coordena operações por portas em application/port/in e
application/port/out. Adaptadores JDBC e HTTP implementam as fronteiras. A
CompositionRoot escolhe as implementações e injeta as dependências pelos construtores.

## Persistência e reconstrução

UUID identifica contas, eventos e demais recursos. Organizador e participante são
perfis de Usuario; não são subclasses persistidas separadamente. Pessoa representa
dados pessoais, enquanto pessoas_evento registra convidados da programação.

Reconstruir um evento persistido não equivale a adicionar novas atividades: o histórico
continua legível após encerramento ou revogação de perfil. Bloqueio preserva motivo
e estado anterior; novas operações são limitadas conforme o caso de uso.

Inscrições, agenda, avaliações e certificados têm contratos e registros próprios
no banco, coordenados pelos casos de uso. O modelo conceitual não implica uma classe
Java para cada tabela. SQL parametrizado e restrições preservam vínculos e unicidade.

## Consequências

- Domínio e casos de uso podem ser testados com implementações em memória das portas.
- Alterações do transporte ou banco ficam nos adaptadores.
- Alterações estruturais são migrações explícitas; não há ORM ou Flyway.
- A API existente permanece compatível; novas operações usam contratos próprios.

Referências: [modelo](../dominio/modelo-dominio.md),
[responsabilidades](../dominio/mapa-responsabilidades.md) e
[autenticação](decisao-004-autenticacao-autorizacao.md).
