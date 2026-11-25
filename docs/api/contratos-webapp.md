# Contratos atuais do webapp

O frontend web e Electron consome a mesma API Java 21 com HttpServer do JDK.
No navegador, o prefixo externo é /api; o proxy o remove ao encaminhar ao Java.
Os caminhos das tabelas abaixo são internos, sem esse prefixo.

## Conta e eventos

| Método | Caminho | Acesso e resultado |
|---|---|---|
| POST | /usuarios | Cadastro público, resposta textual compatível |
| POST | /auth/login | Login público; preserva o campo usuarioID |
| GET | /usuarios/me | JWT; consulta a própria conta, sem hash de senha |
| GET | /eventos | Catálogo público de publicados |
| GET | /eventos/{id} | Detalhe público de evento publicado |
| GET | /usuarios/me/eventos | Dono organizador ou administrador; lista pessoal |
| POST | /eventos | Organizador/admin; cria rascunho |
| PATCH | /eventos/{id}/categoria | Dono organizador/admin; altera categoria |
| POST | /eventos/{id}/publicacao | Dono organizador/admin; publica rascunho |
| POST | /eventos/{id}/encerramento | Dono organizador/admin; encerra publicado |

IDs são UUIDs. O cadastro aceita PARTICIPANTE por padrão ou ORGANIZADOR por escolha
explícita; não aceita ADMINISTRADOR ou VISITANTE. VISITANTE é navegação anônima.
Perfis podem ser múltiplos e menus não substituem autorização no servidor.

O evento recebe titulo, descricao, dataInicio, dataFim, local e categoria.
Categoria é TECNOLOGIA, CURSOS_E_WORKSHOPS, NEGOCIOS_E_CARREIRAS, ACADEMICO ou OUTROS.
Clientes antigos podem omiti-la na criação; o formulário atual pede seleção explícita.
Datas do cronograma usam LocalDateTime; ainda não há política explícita de fuso por
evento. Início/fim devem ser coerentes, e publicação exige período e local.

## Demais contratos

- [Presença e inscrições](presenca.md): chamadas temporárias, confirmação e participação.
- [Programação e funcionalidades](requisitos-funcionais-webapp.md): atividades, vagas,
  trilhas, espaços, pessoas, agenda, questionários, frequência, relatórios e certificados.
- [Administração](administracao.md): contas/perfis, eventos globais, bloqueios e auditoria.

Programação e administração estão conectadas à API real. A lista administrativa é
global; Meus eventos permanece pessoal. Evento bloqueado fica fora do catálogo e
preserva o histórico e seu estado anterior.

## Cliente HTTP e segurança

httpClient.ts recebe funções de acesso à sessão; serviços não importam diretamente
a store. Chamadas autenticadas enviam Bearer. A store usa sessionStorage e o token
é consultado no momento da requisição. Uma resposta antiga não deve encerrar uma
sessão criada posteriormente. Erro 403 não encerra automaticamente uma sessão válida.

| Status | Significado geral |
|---|---|
| 400 | Entrada inválida |
| 401 | Sessão ausente, inválida ou expirada |
| 403 | Falta de permissão |
| 404 | Recurso inexistente ou não disponível no contexto público |
| 409 | Conflito de estado, unicidade ou concorrência |
| 429 | Limite de tentativas de presença |
| 500 | Erro interno genérico |

Erros usam mensagem e instante. Respostas privadas usam no-store. O frontend
renderiza conteúdo informado pelo usuário como texto. JWT, senhas e códigos não
devem aparecer em logs. QR não contém token JWT de login.

## Configuração e limitações

A API não carrega .env automaticamente fora do Docker. Consulte
[execução](../../README.md), [frontend](../../frontend/README.md) e
[banco](../../db/README.md). O schema deve estar na versão lógica 11.

Há edição de conta e evento, relatórios por atividade e ajustes auditáveis conforme
[edição e histórico](edicao-historico.md). Não há upload de imagens, exclusão geral
de evento/atividade ou chave de idempotência para criação de evento. Depois de falha de rede ao criar,
consulte o painel antes de reenviar. SMTP requer configuração no backend.

Testes disponíveis: serviços e validações em Vitest, domínio/casos de uso/HTTP em
JUnit e SQL efêmero em db/tests/schema.mjs. A aprovação automatizada não substitui
conferência integrada com PostgreSQL, celular, SMTP e ambiente de implantação.
