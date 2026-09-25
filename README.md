EventsByMc
## Equipe

**Projeto:** EventsByMc — Plataforma de Gestão de Eventos

**Disciplina:** Programação Orientada a Objetos II

### Integrantes

| Integrante |
|---|
| Brandon Handes Nogueira da Silva |
| Brendha Alves dos Santos |
| Gabriel Pereira Brito |
| Luciano Cassimiro De Godoi Filho |
| Marcos Marques Portela |


O projeto é desenvolvido de forma colaborativa, com o código-fonte, documentação e demais artefatos versionados no repositório da equipe.
 Plataforma de Gestão de Eventos

O **EventsByMc** é uma plataforma de gestão de eventos acadêmicos e profissionais desenvolvida em **Java**, com foco em **Programação Orientada a Objetos, arquitetura hexagonal, SOLID, padrões de projeto, persistência, testes e integração entre aplicações**.

A plataforma centraliza o gerenciamento de eventos, permitindo que organizadores administrem eventos, atividades, programação, inscrições, presença, avaliações e relatórios, enquanto participantes podem consultar eventos, realizar inscrições, acompanhar sua programação e registrar presença.

O projeto foi desenvolvido como atividade integradora da disciplina de **Programação Orientada a Objetos II**.

---

Objetivos

O projeto tem como principais objetivos:

- aplicar conceitos de Programação Orientada a Objetos;
- representar o domínio por meio de objetos com responsabilidades bem definidas;
- aplicar encapsulamento e proteção de invariantes;
- utilizar composição e polimorfismo em pontos reais de variação;
- aplicar princípios SOLID;
- utilizar arquitetura hexagonal;
- separar domínio, aplicação, entrada e persistência;
- disponibilizar uma API para comunicação com as interfaces;
- persistir os dados em banco relacional;
- implementar autenticação e autorização;
- desenvolver testes automatizados;
- documentar decisões arquiteturais e evolução do projeto.

---

## Funcionalidades

Entre os recursos presentes na implementação estão:

### Usuários e autenticação

- cadastro de usuários;
- autenticação;
- autenticação por JWT;
- armazenamento seguro de senhas com BCrypt;
- controle de acesso por perfil;
- perfis de participante, organizador e administrador;
- consulta da conta autenticada.

### Eventos

- criação e gerenciamento de eventos;
- consulta de eventos;
- controle de estado do evento;
- programação vinculada ao evento;
- cadastro e gerenciamento de atividades.

### Inscrições

- inscrição de participantes;
- controle de vínculo entre participante e evento/atividade;
- validações relacionadas à inscrição.

### Programação

- organização de atividades;
- horários e programação;
- consulta da programação pelo frontend;
- tratamento de conflitos em operações relacionadas à programação.

### Frequência e presença

- registro de presença;
- utilização de código temporário;
- geração de QR Code no frontend;
- validade limitada do código;
- revogação de chamada anterior;
- armazenamento seguro do código;
- validação de inscrição e autenticação;
- controle para evitar registros duplicados de presença.

### Avaliações e relatórios

O projeto também possui estrutura de aplicação e persistência para:

- avaliações;
- questionários;
- respostas;
- consulta de resultados;
- relatórios;
- integração dos relatórios com o frontend.

---

## Tecnologias utilizadas

### Backend

- **Java 21**
- **Maven**
- **JDBC**
- **PostgreSQL**
- **Jackson**
- **JJWT**
- **BCrypt**
- **JUnit 5**
- **JDK HttpServer**

### Frontend

- **Vue 3**
- **TypeScript**
- **Vite**
- **Vue Router**
- **Pinia**
- **Bootstrap**
- **QRCode**
- **Vitest**
- **ESLint**
- **Oxlint**
- **Prettier**

### Infraestrutura

- Docker
- Docker Compose
- variáveis de ambiente
- scripts de banco de dados

---

## Arquitetura

O backend foi organizado seguindo os princípios da **Arquitetura Hexagonal (Ports and Adapters)**.

```text
                    ┌─────────────────────┐
                    │      Frontend       │
                    │   Vue + TypeScript  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Adaptadores      │
                    │     de entrada      │
                    │      HTTP/API        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     Aplicação       │
                    │    Casos de uso     │
                    │      + Ports        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │       Domínio       │
                    │ Entidades e regras  │
                    │     de negócio      │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Adaptadores de saída│
                    │ JDBC / Persistência │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │     PostgreSQL      │
                    └─────────────────────┘
```

A estrutura principal do backend está organizada em:

```text
src/main/java/
└── ...
    ├── domain/
    │   ├── model/
    │   └── exception/
    │
    ├── application/
    │   ├── port/
    │   │   ├── in/
    │   │   └── out/
    │   └── usecase/
    │
    ├── adapter/
    │   └── out/
    │       └── jdbc/
    │
    └── infrastructure/
        ├── adapter/
        │   └── in/
        │       └── web/
        └── config/
```

Essa organização procura manter o **domínio e a aplicação independentes da interface e da persistência**, utilizando interfaces como pontos de comunicação entre as camadas.

---

## Principais conceitos de POO aplicados

O projeto utiliza diferentes conceitos de Programação Orientada a Objetos:

### Encapsulamento

As regras relacionadas ao estado dos objetos são mantidas nas próprias classes de domínio, evitando que qualquer camada altere seus dados indiscriminadamente.

### Composição

Os objetos colaboram entre si para realizar comportamentos do sistema, reduzindo dependências desnecessárias.

### Polimorfismo

Pontos de variação do sistema utilizam abstrações e implementações diferentes, especialmente em políticas e componentes de infraestrutura.

### Interfaces

Interfaces são utilizadas nas fronteiras da aplicação e nos pontos em que o comportamento precisa ser substituível.

Exemplos:

```text
EventoRepository
UsuarioRepository
ProgramacaoRepository
PresencaRepository
AvaliacaoRepository
RelatoriosRepository
TokenProvider
CodePass
```

### Inversão de dependência

Os casos de uso dependem de abstrações, enquanto os detalhes de infraestrutura implementam essas abstrações.

---

## Padrões e princípios

O projeto utiliza, entre outros, conceitos relacionados aos padrões:

### Adapter

Utilizado para conectar as interfaces definidas pela aplicação às implementações concretas de infraestrutura.

### Strategy

Utilizado em pontos que possuem diferentes políticas ou comportamentos que podem variar.

Além dos padrões, o projeto busca aplicar princípios **SOLID**, especialmente:

- responsabilidade única;
- inversão de dependência;
- separação entre domínio e infraestrutura;
- interfaces pequenas e específicas;
- baixo acoplamento.

---

## Persistência

A aplicação utiliza **PostgreSQL** como banco de dados relacional e **JDBC** para acesso aos dados.

As alterações estruturais do banco são organizadas por scripts e migrações.

Exemplo de estrutura:

```text
src/main/resources/
└── db/
    └── migration/
        ├── V1__cria_modelo_inicial.sql
        ├── V2__inscricoes_chamadas_presencas.sql
        └── V3__perfis_de_contas_autenticadas.sql
```

Também existem scripts adicionais de banco na pasta:

```text
db/
```

Esses arquivos são utilizados para criação, atualização e preparação do banco de dados.

---

## Segurança

A autenticação da aplicação utiliza:

- **JWT** para identificação da sessão;
- **BCrypt** para armazenamento seguro das senhas;
- autorização realizada no backend;
- controle de acesso de acordo com o perfil do usuário;
- consultas SQL parametrizadas;
- códigos de presença armazenados de forma protegida.

O QR Code utilizado no fluxo de presença não deve conter senha ou token JWT do usuário.

---

## Frontend

O frontend foi desenvolvido utilizando **Vue 3 + TypeScript**.

Sua responsabilidade é consumir a API disponibilizada pelo backend, apresentando as funcionalidades para os usuários.

Entre as telas e recursos existentes estão:

- autenticação;
- cadastro;
- eventos;
- programação;
- agenda;
- presença;
- relatórios;
- comunicação com os serviços HTTP da aplicação.

O frontend não acessa diretamente o banco de dados. A comunicação ocorre por meio da API.

---

## Testes

O projeto possui testes automatizados no backend e frontend.

### Backend

Entre os testes existentes estão:

```text
EventoTest
UsuarioTest
EventosUseCaseTest
PresencaUseCaseTest
NovosRequisitosUseCaseTest
ApiWebIntegrationTest
JwtTokenProviderAdapterTest
JdbcEventoRepositoryTest
JdbcUsuarioRepositoryTest
JdbcPersistenciaIntegrationTest
```

Os testes abrangem diferentes níveis, incluindo:

- regras do domínio;
- casos de uso;
- autenticação;
- persistência;
- integração com a API.

### Frontend

Também existem testes utilizando **Vitest**, incluindo testes relacionados a:

- autenticação;
- cadastro e login;
- eventos;
- filtros;
- validações;
- presença;
- cliente HTTP;
- serviços da aplicação.

> Os testes devem ser executados no ambiente configurado do projeto antes da entrega final para registrar os resultados efetivamente obtidos.

---

## Documentação

A documentação do projeto está organizada principalmente na pasta:

```text
docs/
```

### API

```text
docs/api/
├── contratos-webapp.md
├── integracao-webapp.md
├── presenca.md
└── requisitos-funcionais-webapp.md
```

### Domínio

```text
docs/dominio/
├── glossario.md
├── mapa-responsabilidades.md
└── modelo-dominio.md
```

### Decisões arquiteturais

```text
docs/decisoes/
├── decisao-001-modelagem.md
├── decisao-002-frequencia.md
├── decisao-003-questionario.md
├── decisao-004-autenticacao-autorizacao.md
└── decisoes-semana-1.md
```

### Backlog

```text
docs/backlog/
└── backlog.md
```

### Outros documentos

```text
docs/termo-de-abertura/
└── termo-de-abertura.md
```

---

## Como executar

### Pré-requisitos

Para executar o projeto, recomenda-se possuir instalado:

- Java 21;
- Maven;
- Node.js e npm;
- PostgreSQL;
- Git;
- Docker e Docker Compose, caso seja utilizada a configuração por containers.

### Backend

Entre na pasta do backend e configure as variáveis de ambiente necessárias para conexão com o banco e autenticação.

Depois, utilize o Maven para compilar o projeto:

```bash
mvn clean install
```

Para executar a aplicação:

```bash
mvn spring-boot:run
```

> O comando exato de inicialização deve seguir a configuração atualmente definida no projeto.

### Frontend

Entre na pasta do frontend e instale as dependências:

```bash
npm install
```

Execute o ambiente de desenvolvimento:

```bash
npm run dev
```

O frontend deverá consumir a API disponibilizada pelo backend.

---

## Docker

O projeto possui arquivos relacionados à execução por containers:

```text
Dockerfile
docker-compose.yml
.env.example
deploy/
```

Antes da execução, deve-se configurar as variáveis de ambiente de acordo com o arquivo:

```text
.env.example
```

---

## Estrutura geral do projeto

```text
EventsByMc/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── ...
│
├── db/
│   ├── inicializar.sql
│   ├── atualizar-v2.sql
│   ├── atualizar-v3.sql
│   └── ...
│
├── docs/
│   ├── api/
│   ├── backlog/
│   ├── decisoes/
│   ├── dominio/
│   └── termo-de-abertura/
│
├── deploy/
├── Dockerfile
├── docker-compose.yml
└── .env.example
```

---

## Cenários principais

O projeto foi estruturado para atender aos principais fluxos definidos na especificação:

### CA-01 — Publicação

Organizador cria e mantém um evento e sua programação.

### CA-02 — Inscrição

Visitante cria uma conta e realiza inscrição.

### CA-03 — Agenda

Participante seleciona atividades e consulta sua programação.

### CA-04 — Frequência por QR Code

Participante elegível registra presença utilizando o mecanismo de chamada disponibilizado pelo sistema.

### CA-05 — Frequência manual

Organizador pode realizar operações relacionadas ao controle manual de presença conforme as regras implementadas.

### CA-06 — Avaliação

Participante elegível responde avaliações vinculadas às atividades.

### CA-07 — Relatórios

Organizador consulta informações consolidadas relacionadas a inscrições e presença.

A demonstração final deve executar os cenários de forma integrada, com persistência e tratamento de erros.

---

## Requisitos de qualidade

O projeto busca atender aos requisitos técnicos definidos para a disciplina:

- **ROO-01** — Modelo de domínio;
- **ROO-02** — Encapsulamento;
- **ROO-03** — Objetos de valor;
- **ROO-04** — Composição e colaboração;
- **ROO-05** — Polimorfismo;
- **ROO-06** — Herança quando conceitualmente adequada;
- **ROO-07** — Interfaces;
- **ROO-08** — SOLID;
- **ROO-09** — Arquitetura hexagonal;
- **ROO-10** — Padrões de projeto;
- **ROO-11** — Erros do domínio;
- **ROO-12** — Testes e refatoração.

A implementação e o status de cada requisito devem ser acompanhados pela matriz de rastreabilidade do projeto.



Documentos de referência

A implementação deve permanecer alinhada à:

- Especificação de Requisitos do projeto;
- modelo de domínio;
- mapa de responsabilidades;
- decisões arquiteturais D-01 a D-08;
- matriz de rastreabilidade RF × ROO;
- documentação da API;
- documentação de testes.
---

 Equipe

**Projeto:** EventsByMc — Plataforma de Gestão de Eventos

**Disciplina:** Programação Orientada a Objetos II

**Repositório:**  
https://github.com/SouthPortela/EventsByMc

---

 Status do projeto

O EventsByMc encontra-se em desenvolvimento incremental.

A implementação já possui a base arquitetural, domínio, casos de uso, persistência, autenticação, API, frontend, presença, avaliações, relatórios e testes.

As etapas finais concentram-se na **integração dos cenários completos, consolidação das decisões arquiteturais, rastreabilidade dos requisitos, documentação, refatorações e validação da entrega final**.
