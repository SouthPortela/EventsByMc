# Events by MC

## Plataforma de Gestão de Eventos

Projeto Integrador desenvolvido para a disciplina de **Programação Orientada a Objetos II**.

O **Events by MC** é uma plataforma de gestão de eventos acadêmicos e profissionais, desenvolvida para centralizar o gerenciamento de eventos, atividades, programação, inscrições e controle de presença.

O projeto tem como objetivo principal demonstrar a aplicação prática de **Programação Orientada a Objetos**, arquitetura hexagonal, princípios SOLID, interfaces, composição, polimorfismo, padrões de projeto, persistência e testes automatizados.

---

## 👥 Equipe

| Integrante     | Papel           |
| -------------- | --------------- |
| Brandon Handes | Desenvolvimento |
| Brendha Alves  | Desenvolvimento |
| Gabriel Brito  | Desenvolvimento |
| Luciano Godoi  | Desenvolvimento |
| Marcos Portela | Desenvolvimento |

---

# 📌 Status atual

**Status: 🟡 Em desenvolvimento — integração parcial**

O projeto possui um núcleo funcional integrado entre:

* API Java;
* PostgreSQL;
* frontend Vue;
* autenticação e autorização;
* cadastro e login;
* catálogo público de eventos;
* criação de eventos;
* publicação e encerramento;
* inscrição básica em eventos;
* criação de atividades;
* geração de chamada de presença;
* confirmação de presença por QR Code/código;
* testes automatizados de domínio, aplicação, API e frontend.

Entretanto, ainda existem requisitos obrigatórios da especificação que **não estão implementados** neste estado do repositório, principalmente agenda, conflitos, políticas configuráveis, avaliações, relatórios, administração completa e aplicação desktop.

---

# 🛠️ Tecnologias

## Backend

* Java 21
* JDK `HttpServer`
* JDBC
* PostgreSQL
* Jackson
* JJWT
* BCrypt
* JUnit 5
* Maven

## Frontend

* Vue 3
* TypeScript
* Vite
* Vue Router
* Pinia
* Bootstrap
* Vitest
* ESLint
* Oxlint
* Prettier
* QRCode

## Infraestrutura

* Docker
* Docker Compose
* Nginx

---

# 🏗️ Arquitetura

O backend segue uma organização baseada em **Arquitetura Hexagonal / Ports and Adapters**.

```text
┌───────────────────────────────────────────┐
│                  FRONTEND                 │
│              Vue + TypeScript             │
└─────────────────────┬─────────────────────┘
                      │ HTTP / JSON
                      ▼
┌───────────────────────────────────────────┐
│             ADAPTADORES DE ENTRADA        │
│      Router / Handlers / Autorização      │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│                 APLICAÇÃO                 │
│             Casos de Uso / Ports          │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│                  DOMÍNIO                  │
│ Evento / Atividade / Usuário / Regras     │
└─────────────────────┬─────────────────────┘
                      │
                      ▼
┌───────────────────────────────────────────┐
│             ADAPTADORES DE SAÍDA          │
│              JDBC / PostgreSQL            │
└───────────────────────────────────────────┘
```

### Organização principal do backend

```text
src/main/java/br/com/eventsbymc/eventsapi/

├── domain/
│   └── model/
│       ├── Evento
│       ├── Atividade
│       ├── Usuario
│       ├── Pessoa
│       ├── Programacao
│       ├── Perfil
│       ├── EstadoEvento
│       ├── RegrasChamada
│       └── ...
│
├── application/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   ├── usecase/
│   └── exception/
│
├── adapter/
│   └── out/
│       └── jdbc/
│
└── infrastructure/
    ├── adapter/
    │   ├── in/web/
    │   └── ...
    └── config/
```

---

# 🧠 Orientação a Objetos

A implementação atual já possui evidências de vários conceitos exigidos pela disciplina.

### Encapsulamento

* [x] Atributos privados.
* [x] Estado de `Evento` alterado por comportamentos como `publicar()` e `encerrar()`.
* [x] Invariantes verificadas dentro dos objetos.
* [x] Listas internas protegidas contra alteração direta.
* [x] Perfis de usuário controlados pelo próprio objeto.

### Composição

* [x] `Evento` possui uma `Programacao`.
* [x] `Programacao` mantém as atividades.
* [x] `Usuario` possui uma `Pessoa`.
* [x] Casos de uso recebem suas dependências por composição.

### Interfaces

* [x] `EventoRepository`
* [x] `UsuarioRepository`
* [x] `PresencaRepository`
* [x] `CodePass`
* [x] `TokenProvider`
* [x] `GeradorCodigoPresenca`
* [x] Portas de entrada para casos de uso.

### Inversão de dependência

* [x] Casos de uso dependem de interfaces.
* [x] Implementações JDBC ficam nos adaptadores.
* [x] `CompositionRoot` realiza a composição das dependências.

### Polimorfismo

* [~] Existem interfaces/estratégias reais, como `CodePass` e `TokenProvider`.
* [ ] Ainda falta demonstrar claramente um ponto de variação do **domínio de eventos** resolvido por polimorfismo, conforme esperado pelo ROO-05.

### Herança

* [x] Não foi utilizada herança artificial.
* [x] A solução prioriza composição.
* [x] A ausência de herança é compatível com a orientação do projeto quando composição é mais adequada.

---

# 🔐 Autenticação e autorização

* [x] Cadastro de usuário.
* [x] Login.
* [x] JWT.
* [x] BCrypt para armazenamento da senha.
* [x] Perfis `PARTICIPANTE`, `ORGANIZADOR` e `ADMINISTRADOR`.
* [x] Autorização verificada no backend.
* [x] Filtro de autenticação.
* [x] Interceptor de autorização.
* [x] Proteção das rotas administrativas/de organização.
* [x] Usuário autenticado identificado pelo JWT.
* [x] Hash de senha não é retornado nas respostas públicas.
* [x] Tratamento de sessão inválida.
* [ ] Recuperação de senha — fora do escopo obrigatório.
* [ ] Login social — fora do escopo obrigatório.
* [ ] MFA — fora do escopo obrigatório.

---

# 📋 Requisitos Funcionais

## RF-01 a RF-03 — Acesso e usuários

* [x] **RF-01** — Cadastro de usuário com nome, e-mail e senha.
* [x] **RF-02** — Autenticação e permissões por perfil.
* [~] **RF-03** — Consulta dos dados da própria conta implementada, porém atualização dos dados básicos ainda não está completa.

---

## RF-04 a RF-09 — Eventos, atividades e programação

* [~] **RF-04** — Criação, consulta e encerramento implementados; edição completa ainda não.
* [~] **RF-05** — Atividades podem ser cadastradas, porém não existe ainda modelagem completa de tipos configuráveis.
* [ ] **RF-06** — Trilhas/categorias/espaços e filtros de programação não estão implementados.
* [~] **RF-07** — Atividade é validada dentro do período do evento, mas não existe detecção completa de conflitos de local/horário.
* [ ] **RF-08** — Pessoas e papéis de palestrante/apresentador/responsável não estão implementados no fluxo real.
* [ ] **RF-09** — Filtros combináveis da programação não estão implementados.

---

## RF-10 a RF-15 — Site público e inscrições

* [x] **RF-10** — Catálogo público de eventos publicados.
* [~] **RF-11** — Programação básica é exibida, porém pessoas vinculadas ainda não estão integradas ao backend.
* [x] **RF-12** — Cadastro e inscrição básica pelo site.
* [ ] **RF-13** — Regras configuráveis de inscrição não implementadas.
* [ ] **RF-14** — Controle de vagas não implementado.
* [ ] **RF-15** — Cancelamento de inscrição não implementado.

---

## RF-16 a RF-18 — Agenda

* [ ] **RF-16** — Seleção de atividades.
* [ ] **RF-17** — Agenda pessoal persistida e cronológica.
* [ ] **RF-18** — Tratamento de conflitos de horários.

> A tela de agenda existe no frontend, porém atualmente apresenta dados demonstrativos e não constitui uma implementação integrada ao backend.

---

## RF-19 a RF-23 — Frequência

* [ ] **RF-19** — Políticas configuráveis de frequência.
* [x] **RF-20** — Geração de chamada temporária associada à atividade.
* [x] **RF-21** — Registro de presença por QR/código.
* [~] **RF-22** — Existe chamada por código/QR, mas lançamento e correção manual por organizador ainda não estão implementados.
* [ ] **RF-23** — Cálculo completo da situação de presença conforme política da atividade.

### Presença já implementada

* [x] Código temporário.
* [x] Validade de 5 minutos.
* [x] Hash do código no banco.
* [x] Revogação da chamada anterior.
* [x] Uma presença por participante/atividade.
* [x] Verificação de inscrição ativa.
* [x] Registro de data/hora.
* [x] Registro da origem.
* [x] Limitação de tentativas.
* [x] QR Code gerado no frontend.
* [x] Confirmação autenticada pelo JWT.

---

## RF-24 a RF-28 — Avaliações

* [ ] **RF-24** — Questionários configuráveis.
* [ ] **RF-25** — Texto, escolha única e escala numérica.
* [ ] **RF-26** — Restrição de avaliação por inscrição + presença.
* [ ] **RF-27** — Controle de resposta duplicada.
* [ ] **RF-28** — Consolidação dos resultados.

---

## RF-29 a RF-31 — Relatórios

* [ ] **RF-29** — Relatório de inscritos.
* [ ] **RF-30** — Relatório de frequência/participação.
* [ ] **RF-31** — Exportação CSV/PDF.

> A tela `ReportsView.vue` existe, mas os indicadores apresentados ainda são demonstrativos e não estão conectados aos dados reais do backend.

---

## RF-32 a RF-35 — Certificados

* [ ] **RF-32** — Critério de elegibilidade.
* [ ] **RF-33** — Geração de certificado PDF.
* [ ] **RF-34** — Envio de certificado por e-mail.
* [ ] **RF-35** — Certificado/declaração para palestrante ou apresentador.

Esses requisitos são classificados como **Desejáveis (D)** na especificação.

---

## RF-36 — Extensão social

* [ ] **RF-36** — Espaço de interação entre participantes.

Classificado como **Opcional/Plus (P)**.

---

# 📐 Regras de negócio

| Regra                                           | Situação |
| ----------------------------------------------- | -------- |
| RN-01 — E-mail único                            | [x]      |
| RN-02 — Fotografia opcional                     | [x]      |
| RN-03 — Evento com múltiplas atividades         | [~]      |
| RN-04 — Estado controla visibilidade            | [x]      |
| RN-05 — Inscrição configurável                  | [ ]      |
| RN-06 — Controle de vagas                       | [ ]      |
| RN-07 — Conflitos de atividades                 | [ ]      |
| RN-08 — Agenda derivada das escolhas            | [ ]      |
| RN-09 — Política de frequência por atividade    | [ ]      |
| RN-10 — QR sem senha/dado sensível              | [x]      |
| RN-11 — Data/hora/origem da frequência          | [x]      |
| RN-12 — Rastreabilidade de correções manuais    | [ ]      |
| RN-13 — Avaliação exige inscrição + presença    | [ ]      |
| RN-14 — Uma resposta por questionário           | [ ]      |
| RN-15 — Política de identificação das respostas | [ ]      |
| RN-16 — Critério de certificado                 | [ ]      |
| RN-17 — Preservação de histórico                | [~]      |
| RN-18 — Alterações restritas a autorizados      | [x]      |
| RN-19 — Banco compartilhado                     | [~]      |
| RN-20 — Fuso definido para o evento             | [ ]      |

---

# ⚙️ Requisitos Não Funcionais

| Requisito                                                | Situação |
| -------------------------------------------------------- | -------- |
| RNF-01 — Separação arquitetural                          | [x]      |
| RNF-02 — API/banco compartilhado entre canais            | [~]      |
| RNF-03 — Java no núcleo                                  | [x]      |
| RNF-04 — Banco relacional reproduzível                   | [x]      |
| RNF-05 — Senhas com hash                                 | [x]      |
| RNF-06 — Autorização no servidor                         | [x]      |
| RNF-07 — Validação de entradas                           | [x]      |
| RNF-08 — Privacidade                                     | [~]      |
| RNF-09 — Usabilidade                                     | [~]      |
| RNF-10 — Desempenho com 500 participantes/100 atividades | [ ]      |
| RNF-11 — Confiabilidade/idempotência                     | [~]      |
| RNF-12 — Testabilidade                                   | [x]      |
| RNF-13 — Manutenibilidade                                | [x]      |
| RNF-14 — Observabilidade                                 | [~]      |
| RNF-15 — Portabilidade                                   | [x]      |

### Atenção especial — RNF-02

A especificação prevê:

```text
Aplicação Desktop
       │
       ▼
     API
       │
       ▼
   PostgreSQL
       ▲
       │
Site público
```

No estado atual do repositório existe **API + frontend web**, mas não foi encontrada uma aplicação desktop integrada.

Portanto, esse requisito ainda deve ser tratado como pendência da entrega final.

---

# 🧪 Testes

O projeto possui estrutura de testes tanto no backend quanto no frontend.

## Backend

Existem testes para:

* domínio;
* usuário;
* evento;
* casos de uso;
* JWT;
* persistência JDBC;
* API HTTP;
* presença.

Arquivos relevantes:

```text
src/test/java/
├── EventoTest.java
├── UsuarioTest.java
├── EventosUseCaseTest.java
├── PresencaUseCaseTest.java
├── ApiWebIntegrationTest.java
├── JwtTokenProviderAdapterTest.java
├── JdbcEventoRepositoryTest.java
├── JdbcUsuarioRepositoryTest.java
└── JdbcPersistenciaIntegrationTest.java
```

* [x] Testes de domínio.
* [x] Testes de casos de uso.
* [x] Testes de autenticação.
* [x] Testes de API.
* [x] Testes de presença.
* [x] Testes de persistência.
* [~] Testes das regras centrais ainda precisam acompanhar todos os requisitos obrigatórios.

## Frontend

Existem testes para:

* autenticação;
* validação de cadastro;
* validação de login;
* eventos;
* filtros;
* normalização de texto;
* validação de evento;
* imagens;
* cliente HTTP;
* presença.

```text
frontend/src/
├── features/auth/**/*.spec.ts
├── features/events/**/*.spec.ts
├── features/media/**/*.spec.ts
└── tests/*.spec.ts
```

* [x] Testes unitários de frontend.
* [x] Testes de serviços.
* [x] Testes de validações.
* [x] Testes relacionados à presença.

### Observação sobre execução

O snapshot analisado contém os arquivos de testes, porém **não foi possível executar os testes Java nesta auditoria**, porque o Maven Wrapper tentou baixar o Maven e o ambiente não conseguiu acessar o repositório externo.

Da mesma forma, os testes do frontend não puderam ser executados no ambiente da auditoria porque as dependências executáveis do `node_modules` não estavam disponíveis no diretório extraído.

Portanto, o README não declara os testes como "executados agora"; apenas registra a existência dos testes no projeto.

---

# 🔌 API

## Autenticação

```text
POST /usuarios
POST /auth/login
GET  /usuarios/me
```

## Eventos

```text
GET  /eventos
GET  /eventos/{id}

GET  /usuarios/me/eventos
POST /eventos
POST /eventos/{id}/publicacao
POST /eventos/{id}/encerramento
```

## Inscrição e presença

```text
POST /eventos/{id}/inscricoes

GET  /eventos/{id}/atividades
POST /eventos/{id}/atividades

POST /atividades/{id}/chamadas
POST /presencas/confirmacoes
```

---

# 🗄️ Banco de dados

O projeto utiliza PostgreSQL.

### Migrações

```text
src/main/resources/db/migration/

V1__cria_modelo_inicial.sql
V2__inscricoes_chamadas_presencas.sql
V3__perfis_de_contas_autenticadas.sql
```

### Principais tabelas

* `usuarios`
* `usuario_perfis`
* `eventos`
* `atividades`
* `inscricoes`
* `chamadas_presenca`
* `presencas`
* `limites_presenca`
* `versoes_schema`

### Situação

* [x] Banco relacional.
* [x] Script inicial.
* [x] Migração de inscrições/presença.
* [x] Migração de perfis.
* [x] Constraints de integridade.
* [x] Chaves estrangeiras.
* [x] Índices.
* [x] Restrições de unicidade.
* [x] Controle de concorrência em operações de presença.

---

# 🔒 Segurança

* [x] BCrypt para senhas.
* [x] JWT para autenticação.
* [x] Autorização no backend.
* [x] Código de presença armazenado como hash.
* [x] QR Code não contém senha.
* [x] Não retorna hash de senha nas respostas.
* [x] Limitação de tentativas de presença.
* [x] SQL parametrizado.
* [x] Controle de propriedade do evento.
* [~] Limites operacionais gerais e proteção contra abuso ainda podem ser ampliados.

---

# 🎯 Cenários de Aceitação

## CA-01 — Publicação

* [x] Criar evento.
* [x] Publicar evento.
* [x] Evento publicado aparece no catálogo público.
* [x] Evento encerrado deixa de aparecer no catálogo.

**Situação:** `[~]`

A parte principal está integrada, porém cadastro completo de atividades e pessoas ainda não cobre todo o cenário original.

---

## CA-02 — Inscrição

* [x] Usuário cadastrado.
* [x] Login.
* [x] Evento publicado.
* [x] Inscrição persistida.
* [x] Não duplica inscrição ativa.

**Situação:** `[x]` para a inscrição básica.

---

## CA-03 — Agenda

* [ ] Seleção de atividades.
* [ ] Detecção de conflito.
* [ ] Agenda persistida.
* [ ] Agenda cronológica real.

**Situação:** `[ ]`

---

## CA-04 — Frequência QR

* [x] Organizador gera chamada.
* [x] QR Code é exibido.
* [x] Código possui validade.
* [x] Participante precisa estar autenticado.
* [x] Participante precisa possuir inscrição ativa.
* [x] Presença é persistida.
* [x] Duplicidade é impedida.

**Situação:** `[x]` para o fluxo implementado.

---

## CA-05 — Frequência manual

* [ ] Lançamento manual.
* [ ] Correção manual.
* [ ] Autoria da operação.
* [ ] Rastreabilidade.

**Situação:** `[ ]`

---

## CA-06 — Avaliação

* [ ] Questionário.
* [ ] Elegibilidade.
* [ ] Presença validada.
* [ ] Bloqueio de usuário inelegível.
* [ ] Resposta persistida.

**Situação:** `[ ]`

---

## CA-07 — Relatório

* [ ] Consulta de inscrições.
* [ ] Consulta de frequência.
* [ ] Consolidação.
* [ ] Exportação.

**Situação:** `[ ]`

---

# 🧩 Requisitos Técnicos de POO — ROO

| ROO    | Requisito                | Situação                    |
| ------ | ------------------------ | --------------------------- |
| ROO-01 | Modelo de domínio        | [x]                         |
| ROO-02 | Encapsulamento           | [x]                         |
| ROO-03 | Objetos de valor         | [~]                         |
| ROO-04 | Composição e colaboração | [x]                         |
| ROO-05 | Polimorfismo             | [~]                         |
| ROO-06 | Herança                  | [x] — composição priorizada |
| ROO-07 | Interfaces               | [x]                         |
| ROO-08 | SOLID                    | [~]                         |
| ROO-09 | Arquitetura hexagonal    | [x]                         |
| ROO-10 | Padrões de projeto       | [~]                         |
| ROO-11 | Erros do domínio         | [x]                         |
| ROO-12 | Testes e refatoração     | [~]                         |

### Pontos que precisam ser fortalecidos

Os principais pontos de POO que ainda precisam de evidência mais clara são:

1. **Polimorfismo no domínio**
2. **Objetos de valor**
3. **Pelo menos dois padrões de projeto documentados com problema → solução → efeito**
4. **Duas refatorações relevantes documentadas**
5. **Evidência de SOLID antes/depois**
6. **Rastreabilidade RF → ROO → classe → teste**

---

# 🧱 Padrões de projeto

Já existem indícios claros dos seguintes padrões:

### Adapter

Usado nos adaptadores JDBC e de infraestrutura.

Exemplos:

```text
UsuarioRepository
       ▲
       │
JdbcUsuarioRepository
```

```text
TokenProvider
       ▲
       │
JwtTokenProviderAdapter
```

### Strategy

As interfaces como:

```text
CodePass
TokenProvider
```

permitem trocar a implementação utilizada pelo caso de uso.

### Situação

* [x] Adapter.
* [x] Strategy.
* [ ] Documentar formalmente dois padrões com problema, alternativa descartada e efeito.
* [ ] Demonstrar um padrão adicional diretamente relacionado às políticas configuráveis do domínio.

---

# 📚 Documentação existente

```text
docs/
├── api/
│   ├── contratos-webapp.md
│   ├── integracao-webapp.md
│   └── presenca.md
│
├── backlog/
│   └── backlog.md
│
├── decisoes/
│   ├── decisao-001-modelagem.md
│   ├── decisao-002-frequencia.md
│   ├── decisao-003-questionario.md
│   ├── decisao-004-autenticacao-autorizacao.md
│   └── decisoes-semana-1.md
│
├── dominio/
│   ├── glossario.md
│   ├── mapa-responsabilidades.md
│   └── modelo-dominio.md
│
└── termo-de-abertura/
    └── termo-de-abertura.md
```

### Documentação

* [x] Termo de abertura.
* [x] Glossário.
* [x] Mapa de responsabilidades.
* [x] Modelo de domínio.
* [x] Backlog.
* [x] Documentação da API.
* [x] Documentação da presença.
* [x] Decisão de autenticação.
* [x] Decisão de frequência.
* [~] D-01 a D-08 completos.
* [ ] Matriz final RF × ROO.
* [ ] Registro de duas refatorações relevantes.
* [ ] Diagrama arquitetural final atualizado.
* [ ] Evidências finais de demonstração.

> Observação: `decisao-001-modelagem.md` e `decisao-003-questionario.md` existem no repositório, mas estão vazios no snapshot analisado. Portanto, não devem ser considerados documentação concluída.

---

# 🐳 Execução com Docker

O projeto possui:

```text
Dockerfile
docker-compose.yml
.env.example
deploy/
```

O Compose prepara:

```text
PostgreSQL
    │
    ▼
API Java
    │
    ▼
Frontend Vue/Nginx
```

Execução:

```bash
cp .env.example .env
docker compose up --build -d
```

Verificar:

```bash
docker compose ps
docker compose logs -f
```

Parar:

```bash
docker compose down
```

---

# 📁 Estrutura do projeto

```text
EventsByMc-main/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   │
│   └── test/
│
├── frontend/
│   └── src/
│
├── db/
│
├── docs/
│
├── deploy/
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# 🚧 Pendências prioritárias para a entrega final

## 🔴 Prioridade 1 — Requisitos obrigatórios ausentes

* [ ] Agenda real do participante.
* [ ] Seleção de atividades.
* [ ] Conflito de horários.
* [ ] Controle de vagas.
* [ ] Cancelamento de inscrição.
* [ ] Política de inscrição configurável.
* [ ] Política de frequência.
* [ ] Lançamento manual de presença.
* [ ] Correção manual de presença.
* [ ] Avaliações.
* [ ] Questionários.
* [ ] Relatórios.
* [ ] Exportação de relatório.
* [ ] Programação com trilhas/categorias.
* [ ] Pessoas e papéis vinculados às atividades.

## 🔴 Prioridade 2 — Exigência arquitetural

* [ ] Aplicação desktop integrada à API.
* [ ] Demonstrar que desktop e site utilizam a mesma API/banco.

## 🟠 Prioridade 3 — Evidências de POO II

* [ ] Polimorfismo real no domínio.
* [ ] Objetos de valor.
* [ ] Dois padrões documentados formalmente.
* [ ] Duas refatorações documentadas.
* [ ] Evidência de SOLID antes/depois.
* [ ] Matriz RF × ROO.

## 🟡 Prioridade 4 — Documentação final

* [ ] Completar D-01.
* [ ] Completar D-03.
* [ ] Completar D-05.
* [ ] Completar D-06.
* [ ] Completar D-07.
* [ ] Completar D-08.
* [ ] Atualizar modelo de domínio.
* [ ] Atualizar mapa de responsabilidades.
* [ ] Criar/atualizar diagrama arquitetural.
* [ ] Preparar base de dados para demonstração.
* [ ] Preparar roteiro CA-01 a CA-07.

---

# 🧪 Checklist de definição de pronto

Para considerar um requisito concluído:

* [ ] Implementado.
* [ ] Integrado à API.
* [ ] Persistido no banco.
* [ ] Regra de negócio protegida no backend/domínio.
* [ ] Caminho principal testado.
* [ ] Pelo menos um erro relevante tratado.
* [ ] Documentado.
* [ ] Demonstrável.
* [ ] Relacionado a uma responsabilidade de objeto adequada.

---

# 🎓 Checklist final da disciplina

## Modelagem orientada a objetos — 30%

* [x] Domínio modelado.
* [x] Responsabilidades distribuídas.
* [x] Encapsulamento.
* [x] Composição.
* [~] Polimorfismo.
* [~] Objetos de valor.
* [ ] Modelo final completo conforme todos os requisitos.

## Arquitetura e qualidade — 20%

* [x] Separação domínio/aplicação/infraestrutura.
* [x] Portas e adaptadores.
* [x] Inversão de dependência.
* [x] Autorização no servidor.
* [~] SOLID documentado.
* [~] Padrões de projeto documentados.

## Testes e evolução — 10%

* [x] Testes de domínio.
* [x] Testes de aplicação.
* [x] Testes de API.
* [x] Testes frontend.
* [~] Refatorações documentadas.
* [ ] Evidência final de evolução antes/depois.

## Atendimento funcional e integração — 20%

* [x] API.
* [x] PostgreSQL.
* [x] Frontend.
* [x] Cadastro.
* [x] Login.
* [x] Eventos.
* [x] Inscrição básica.
* [x] Presença QR/código.
* [ ] Agenda.
* [ ] Avaliações.
* [ ] Relatórios.
* [ ] Desktop.

## Processo incremental — 15%

* [x] Backlog.
* [x] Documentação inicial.
* [x] Incrementos implementados.
* [~] Rastreabilidade das entregas semanais.
* [ ] Evidência final consolidada.

## Documentação e comunicação — 5%

* [x] README.
* [x] Glossário.
* [x] Mapa de responsabilidades.
* [x] Modelo de domínio.
* [x] Documentação da API.
* [~] Decisões D-01 a D-08.
* [ ] Matriz RF × ROO.
* [ ] Registro final de refatorações.

---

# 🚀 Roteiro sugerido para a demonstração final

### 1. Problema

Apresentar em aproximadamente dois minutos:

> O Events by MC centraliza o gerenciamento de eventos acadêmicos e profissionais, permitindo organizar eventos, atividades, inscrições e presença, utilizando uma arquitetura orientada a objetos.

### 2. Fluxo funcional

Demonstrar:

```text
Cadastro
   ↓
Login
   ↓
Organizador
   ↓
Criar evento
   ↓
Publicar
   ↓
Site público
   ↓
Participante
   ↓
Inscrição
   ↓
Atividade
   ↓
QR Code
   ↓
Presença
```

### 3. POO

Demonstrar:

* `Evento`
* `Atividade`
* `Programacao`
* `Usuario`
* interfaces
* casos de uso
* repositórios
* composição
* encapsulamento
* padrões de projeto

### 4. Arquitetura

Mostrar:

```text
Frontend
   ↓
HTTP Handler
   ↓
Use Case
   ↓
Domain
   ↓
Repository Port
   ↓
JDBC
   ↓
PostgreSQL
```

### 5. Testes

Executar alguns testes de:

* Evento;
* Usuário;
* Caso de uso;
* Presença;
* API.

### 6. Refatoração

Apresentar:

```text
ANTES
↓
Problema encontrado
↓
Decisão de modelagem
↓
DEPOIS
↓
Menor acoplamento / maior coesão
```

---

# 📌 Estado final do snapshot analisado

### Já existe uma base real e integrada

* [x] Java 21.
* [x] API HTTP.
* [x] PostgreSQL.
* [x] Vue 3.
* [x] Autenticação.
* [x] Autorização.
* [x] Eventos.
* [x] Catálogo público.
* [x] Inscrição básica.
* [x] Atividades.
* [x] QR Code.
* [x] Presença.
* [x] Testes.
* [x] Docker.
* [x] Documentação arquitetural inicial.

### Ainda não caracteriza o núcleo completo da especificação

* [ ] Agenda.
* [ ] Conflitos.
* [ ] Vagas.
* [ ] Políticas configuráveis.
* [ ] Frequência manual.
* [ ] Avaliações.
* [ ] Relatórios.
* [ ] Exportação.
* [ ] Pessoas/papéis.
* [ ] Trilhas/categorias.
* [ ] Aplicação desktop.
* [ ] Matriz RF × ROO.
* [ ] D-01 a D-08 completos.
* [ ] Refatorações finais documentadas.

---

# 👨‍💻 Equipe

**Events by MC**

Projeto Integrador — Programação Orientada a Objetos II

2026
