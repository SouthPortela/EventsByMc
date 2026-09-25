# Events by MC

Plataforma de gestão de eventos acadêmicos e profissionais desenvolvida como Projeto Integrador de **Programação Orientada a Objetos II**. Reúne catálogo, programação, inscrições, agenda, frequência, avaliações, relatórios, certificados e interação entre participantes.

**Status: em desenvolvimento.** Há fluxos implementados no repositório, mas a validação ponta a ponta com PostgreSQL real, SMTP e celular ainda precisa ser concluída. Este README atualiza o panorama do Markdown de referência, que descreve uma etapa anterior do projeto.

## Equipe

| Integrante | Papel |
|---|---|
| Brandon Handes | Desenvolvimento |
| Brendha Alves | Desenvolvimento |
| Gabriel Brito | Desenvolvimento |
| Luciano Godoi | Desenvolvimento |
| Marcos Portela | Desenvolvimento |

## Tecnologias e arquitetura

| Camada | Tecnologias principais |
|---|---|
| Backend | Java 21, servidor HTTP do JDK (HttpServer), JDBC, Jackson, JJWT, BCrypt, Maven e JUnit 5 |
| Frontend | Vue 3, TypeScript, Vite, Vue Router, Pinia, Bootstrap, Vitest, ESLint e Oxlint |
| Dados e execução | PostgreSQL, Docker Compose e Nginx |

**O backend não utiliza Spring Boot.** Ele segue a separação por portas e adaptadores:

```text
Vue SPA ──HTTP/JSON──► Nginx ou proxy Vite ──► rotas/handlers HTTP
                                                   │
                                                   ▼
                                            casos de uso ──► domínio
                                                   │
                                                   ▼
                                           portas de saída ──► JDBC ──► PostgreSQL
```

```text
src/main/java/br/com/eventsbymc/eventsapi/
├── domain/model/                 # Entidades, estados e regras
├── application/port/in/          # Operações oferecidas
├── application/port/out/         # Contratos com serviços externos
├── application/usecase/          # Casos de uso
├── adapter/out/jdbc/             # Persistência
└── infrastructure/
    ├── adapter/in/web/           # Rotas, handlers e autenticação
    └── config/                   # CompositionRoot

frontend/src/
├── features/                      # Serviços, tipos e componentes por função
├── shared/                        # Cliente HTTP e recursos comuns
├── views/                         # Telas
└── router/                        # Navegação SPA e guardas
```

O navegador acessa a API em `/api`, nunca o banco diretamente. O cliente HTTP do Vue centraliza requisições e envia JWT nas operações protegidas. O backend verifica identidade, perfil e propriedade dos recursos: esconder botões no frontend não substitui autorização. O domínio Java não depende do servidor HTTP ou de anotações de persistência; o `CompositionRoot` conecta casos de uso às implementações das portas.

## Requisitos funcionais do webapp

**Implementado** significa que há fluxo de API/interface no repositório, não homologação em produção. **Parcial** indica uma parte ainda pendente.

| Requisitos | Situação |
|---|---|
| RF-01–02 — Cadastro, autenticação e perfis | Implementado: JWT e perfis PARTICIPANTE, ORGANIZADOR e ADMINISTRADOR. VISITANTE é o estado anônimo da interface. |
| RF-03 — Conta | Parcial: consulta da própria conta; atualização de dados ainda incompleta. |
| RF-04–05 — Eventos e atividades | Parcial: criação, consulta, publicação, encerramento e cadastro de atividades; edição/exclusão completas e tipos configuráveis faltam. |
| RF-06, RF-08–09 — Programação | Implementado: categorias, trilhas, espaços, pessoas/papéis e filtros combináveis. |
| RF-07 — Restrições de programação | Parcial: validação de período existe; choque de local/horário ainda não é tratado integralmente. |
| RF-10–12 — Site público e inscrição | Implementado: catálogo, detalhes, programação e inscrição autenticada. |
| RF-13–15 — Regras, vagas e cancelamento | Implementado: janela e abertura de inscrições, limite opcional por evento/atividade e cancelamento. Sem limite por padrão. |
| RF-16–18 — Agenda | Implementado: seleção persistida, ordem cronológica e bloqueio de conflito entre escolhas do participante. |
| RF-19–21, RF-23 — Frequência | Implementado: políticas por atividade, QR/código temporário e cálculo da situação de frequência. Mínimo do evento configurável, padrão de 75%. |
| RF-22 — Presença manual | Parcial: marcação pelo organizador com data/hora e autoria; correção posterior ainda falta. |
| RF-24–28 — Avaliações | Implementado: questionários por evento/atividade, texto, escolha única e escala, elegibilidade, unicidade e resultados. |
| RF-29–31 — Relatórios | Implementado: inscritos e frequência em JSON, CSV e PDF. |
| RF-32–35 — Certificados | Implementado: elegibilidade, PDF, declaração para palestrante/apresentador e envio individual quando SMTP está configurado. |
| RF-36 — Interação | Versão inicial implementada: mensagens por evento para participantes ativos; moderação e paginação faltam. |

As políticas de frequência disponíveis são CHECKIN_UNICO, VALIDACAO_MANUAL, ENTRADA_SAIDA e PERCENTUAL_PERMANENCIA. A permanência mínima por atividade é configurável, com padrão de 75%. A avaliação de uma atividade exige presença válida **na mesma atividade**. O QR abre a página de confirmação, mas não confirma a presença automaticamente: o participante precisa autenticar-se, estar inscrito e enviar a confirmação. A chamada vence em cinco minutos; apenas o hash do código fica no banco. O QR compartilhado, por si só, não comprova presença física.

Veja os [contratos atuais dos requisitos](docs/api/requisitos-funcionais-webapp.md), o [contrato de presença](docs/api/presenca.md) e os [contratos iniciais da integração](docs/api/contratos-webapp.md). O último registra uma fase anterior; consulte os contratos atuais para os fluxos mais novos.

## API e banco de dados

Exemplos de rotas com o prefixo `/api` usado pelo navegador:

| Fluxo | Rotas |
|---|---|
| Conta | `POST /api/usuarios`, `POST /api/auth/login`, `GET /api/usuarios/me` |
| Eventos/programação | `GET /api/eventos`, `POST /api/eventos`, `GET /api/eventos/{id}/programacao` |
| Inscrição/agenda | `POST /api/eventos/{id}/inscricoes`, `GET /api/usuarios/me/agenda`, `POST /api/atividades/{id}/agenda` |
| Frequência | `POST /api/atividades/{id}/chamadas`, `POST /api/presencas/confirmacoes` |
| Avaliações/relatórios | `GET /api/eventos/{id}/questionario`, `GET /api/eventos/{id}/relatorios/frequencia` |
| Certificados/interação | `GET /api/eventos/{id}/certificados/me.pdf`, `GET /api/eventos/{id}/mensagens` |

Os detalhes de métodos, filtros e permissões estão na documentação da API. O prefixo `/api` é tratado pelo proxy; as rotas Java são registradas sem ele.

O PostgreSQL usa scripts **V1–V10** em `src/main/resources/db/migration/`, abrangendo usuários, eventos, inscrições, presença, programação, agenda, avaliações, mensagens, certificados e moderação. A API **não migra o banco ao iniciar**. Um volume novo no Docker recebe V1–V10; `db/inicializar.sql` prepara um banco vazio fora do Compose. Em um banco existente, faça backup, consulte `versoes_schema` e aplique apenas os scripts `db/atualizar-vN.sql` pendentes, em ordem. Reiniciar o Compose não atualiza um volume já inicializado.

O [guia do banco](db/README.md) detalha a atualização e a criação controlada do primeiro administrador. O cadastro público nunca concede ADMINISTRADOR; após mudar um perfil no banco, faça login novamente para receber um JWT atualizado.

O painel administrativo consulta eventos de todos os organizadores, usuários com e-mails mascarados e decisões de moderação. O administrador pode suspender, restaurar como rascunho ou excluir logicamente eventos com motivo obrigatório; a exclusão remove o conteúdo textual, mas mantém inscrições, presenças e certificados vinculados. Consulte [administração da API](docs/api/administracao.md). O backend ainda não persiste banners/fotos de eventos; não há mídia armazenada para remover nesta versão.

## Executar com Docker

Copie `.env.example` para `.env` e troque **a senha do banco e o segredo JWT** antes de iniciar. Não use os valores de exemplo em um ambiente exposto.

```powershell
Copy-Item .env.example .env
docker compose config
docker compose up --build -d
docker compose ps
```

O site fica em `http://localhost` por padrão; `WEB_PORT` altera essa porta. API e PostgreSQL são publicados apenas em `127.0.0.1` para diagnóstico local. O Nginx entrega a SPA e encaminha `/api` à API pela rede interna.

```powershell
docker compose logs -f
docker compose down
```

**Não execute `docker compose down -v` para atualizar o projeto:** isso remove o volume e apaga o banco. Para um volume existente, siga [db/README.md](db/README.md). O envio de certificados exige SMTP_HOST, SMTP_PORT (TLS implícito, porta 465 por padrão), SMTP_USERNAME, SMTP_PASSWORD e SMTP_FROM no processo da API. Essas variáveis não são fornecidas automaticamente pelo Compose; sem SMTP, o download do PDF continua disponível.

Há também um [guia separado de implantação com Apache e HTTPS](deploy/DEPLOY.md). Ele descreve um servidor diferente do ambiente local com Docker; confira a configuração efetiva antes de usar o domínio público.

## Desenvolvimento e testes

Sem Docker, use Java 21, PostgreSQL preparado conforme o [guia do banco](db/README.md) e Node.js compatível com `frontend/package.json` (`^22.18.0` ou `>=24.12.0`). Configure DB_URL, DB_USERNAME, DB_PASSWORD e JWT_SECRET no backend. No frontend, mantenha `VITE_API_BASE_URL=/api`; o Vite encaminha a API local normalmente para `127.0.0.1:8080`.

```powershell
./mvnw.cmd test
./mvnw.cmd package
cd frontend
npm install
npm run dev
npm run test:run
npm run build
```

Para lint sem correção automática, execute `npm exec -- oxlint .` e `npm exec -- eslint .` dentro de `frontend/`; `npm run lint` aplica correções. Há testes de esquema em `db/tests/schema.mjs`; o [guia do banco](db/README.md) explica a dependência temporária necessária.

Na validação registrada em 24/09/2026, os testes de frontend e SQL passaram, e a suíte Java terminou sem falhas, com seis testes dependentes de PostgreSQL ignorados por falta de banco de teste. O pacote Java foi gerado. Isso **não substitui** testes integrados com PostgreSQL real, SMTP e QR no celular. Nesta máquina Windows, testes HTTP Java precisaram de um ajuste local do diretório temporário do JDK, descrito no guia do banco. O Compose não foi executado nessa validação.

## Pendências e documentação

- Concluir edição/exclusão de eventos e atividades, atualização da conta, conflitos completos de programação e correção auditável de presença manual.
- Salvar e servir imagens pelo banco, se esse recurso continuar no escopo. Não há integração com bucket S3.
- Definir fuso horário por evento e ampliar validação de concorrência, desempenho e proteção operacional do login/cadastro.
- Evoluir mensagens com moderação/paginação e validar todos os fluxos com dados reais.
- Completar evidências de POO II: polimorfismo no domínio, objetos de valor, padrões e refatorações documentados, e rastreabilidade entre requisitos, classes e testes.

Documentos de apoio: [termo de abertura](docs/termo-de-abertura/termo-de-abertura.md), [glossário](docs/dominio/glossario.md), [modelo de domínio](docs/dominio/modelo-dominio.md), [mapa de responsabilidades](docs/dominio/mapa-responsabilidades.md), [backlog](docs/backlog/backlog.md) e [decisões arquiteturais](docs/decisoes/decisoes-semana-1.md).

**Prazo acadêmico informado:** primeira semana de outubro de 2026.
