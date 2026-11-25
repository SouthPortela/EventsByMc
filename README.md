# EventsByMc

Plataforma acadêmica de gestão de eventos com programação, inscrições, agenda,
frequência por QR Code ou lançamento manual, avaliações, relatórios e certificados.
Projeto da disciplina de Programação Orientada a Objetos II.

## Equipe

- Brandon Handes Nogueira da Silva
- Brendha Alves dos Santos
- Gabriel Pereira Brito
- Luciano Cassimiro De Godoi Filho
- Marcos Marques Portela

## Stack e arquitetura

- Backend: Java 21, HttpServer do JDK, Maven, JDBC e PostgreSQL 16.
- Autenticação: JWT com JJWT e senhas com BCrypt.
- Frontend: Vue 3, TypeScript, Vite, Pinia, Vue Router e Bootstrap.
- Desktop Windows: Electron, utilizando o mesmo frontend e a mesma API.
- Testes: JUnit 5, Vitest, node:test e validação SQL com PGlite.

O backend não utiliza Spring Boot. Domínio e aplicação permanecem independentes
dos adaptadores HTTP/JDBC; as implementações são ligadas na CompositionRoot.

| Diretório | Responsabilidade |
|---|---|
| src/main/java/.../domain | Entidades, objetos de valor e regras de domínio |
| src/main/java/.../application | Casos de uso e portas de entrada/saída |
| src/main/java/.../adapter/out/jdbc | Persistência PostgreSQL |
| src/main/java/.../infrastructure | HTTP, segurança e composição |
| src/main/resources/db/migration | Migrações SQL versionadas |
| src/test/java | Testes Java |
| frontend/src | Interface web compartilhada com o desktop |
| desktop | Aplicativo Electron e seus testes/scripts |
| db | Inicialização, atualização, mocks e testes SQL |
| docs | Contratos, domínio e decisões |
| deploy | Referência da implantação Apache/systemd |

O backend fica na raiz do repositório; não existe uma pasta backend separada.

## Funcionalidades atuais

- Edição de cadastro e eventos; conflitos de espaço/horário, retificações manuais
  com histórico e relatórios detalhados por atividade (schema 11).
- Cadastro como participante ou organizador, login e consulta da própria conta.
- Catálogo, criação de eventos, categoria, publicação e encerramento.
- Programação com trilhas, espaços, pessoas, papéis e capacidade por atividade.
- Inscrições com janela, limite e cancelamento; agenda com vagas e conflitos.
- QR/código temporário, confirmação manual, entrada/saída e permanência mínima;
  [acesso ao QR pelo celular na rede local](docs/api/presenca-rede.md).
- Questionários gerais ou por atividade, respostas e resultados.
- Frequência individual, relatórios JSON/CSV/PDF e certificados PDF.
- Envio individual de certificado por SMTP quando configurado.
- Mensagens entre inscritos e consulta por organizador/administrador.
- Administração real: contas/perfis, visão global, bloqueio de eventos e auditoria.

Conclusão é calculada pela frequência: inscrição ativa, evento encerrado, ao menos
uma atividade obrigatória e percentual mínimo atingido. Não há um campo de conclusão
no cadastro do usuário. Consulte as regras nos contratos abaixo.

## Executar com Docker

Instale Docker e Docker Compose. Na raiz, copie .env.example para .env e preencha
as credenciais e o segredo JWT localmente; não versione esses valores.

    docker compose up -d --build

O site usa a porta WEB_PORT (80 por padrão). Java e PostgreSQL têm portas
publicadas somente em loopback. O frontend Nginx encaminha /api para o Java.

Banco vazio recebe as migrações até a versão lógica 11. Volume existente exige
atualização manual: siga [db/README.md](db/README.md). Não remova o volume para migrar.

## Executar Java sem Docker

Use JDK 21 e o Maven Wrapper (no Linux/macOS, substitua mvnw.cmd por ./mvnw).
Configure DB_URL, DB_USERNAME, DB_PASSWORD e JWT_SECRET no ambiente do processo.
JWT_EXPIRATION_MINUTES e SERVER_PORT são opcionais. O JAR não carrega .env sozinho.

    .\mvnw.cmd clean install
    java -jar target/gestao-eventos-0.0.1-SNAPSHOT.jar

A porta Java padrão é 8080. Prepare primeiro o PostgreSQL conforme o guia do banco.

## Frontend em desenvolvimento

Requer Node.js ^22.18.0 ou >=24.12.0. Na raiz:

    npm --prefix frontend install
    npm --prefix frontend run dev

O Vite usa /api e encaminha para http://127.0.0.1:8080 por padrão. Para outro destino,
configure frontend/.env.local conforme [frontend/README.md](frontend/README.md).

## Desktop Windows

O package.json da raiz pertence ao Electron. npm install na raiz não instala as
dependências do frontend. Siga [desktop/README.md](desktop/README.md) para preparar,
executar e gerar o instalador. O aplicativo exige conexão com a API; não há modo offline.

## Banco e demonstração

O primeiro administrador é concedido pelo banco a uma conta já cadastrada.
Depois, administradores podem gerenciar perfis na aplicação, preservando ao menos
um administrador. As alterações são auditadas.

A carga demonstrativa é explícita: db/Popular-Demo.ps1 executa DemoDataSeeder e cria
100 participantes, 20 organizadores, 50 eventos e 100 atividades. Depois,
db/mocks.sql complementa inscrições, agenda, presenças, avaliações e certificados.
Nada disso é executado automaticamente ao iniciar a API. Use banco de desenvolvimento.

## Validação

    .\mvnw.cmd test
    npm test
    npm run lint
    npm --prefix frontend run test:run
    npm --prefix frontend run lint
    npm --prefix frontend run build

npm test e npm run lint na raiz validam o desktop. O lint atual do frontend usa
correção automática; para apenas verificar, rode os comandos ESLint/Oxlint descritos
no README do frontend. Não há Checkstyle configurado no Maven.

Os testes Java de integração com PostgreSQL são condicionais às variáveis de banco.
Quando habilitados, criam e removem dados: use uma instância dedicada a testes.
Para os testes SQL efêmeros, consulte [db/README.md](db/README.md).

Resultado desta revisão: [validação de 25/11/2026](docs/testes/validacao.md), com
verificações aprovadas e limitações do ambiente.

## Documentação

- [Banco e mocks](db/README.md)
- [Frontend](frontend/README.md)
- [Desktop](desktop/README.md)
- [Contratos iniciais](docs/api/contratos-webapp.md)
- [Inscrições e presença](docs/api/presenca.md)
- [Programação, agenda, avaliações e certificados](docs/api/requisitos-funcionais-webapp.md)
- [Administração](docs/api/administracao.md)
- [Edição e histórico — migração 11](docs/api/edicao-historico.md)
- [Modelo de domínio](docs/dominio/modelo-dominio.md)
- [Responsabilidades](docs/dominio/mapa-responsabilidades.md)
- [Backlog](docs/backlog/backlog.md)
- [Deploy Apache/systemd](deploy/DEPLOY.md)

O roteiro integracao-webapp.md e decisoes-semana-1.md são registros históricos;
não devem substituir os contratos atuais. O PDF de requisitos e o termo de abertura
registram o escopo acadêmico; não são uma declaração de que todo requisito está entregue.

## Limitações e manutenção

Continuam fora da implementação: upload/persistência de imagens, exclusão de eventos e atividades,
edição geral de atividades, moderação/paginação de mensagens,
envio em massa de certificados e sincronização offline. Categoria, políticas e
metadados de programação possuem operações próprias documentadas.

O módulo de upload sem integração foi removido. Uma futura implementação precisará
de contrato HTTP, persistência e validação no servidor, além da interface.

Dependências, backups, configurações locais e saídas de build não devem ser versionados.
Preserve package-lock.json e frontend/package-lock.json; reinstale dependências com npm.
