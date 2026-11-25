# Edição, ocupação de espaços e histórico de frequência

Estas funcionalidades exigem schema 11. Os caminhos abaixo são internos ao Java;
o frontend usa o prefixo /api. Todas as rotas novas exigem JWT válido.
Não foi aplicada migração ao banco do usuário nesta implementação.

## Atualizar o banco

Faça backup e confirme que as versões anteriores já foram aplicadas. Na raiz:

    psql -X -h localhost -U SEU_USUARIO -d SEU_BANCO -W -v ON_ERROR_STOP=1 -f db/atualizar-v11.sql

A entrada usa V9b__edicao_frequencia_historico.sql, mantendo a ordem alfabética do
init Docker. Banco vazio recebe a versão 11 por db/inicializar.sql ou init do Docker.
Não remova volumes nem reaplique migrações já registradas.

A migração acrescenta ajustes_frequencia, historico_eventos, a view
frequencias_efetivas e proteções transacionais. Não apaga presenças, inscrições ou
certificados antigos e não exige extensão/biblioteca adicional. Eventos já encerrados
passam a preservar a configuração existente; não se reconstrói histórico retroativo.
Conflitos antigos de programação não são corrigidos automaticamente.

## Conta e evento

| Método e caminho | Acesso / entrada |
|---|---|
| PUT /usuarios/me | Própria conta; nome e email |
| GET /eventos/{id}/gestao | Dono organizador/admin; inclusive rascunho |
| PUT /eventos/{id} | Dono organizador/admin; mesmos campos da criação |
| GET /eventos/{id}/historico | Dono organizador/admin; snapshots antes/depois |

Editar conta não altera senha ou perfis e não aceita outro usuário no caminho/body.
E-mail duplicado retorna 409. O próximo login usa o novo e-mail; o token atual continua
identificando a mesma conta pelo UUID. O cliente atualiza seus dados exibidos na sessão.

Evento editável deve estar em RASCUNHO ou PUBLICADO. O novo período precisa abranger
todas as atividades. Não se modifica o estado por edição, nem os campos específicos
das atividades. A edição aparece em Meus eventos e na lista administrativa.

## Ocupação de espaços

A criação de atividades verifica conflitos no domínio e no PostgreSQL. A proteção
SQL serializa alterações da programação pelo evento, evitando que requisições
concorrentes confirmem ocupação sobreposta do mesmo espaço. Vale para ambas as rotas
de criação de atividade, inclusive clientes antigos.

O identificador do espaço e o nome normalizado (ignorando caixa/espaços repetidos)
identificam o local dentro do evento. Horários usam intervalo semiaberto: uma atividade
pode começar quando outra termina. Atividades simultâneas em espaços distintos são
permitidas. Não há cadastro global de instalações compartilhadas entre eventos.

## Lançamento alternativo e retificações

| Método e caminho | Finalidade |
|---|---|
| POST /atividades/{id}/frequencia/ajustes | Confirma ou desconsidera presença com justificativa |
| GET /atividades/{id}/frequencia/ajustes | Histórico de ajustes da atividade |

Corpo: id (UUID da solicitação), usuarioId, confirmada (booleano) e motivo (5 a 1000
caracteres). O responsável é obtido do JWT; o horário vem do banco. Repetir o mesmo
id com os mesmos dados retorna o ajuste existente. Reutilizá-lo com outros dados ou
outro responsável retorna 409.

A operação é restrita ao dono organizador/admin e a inscrições ativas em evento
PUBLICADO ou ENCERRADO. Funciona em qualquer política, inclusive CHECKIN_UNICO.
Não cria QR, não altera a política e não apaga marcações anteriores. O último ajuste
é a decisão efetiva da presença; outro ajuste justificado pode revertê-lo.

A confirmação pode conceder presença apesar de marcações incompletas; desconsiderar
retira sua validade. É uma decisão manual explícita e auditável. QR posterior não
anula silenciosamente a retificação. O fluxo normal de entrada/saída continua disponível
para eventos publicados. Correções após encerramento usam somente ajustes justificados.

## Relatórios e consolidação

- GET /eventos/{id}/relatorios/atividades: detalhes por participante/atividade.
- GET /eventos/{id}/relatorios/atividades.csv: exportação de todos esses detalhes.

Acesso de dono organizador/admin. O resultado distingue inscrição ATIVA/CANCELADA,
reserva pessoal, presença efetiva, política, entrada, saída, origem e responsável/motivo
da correção. Sem seleção de atividade não é apresentado como reserva. Filtros da tela
são locais; o CSV exporta o evento completo. Inscrições canceladas preservam histórico,
mas não qualificam avaliações ou certificados.

Relatórios consolidados, participação individual, elegibilidade das avaliações e
certificados usam a mesma fonte de presença efetiva. Respostas/certificados já
registrados não são apagados por uma retificação; a elegibilidade atual passa a
considerar a decisão corrigida.

Após encerramento (inclusive bloqueio com estado anterior ENCERRADO), não é permitido
alterar título, descrição, período, local, categoria ou frequência mínima; a programação
fica imutável. Bloquear/desbloquear continua possível e preserva o estado anterior.
Alterações de evento são registradas com antes/depois; edição e política feitas pelas
novas operações registram também o responsável. Escritas diretas/legadas podem aparecer
sem responsável nesse histórico, e bloqueios/perfis mantêm sua auditoria administrativa.

## Testes e validação

- Java: EdicaoHistoricoUseCaseTest e ApiWebIntegrationTest verificam autorização,
  cadastro, edição, regra de ocupação, período, congelamento e ajustes justificados.
- Frontend: edicaoHistorico.spec.ts verifica autenticação, contratos, repetição de
  solicitação e propagação de conflitos.
- SQL: db/tests/edicao-historico.mjs verifica espaço, histórico, imutabilidade,
  frequência efetiva e vínculos, com banco efêmero preparado conforme db/README.md.

    node --test db/tests/schema.mjs db/tests/edicao-historico.mjs

Nesta revisão: 61 testes Java, 56 frontend e 7 desktop aprovados. Sete testes Java
condicionais de banco ficaram ignorados. A suíte SQL não pôde executar: PGlite ausente,
registry.npmjs.org indisponível por DNS e acesso à API Docker negado neste ambiente.
Isso exige validação SQL em ambiente de desenvolvimento antes de implantar.
