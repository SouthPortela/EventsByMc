# Requisitos funcionais — contratos do webapp

Esta página descreve as APIs adicionadas após o contrato inicial em
`contratos-webapp.md`. O servidor continua em Java 21 com `HttpServer`, portas e
adaptadores JDBC; **não** usa Spring Boot. Todas as rotas abaixo, exceto a
programação publicada, exigem JWT `Authorization: Bearer`. O frontend usa
`/api` como base e o proxy/Nginx o encaminha ao backend.

## Programação, inscrição e agenda (RF-06, 08, 09, 13–18)

| Rota | Acesso e finalidade |
|---|---|
| `GET /eventos/{id}/programacao` | Pública para evento publicado; filtros combináveis `trilhaId`, `espacoId`, `tipo`, `de`, `ate` |
| `GET /eventos/{id}/programacao/gestao` | Dono organizador/admin; inclui rascunhos |
| `GET/PUT /eventos/{id}/politica` | Dono/admin; abertura, janela, limite, cancelamento e percentual mínimo |
| `GET/POST /eventos/{id}/trilhas`, `/espacos`, `/pessoas` | Dono/admin; metadados da programação |
| `POST /eventos/{id}/programacao` | Dono/admin; atividade com vagas, trilha, espaço, pessoas e política de frequência |
| `POST /eventos/{id}/atividades/{atividadeId}/pessoas` | Dono/admin; papel `PALESTRANTE`, `APRESENTADOR` ou `RESPONSAVEL` |
| `POST /eventos/{id}/inscricoes` | Conta autenticada; respeita janela e limite; idempotente se ativa |
| `DELETE /eventos/{id}/inscricoes/me` | Inscrito; cancela e remove seleções da agenda |
| `GET /usuarios/me/agenda` | Agenda cronológica persistida |
| `POST/DELETE /atividades/{id}/agenda` | Seleção/removal; exige inscrição ativa, vaga e ausência de conflito |

Inscrições são ilimitadas por padrão (`limite_inscritos=NULL`). O organizador pode
definir limite por evento e por atividade. Reservas da agenda usam a capacidade
da atividade; se ela não for informada, herda a capacidade do espaço. O servidor
serializa as inscrições no evento e a seleção na atividade para não vender a última
vaga duas vezes.

## Frequência (RF-19, 23)

Cada atividade pode usar `CHECKIN_UNICO`, `VALIDACAO_MANUAL`, `ENTRADA_SAIDA` ou
`PERCENTUAL_PERMANENCIA`. O check-in único mantém QR/código de cinco minutos.
As demais políticas usam `GET/POST /atividades/{id}/frequencia`, apenas para
dono organizador/admin, com corpo `{"usuarioId":"UUID","marcacao":"ENTRADA"}`
(ou `SAIDA`/`CONFIRMACAO`). A marcação registra horário e responsável; não aceita
duplicidade e exige inscrição ativa. Para permanência, a duração entre entrada
e saída deve atingir o percentual configurado da duração da atividade (75% por
padrão). O relatório calcula o número de atividades obrigatórias, válidas, o
percentual do participante e a situação final. O mínimo do evento é 75% por
padrão, configurável em `/politica`.

## Avaliações (RF-24–28)

Há um questionário geral opcional por evento e um por atividade. O organizador
cria o título, adiciona questões de `TEXTO`, `ESCOLHA_UNICA` ou `ESCALA` e publica
quando estiver pronto. Questionário publicado fica imutável. O prefixo para o
geral é `/eventos/{id}`; para o específico, `/atividades/{id}`:

| Sufixo | Acesso |
|---|---|
| `GET /questionario` | Conta autenticada; só publicado |
| `GET /questionario/gestao` | Dono/admin; inclui rascunho |
| `POST /questionario` | Dono/admin; cria título |
| `POST /questionario/questoes` | Dono/admin; adiciona questão |
| `POST /questionario/publicacao` | Dono/admin; publica |
| `POST /avaliacoes` | Inscrito com presença válida; uma resposta por questionário |
| `GET /avaliacoes/me` | Informa se a própria conta já respondeu |
| `GET /avaliacoes/resultados` | Dono/admin; contagens, distribuição e textos |

Uma avaliação de atividade exige frequência válida **na mesma atividade**. A
unicidade também é garantida no banco para impedir resposta dupla concorrente.

## Relatórios e certificados (RF-29–35)

- `GET /eventos/{id}/relatorios/inscritos` e `/frequencia`: JSON, dono/admin.
- Os mesmos caminhos com `.csv` ou `.pdf`: download autorizado. O CSV escapa
  fórmulas para evitar execução acidental em planilhas.
- `GET /eventos/{id}/frequencia/me`: situação do próprio participante.
- `GET /eventos/{id}/certificados/me.pdf`: certificado do participante, apenas
  após encerramento, inscrição ativa e frequência mínima atendida.
- `POST /eventos/{id}/certificados/me/envio`: envia o PDF por SMTP configurado.
- `GET /eventos/{id}/certificados/pessoas/{pessoaId}/{papel}/pdf` e o mesmo caminho
  terminando em `/envio` com `POST`: declaração do palestrante/apresentador,
  acessível ao dono/admin após encerramento.

As emissões são únicas por destinatário/evento/tipo e podem ser baixadas
novamente. O envio é individual e síncrono; `enviado_em` só é marcado depois
que o servidor SMTP aceita a mensagem. Configure `SMTP_HOST`, `SMTP_PORT`
(465 por padrão), `SMTP_USERNAME`, `SMTP_PASSWORD` e `SMTP_FROM` **somente no
ambiente do backend**. Sem essas variáveis, o download continua disponível;
enviar por e-mail retorna erro de configuração. O adaptador suporta SMTP com TLS
implícito, não STARTTLS na porta 587.

## Interação (RF-36)

`GET/POST /eventos/{id}/mensagens` exibe/publica as 50 mensagens mais recentes.
Leitura é restrita a participante ativo, dono organizador ou admin; publicação
exige inscrição ativa. O frontend renderiza texto com interpolação Vue, sem HTML
injetado. Moderação, paginação e notificações estão fora deste fluxo inicial.

## Banco e testes

Um banco vazio recebe V1–V9 automaticamente pelo Compose; um volume existente
**não recebe migrações novas automaticamente**. Faça backup, confira
`versoes_schema` e execute, em ordem, os scripts `db/atualizar-v5.sql` a
`db/atualizar-v9.sql` que ainda faltarem. Consulte `db/README.md`.

Verifique com `node --test db/tests/schema.mjs`, `mvn test` e, em `frontend/`,
`npm run test:run`, `npm exec -- oxlint .`, `npm exec -- eslint .` e
`npm run build`. Neste Windows, os testes HTTP precisaram de
`-DargLine=-Djdk.net.unixdomain.tmpdir=C:\Users\marcos.portela\IdeaProjects\events-api\target`;
o resultado foi 51 testes Java descobertos, sem falhas, 6 condicionais ignorados.
Os fluxos de SMTP e QR em celular também pedem
validação manual no ambiente onde serão usados.
