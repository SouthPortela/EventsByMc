# Integração implementada do webapp

> Este é o registro da primeira etapa de integração. Para o estado posterior
> com programação, agenda, políticas de frequência, avaliações, relatórios,
> certificados e interação, consulte
> [requisitos-funcionais-webapp.md](requisitos-funcionais-webapp.md).

Atualização de presença: inscrição básica, criação/listagem de atividades e confirmação
por QR/código agora possuem implementação. Veja os contratos em
[presenca.md](presenca.md) e os scripts em [db/README.md](../../db/README.md).
As limitações descritas ao final deste documento refletem a entrega anterior de
conta/eventos; o guia de presença atualiza especificamente esses novos fluxos.

Atualizado em 24/09/2026. Este documento descreve a integração inicial; o roteiro
`integracao-webapp.md` foi preservado como histórico didático.

## O que está conectado

Vue 3 e Bootstrap continuam responsáveis pelas telas, sem recarregar a página
durante a navegação. O backend continua em Java 21, com HttpServer do JDK, sem Spring.
Não foram adicionadas dependências, tabelas, credenciais ou configurações de implantação;
a migração V4 acrescenta a coluna `categoria` à tabela de eventos.

| Tela/operação | Requisição feita pelo navegador | Acesso |
|---|---|---|
| Cadastro | POST /api/usuarios | Público; contrato anterior preservado |
| Login | POST /api/auth/login | Público; contrato anterior preservado |
| Minha conta | GET /api/usuarios/me | Qualquer usuário autenticado |
| Minha participação | GET /api/usuarios/me/participacao | Inscrições, agenda e presenças da própria conta |
| Catálogo | GET /api/eventos | Público; apenas PUBLICADO |
| Detalhes | GET /api/eventos/{id} | Público; apenas PUBLICADO |
| Meus eventos | GET /api/usuarios/me/eventos | Organizador/admin; somente eventos próprios |
| Salvar rascunho | POST /api/eventos | Organizador; categoria opcional para clientes antigos |
| Alterar categoria | PATCH /api/eventos/{id}/categoria | Dono organizador ou administrador |
| Publicar | POST /api/eventos/{id}/publicacao | Dono organizador ou administrador |
| Encerrar | POST /api/eventos/{id}/encerramento | Dono organizador ou administrador |

O Java recebe os caminhos sem `/api`. O proxy local do Vite remove esse prefixo.
IDs de usuário, evento e atividade são UUIDs, representados por strings no Vue.
Links antigos com IDs numéricos dos mocks não representam eventos reais.
Se não houver eventos publicados no banco, o catálogo estará vazio; não há fallback
para dados fictícios quando a API falha.

## Como os arquivos colaboram

1. `frontend/src/views/EventFormView.vue` captura e valida os campos; mostra erro
   ou estado de salvamento e impede envios repetidos enquanto há uma requisição.
2. `frontend/src/features/events/services/eventoService.ts` conhece endpoints
   e transforma o JSON da API nos tipos usados pelas telas. Os componentes não
   precisam conhecer detalhes da resposta Java.
3. `frontend/src/shared/services/httpClient.ts` centraliza fetch, cabeçalhos e
   erros. O trabalho já iniciado nesse arquivo e no `main.ts` foi aproveitado.
   `autenticada: true` envia Bearer; consultas públicas não enviam o token.
4. `RotasApi` registra os endpoints e `Router` reconhece parâmetros de caminho.
   `AutenticacaoFiltro` valida JWT; `GestaoEventosHandler` exige perfil e converte
   a entrada HTTP para dados do caso de uso.
5. `EventosUseCase` valida os dados, consulta o usuário atual no banco e verifica
   a propriedade do evento. A autorização não depende dos botões visíveis no Vue.
6. `EventoRepository` é a porta de saída; `JdbcEventoRepository` usa SQL
   parametrizado no PostgreSQL. `CompositionRoot` conecta as implementações.
7. `DadosEvento` e `DadosMinhaConta` selecionam os campos de saída. Objetos
   completos de usuário e hashes de senha nunca são serializados nessas respostas.
8. Após a resposta, o Vue atualiza suas referências reativas. A confirmação de
   publicação só aparece depois de resposta positiva do servidor.

`AccountView.vue` usa o `contaService.ts` que já havia sido criado. A rota
`/conta` aceita qualquer conta autenticada; o endereço anterior
`/participante/conta` redireciona para ela. VISITANTE representa a navegação anônima.

## Contratos dos eventos

Criação: enviar `Content-Type: application/json` e Bearer; resposta `201`.
Os valores abaixo são apenas exemplos:

```json
{
  "titulo": "Simpósio de Tecnologia",
  "descricao": "Encontro com palestras sobre desenvolvimento de software.",
  "local": "Auditório principal",
  "dataInicio": "2026-10-10T09:00",
  "dataFim": "2026-10-10T18:00",
  "categoria": "TECNOLOGIA"
}
```

- Título: 5–200 caracteres; descrição: 20–10.000; local: 1–200.
- Datas obrigatórias; término posterior ao início. Frontend e backend validam.
- Texto é aparado nas extremidades. Vue exibe os dados por interpolação escapada;
  não usar `v-html` para descrição ou outros conteúdos recebidos.
- Corpo de criação limitado a 64 KiB; conteúdo inválido retorna `400`.
- Categoria: `TECNOLOGIA`, `CURSOS_E_WORKSHOPS`, `NEGOCIOS_E_CARREIRAS`,
  `ACADEMICO` ou `OUTROS`. Clientes antigos que a omitem recebem `OUTROS`.
  O formulário atual exige uma categoria válida e os cartões/filtros usam o valor
  persistido, nunca uma inferência pelo banner ou título.
- Não enviar organizador, estado, capacidade, preço ou banner.
  O organizador vem do JWT validado e o estado inicial é RASCUNHO.
- As datas usam `LocalDateTime`/TIMESTAMP, sem fuso no contrato atual.
  O frontend não adiciona um deslocamento fictício. Uma política explícita de
  fuso do evento ainda é necessária para usuários em diferentes fusos.

Resposta da criação, consulta e mudança de estado:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "titulo": "Simpósio de Tecnologia",
  "descricao": "Encontro com palestras sobre desenvolvimento de software.",
  "local": "Auditório principal",
  "dataInicio": "2026-10-10T09:00:00",
  "dataFim": "2026-10-10T18:00:00",
  "estado": "RASCUNHO",
  "categoria": "TECNOLOGIA",
  "atividades": []
}
```

Listas retornam arrays desses objetos. Atividades existentes retornam somente
`id`, `titulo`, `local`, `dataInicio` e `dataFim`; a tela apresenta esse cronograma,
mas ainda não cria ou edita atividades pela API.

Para classificar um evento já existente, o painel envia
`PATCH /api/eventos/{id}/categoria` com `{"categoria":"ACADEMICO"}` e Bearer. A resposta
`200` tem o mesmo formato de evento. Rascunhos e eventos já publicados podem ser
reclassificados sem alterar o estado. A categoria é validada no servidor, e a
alteração exige propriedade do evento ou perfil de administrador. Após a V4 do banco,
eventos antigos ficam em `OUTROS` até serem classificados pelo organizador.

Publicação/encerramento não precisam de corpo e retornam `200`.
O ciclo é RASCUNHO → PUBLICADO → ENCERRADO. Encerrados saem do catálogo público.
Repetir o estado atual é aceito; transição inválida retorna `409`.
A atualização compara o estado anterior no SQL: uma disputa entre requisições
retorna `409`, sem sobrescrever uma alteração concorrente.

Consultar rascunho/encerrado pelo endpoint público retorna `404`, inclusive para
o dono. Para acompanhar esses registros, use a lista autenticada de eventos próprios.
Um administrador pode alterar o estado de qualquer evento, mas sua lista “Meus
eventos” continua sendo pessoal. A visão global e a moderação estão documentadas em
[administracao.md](administracao.md).

## Conta, autenticação e erros

`GET /api/usuarios/me` retorna `usuarioId`, `nome`, `email` e `perfis`.
O ID vem do token e os dados vêm da consulta atual ao banco. Conta removida retorna
`401`. Respostas privadas bem-sucedidas possuem `Cache-Control: no-store`.

O cadastro mantém resposta `200 text/plain`; login mantém `usuarioID` e os demais
campos existentes. Não confundir `usuarioID` do login com `usuarioId` da conta.

| Status | Significado no fluxo |
|---|---|
| 400 | Dados/JSON/UUID inválidos |
| 401 | Token ausente, inválido, expirado ou conta removida |
| 403 | Perfil insuficiente ou evento de outro organizador |
| 404 | Recurso inexistente ou evento fora do catálogo público |
| 405 | Método incorreto para rota reconhecida, após a autenticação quando exigida |
| 409 | Transição inválida ou conflito concorrente |
| 500 | Falha interna; mensagem genérica, sem detalhes de banco |

Erros retornam `{ mensagem, instante }`. Em 401 de uma requisição protegida,
o cliente encerra a sessão e encaminha para login. Um 403 não encerra a sessão.
Um 401 de login não limpa outra sessão; uma resposta antiga também não limpa
uma sessão cujo token já mudou. Uma página HTML devolvida por proxy incorreto
é rejeitada, em vez de tratada como JSON de eventos.

O cadastro público atribui PARTICIPANTE por padrão. O campo opcional `perfil`
aceita `PARTICIPANTE` ou `ORGANIZADOR`; outros valores retornam 400, inclusive
`VISITANTE` e `ADMINISTRADOR`. Para conceder administrador, use o procedimento
controlado em [Banco e chamada de presença](../../db/README.md).
Após mudar um perfil no banco, faça login novamente para obter um JWT atualizado.

## Executar e conferir localmente

1. Use Java 21 e configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET`
   no processo que inicia a API. Não coloque esses valores no frontend.
2. Prepare um PostgreSQL de desenvolvimento com o script já existente
   `src/main/resources/db/migration/V1__cria_modelo_inicial.sql`. Ele não foi alterado;
   não há execução automática de migrações na inicialização. Não reaplique scripts
   indiscriminadamente em um banco com dados.
3. Inicie `EventsApiApplication` pela IDE usando Java 21 ou gere o JAR com
   `mvn package` e execute-o com `java -jar target/gestao-eventos-0.0.1-SNAPSHOT.jar`.
   Porta padrão: 8080; opcionalmente configure `SERVER_PORT`.
4. No frontend, mantenha `VITE_API_BASE_URL=/api`; execute `npm run dev`.
   O proxy aponta por padrão para `http://127.0.0.1:8080`.
5. Para outra porta/destino, configure `API_PROXY_TARGET` no `.env.local` do
   frontend. Se o destino já atende sob `/api`, configure também
   `API_PROXY_KEEP_PREFIX=true`. Reinicie o Vite após mudar essas variáveis.
   Essas duas opções são do servidor de desenvolvimento, não segredos nem
   configuração de implantação.
6. Cadastre-se, faça login e confira Minha conta. Com uma conta autorizada como
   organizador, crie um rascunho, confira a lista, publique e abra o catálogo.
   Encerre e confira que ele deixa de aparecer publicamente.

No navegador, a aba Network permite acompanhar método, caminho e status.
Nunca compartilhe capturas contendo o cabeçalho Authorization ou dados pessoais.
Uma falha de rede ao criar pode ocorrer depois do salvamento; confira “Meus eventos”
antes de reenviar. Ainda não existe chave de idempotência para criação.

## Testes e limitações

Resultado desta validação: 42 testes frontend aprovados; build e lint frontend
aprovados. No Java, 35 testes aprovados e 6 testes dependentes de banco ignorados
(41 descobertos, nenhuma falha na execução final). Também passaram 3 cenários SQL.

- `npm run test:run`: cliente HTTP, mapeamento dos eventos, criação, publicação,
  encerramento, validação e regressões dos utilitários.
- `npm run build`: compilação e verificação de tipos do Vue.
- Oxlint e ESLint foram executados sem correção automática global, para preservar
  mudanças que já existiam no repositório.
- `mvn test`: regras de conta/eventos e testes HTTP com servidor/JWT reais e
  repositórios em memória. Os testes existentes que exigem PostgreSQL ficam
  ignorados sem variáveis de banco.
- Nesta máquina, o JDK falhou ao abrir sockets no diretório temporário padrão.
  A execução passou usando somente nos testes
  `-DargLine=-Djdk.net.unixdomain.tmpdir=C:\Users\marcos.portela\IdeaProjects\events-api\target`.
  Não foi alterada configuração global do Java; em outro ambiente esse ajuste
  pode não ser necessário.
- `mvn checkstyle:check` não pôde executar: o plugin não está configurado/disponível
  no cache local. Não foi adicionada dependência apenas para mudar a infraestrutura.
- Ainda é necessária validação ponta a ponta com Vue, Java e PostgreSQL de
  desenvolvimento juntos. Não foi feita implantação nem alteração de dados reais.

Inscrição, atividades e presença por QR/código foram integradas posteriormente;
consulte [o contrato atualizado](presenca.md). O módulo do participante mostra
inscrições, agenda e presenças reais. Avaliações, relatórios, administração global,
edição de conta, imagens no banco e edição de eventos continuam pendentes.
Os painéis ainda demonstrativos exibem um aviso. Não foi implementado bucket S3.

Também permanecem como evolução: paginação/filtros no servidor (a busca do catálogo
continua local), otimização das consultas de listagem, limites de requisição e
proteção operacional do login, política de fuso, idempotência de criação e validação
de concorrência/perfis em cenários mais amplos. Não considerar esta entrega uma
certificação de prontidão para produção.
