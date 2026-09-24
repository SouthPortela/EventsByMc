# API de inscrições, atividades e presença

Os caminhos abaixo são internos ao Java. O navegador usa o prefixo /api via proxy.
Todas as rotas exigem JWT válido; o código da chamada não substitui o token.
As tabelas da V2 são pré-requisito. Consulte [preparação do banco](../../db/README.md).

| Método | Caminho | Acesso / resultado |
|---|---|---|
| POST | /eventos/{id}/inscricoes | Usuário autenticado; 200 com inscrição ativa, sem duplicar |
| GET | /usuarios/me/participacao | Usuário autenticado; inscrições, agenda e total de presenças da própria conta |
| GET | /eventos/{id}/atividades | Dono organizador/admin; 200 com atividades |
| POST | /eventos/{id}/atividades | Dono organizador/admin; 201 com atividade criada |
| POST | /atividades/{id}/chamadas | Dono organizador/admin; 201 com chamada temporária |
| POST | /presencas/confirmacoes | Usuário autenticado inscrito; 200 com confirmação |

Inscrição e geração de chamada não precisam de body.
Criação de atividade e confirmação exigem application/json, com limite de 64 KiB.

`GET /usuarios/me/participacao` usa exclusivamente a identidade do JWT validado;
não recebe ID de usuário enviado pelo navegador. A resposta inclui `inscricoes`
(ativas ou canceladas, com título, período, local e estado do evento), `atividades`
dos eventos com inscrição ativa (com data de presença, quando houver) e
`totalPresencas` do histórico. Não fornece e-mail de terceiros nem códigos de QR.
Atividades sem horário podem ter `dataInicio` e `dataFim` nulos. A agenda é o
cronograma dos eventos inscritos; ainda não existe reserva por atividade.

Exemplo de nova atividade:

```json
{
  "titulo": "Palestra de abertura",
  "descricao": "Apresentação da programação.",
  "local": "Auditório",
  "dataInicio": "2026-10-10T09:00",
  "dataFim": "2026-10-10T10:00"
}
```

Título/local têm até 200 caracteres; descrição até 10.000. Datas são obrigatórias
e devem estar dentro do período do evento. O contrato de agenda ainda usa
LocalDateTime, sem política explícita de fuso, como os endpoints anteriores.
Não existe edição/exclusão de atividade nem reserva de vaga nesta entrega.

Resposta da geração:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "atividade": "Palestra de abertura",
  "codigo": "ABCDEFGHJKMN",
  "expiraEm": "2026-10-10T12:05:00Z"
}
```

Código e UUID acima são exemplos. O código só é devolvido na emissão. O banco guarda
seu hash; não existe endpoint para recuperar o texto do código. Gere outra chamada
se perder a tela. Uma nova emissão revoga a anterior.

O participante envia:

```json
{ "codigo": "ABCD-EFGH-JKMN", "origem": "CODIGO" }
```

Espaços, hífens e diferenças entre maiúsculas/minúsculas são normalizados.
Origem também aceita QR; é informativa, não uma garantia de leitura pela câmera.
Não enviar usuarioId, inscrição, evento ou timestamp: o servidor determina os vínculos,
a identidade pelo JWT e o instante pelo relógio do banco.

Resposta:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "atividade": "Palestra de abertura",
  "registradaEm": "2026-10-10T12:02:00Z",
  "jaRegistrada": false
}
```

Repetir com chamada válida e inscrição ainda ativa retorna a presença anterior
com jaRegistrada=true. Não cria outra marcação nem altera a data original.
Código expirado/revogado continua sendo recusado, mesmo que já exista presença.

Erros usam o contrato anterior {mensagem, instante}:

- 400: JSON/código inválido, expirado, revogado ou dados inválidos.
- 401: sessão ausente/inválida/expirada ou conta removida.
- 403: falta de autorização para gerenciar evento/atividade.
- 404: evento ou atividade inexistente.
- 409: evento não publicado/encerrado, inscrição não ativa ou conflito concorrente.
- 429: mais de 10 tentativas por minuto por conta/operação; Retry-After: 60.
- 500: erro interno/banco; sem expor SQL ou credenciais.

## Arquivos e responsabilidades

- PresencaUseCase: coordena identidade, autorização, atividade, código e porta de persistência.
- RegrasChamada: janela e validação da expiração, sem dependências de framework.
- CodigoPresencaSeguro: SecureRandom, normalização e hash, implementando uma porta.
- JdbcPresencaRepository: operações atômicas, SQL parametrizado, bloqueios e unicidade.
- PresencaHandler / RotasApi: conversão entre HTTP e casos de uso; filtro JWT já existente.
- presencaService.ts: endpoints tipados do frontend.
- AttendanceManagementView.vue: seleção de evento/atividade, criação de atividade e QR.
- ConfirmAttendanceView.vue: recepção do código, login sem propagar código na query e POST explícito.
- EventDetailsView.vue: inscrição real antes da confirmação de presença.
- ParticipantDashboardView.vue, ParticipantRegistrationsView.vue e AgendaView.vue:
  usam a consulta autenticada da participação, sem dados de demonstração.

Não há acoplamento com Spring nem imagens de QR armazenadas no banco. O QR é
apenas a representação visual de um link contendo o código temporário.
