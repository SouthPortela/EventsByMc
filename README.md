# Events by MC

## Plataforma de Gestão de Eventos

Projeto Integrador desenvolvido para a disciplina de Programação Orientada a Objetos II.

## Equipe

| Integrante | Papel |
|---|---|
| Brandon Handes | Desenvolvimento |
| Brendha Alves | Desenvolvimento |
| Gabriel Brito | Desenvolvimento |
| Luciano Godoi | Desenvolvimento |
| Marcos Portela | Desenvolvimento |

## Objetivo

Desenvolver uma plataforma de gestão de eventos capaz de centralizar o gerenciamento de eventos, atividades, inscrições, programação, frequência, avaliações e relatórios.

O projeto terá como foco a aplicação dos princípios de Programação Orientada a Objetos, incluindo encapsulamento, composição, polimorfismo, interfaces, SOLID, padrões de projeto, testes e arquitetura organizada.

## Status

🚧 Em desenvolvimento

## Integração do webapp

Inscrição básica, cadastro de atividades e chamada de presença por QR/código também
foram implementados. Antes de usar esses fluxos, inicialize ou atualize o banco
seguindo [Banco e chamada de presença](db/README.md). Nenhum script é aplicado
automaticamente pela API.

A base atual usa Java 21, servidor HTTP do JDK, JDBC/PostgreSQL e Vue com Bootstrap.
Cadastro e login foram preservados. Conta autenticada, catálogo público, detalhes,
lista de eventos do organizador, criação de rascunhos, publicação e encerramento
agora possuem endpoints e consumo real no Vue, sem fallback para eventos simulados.
Consulte os [contratos, execução local e limitações](docs/api/contratos-webapp.md).
O [roteiro didático anterior](docs/api/integracao-webapp.md) foi mantido como histórico;
o documento de contratos descreve o estado implementado.

## Execução dos três serviços com Docker

O Docker Compose prepara PostgreSQL, API Java e frontend Vue/Nginx. Copie
`.env.example` para `.env`, substitua a senha do banco e o segredo JWT e execute:

```bash
docker compose up --build -d
```

Por padrão, o site fica disponível em `http://localhost`. `WEB_PORT` pode mudar
essa porta. PostgreSQL e API são publicados somente em `127.0.0.1`, para diagnóstico
local; entre os contêineres, a comunicação ocorre pela rede interna do Compose.

```bash
docker compose ps
docker compose logs -f
docker compose down
```

Os scripts SQL em `src/main/resources/db/migration` são executados automaticamente
somente quando o volume `pg_data` está vazio. Em um banco já inicializado, faça
backup e aplique a atualização indicada em [Banco e chamada de presença](db/README.md).
Não remova o volume para atualizar um banco que contenha dados.

### Semana 1 — Domínio, responsabilidades e backlog

- [x] Repositório criado
- [x] README inicial
- [ ] Termo de Abertura
- [ ] Glossário do domínio
- [ ] Mapa de responsabilidades
- [ ] Modelo inicial de domínio
- [ ] Backlog
- [ ] Decisões arquiteturais iniciais

## Disciplina

Programação Orientada a Objetos II

## Prazo

Primeira semana de outubro de 2026.
