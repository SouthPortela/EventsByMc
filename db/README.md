# Banco e chamada de presença

## Estado atual

A V1 tinha quatro tabelas: usuários, perfis, eventos e atividades. A V2 acrescenta
a base de inscrição e presença acordada: uma confirmação por atividade, inscrição
ativa no evento e chamada válida por cinco minutos.
A V3 converte contas antigas que tinham somente VISITANTE para PARTICIPANTE e
remove VISITANTE de contas organizadoras/administradoras. VISITANTE passa a existir
somente como estado anônimo no frontend, sem linha em `usuario_perfis`.
A V4 acrescenta uma categoria explícita aos eventos. Registros antigos permanecem
em `OUTROS` até serem classificados pelo organizador no painel; banners/imagens e
palavras do título não são usados para adivinhar essa informação.
A V5 modela trilhas, espaços, pessoas/papéis, agenda e regras de inscrição.
A V6 adiciona questionários, respostas e mensagens; a V7 persiste certificados.
A V8 configura a política de frequência por atividade e armazena marcações manuais
auditáveis. A V9 permite questionários gerais e por atividade no mesmo evento.

| Tabela nova | Responsabilidade |
|---|---|
| inscricoes | Vínculo único entre usuário e evento, com estado e datas |
| chamadas_presenca | Chamada de uma atividade, emissor, hash do código, validade e revogação |
| presencas | Uma marcação por usuário/atividade, ligada à inscrição e à chamada corretas |
| limites_presenca | Contador persistente de tentativas por usuário/operação |
| versoes_schema | Registro das versões aplicadas manualmente |
| trilhas, espacos, pessoas_evento, atividade_pessoas | Programação e papéis |
| agenda_atividades | Seleção pessoal de atividades, com vínculo à inscrição |
| questionarios, questoes, avaliacoes, respostas_avaliacao | Avaliações configuráveis |
| mensagens_evento | Conversas dos participantes de um evento |
| certificados | Emissão única e histórico de envio |
| registros_frequencia | Confirmação manual, entrada e saída por atividade |

Chaves estrangeiras compostas impedem associar presença, inscrição e chamada de
eventos diferentes. UNIQUE protege contra duplicidade, inclusive se o navegador
enviar duas requisições. Histórico de presença impede exclusões físicas em cascata
que apagariam evidências. Não foram introduzidos gatilhos nem um framework de migração.
As regras de unicidade e referência seguem os mecanismos nativos do
[PostgreSQL](https://www.postgresql.org/docs/16/ddl-constraints.html).

## Qual arquivo executar?

**Nenhum destes scripts foi executado contra o seu banco pelo agente.**
Confirme a conexão selecionada e faça backup antes de alterar um banco existente.

- Banco vazio: execute `db/inicializar.sql` pelo psql. Ele inclui V1 a V9 em uma
  única transação; não cria contas nem insere senhas ou dados demonstrativos.
- Banco existente: consulte `SELECT * FROM versoes_schema ORDER BY versao;` e execute
  **somente as versões pendentes**, em ordem, de `db/atualizar-v2.sql` até
  `db/atualizar-v9.sql`. Por exemplo, um banco em V4 precisa de V5, V6, V7, V8 e V9.
- Banco parcialmente modificado/manualmente diferente da V1: compare o esquema
  antes. Não use o inicializador para tentar “consertar” esse banco.

Crie primeiro um banco vazio no pgAdmin, com nome escolhido por você. Alternativamente,
um usuário PostgreSQL com permissão pode executar, fora de transação:

```sql
CREATE DATABASE events_dev;
```

`events_dev` é um nome de exemplo; os scripts não criam o banco automaticamente.
Em seguida, na raiz deste repositório, substitua os parâmetros:

```powershell
psql -X -h localhost -U SEU_USUARIO -d events_dev -W -f db/inicializar.sql
```

Para atualizar um banco V1, use os mesmos parâmetros de conexão e execute
`db/atualizar-v2.sql` até `db/atualizar-v9.sql`, em ordem. A senha é solicitada interativamente; não colocar
senha no SQL, no frontend ou no histórico do terminal.

Os comandos `\ir` e `\set` são do psql, não do Query Tool do pgAdmin.
No pgAdmin, para um banco vazio, execute o conteúdo de V1 a V9, nessa ordem, dentro
de `BEGIN;` / `COMMIT;`. Para um banco existente, execute apenas as versões pendentes.
Se houver erro, use `ROLLBACK;` e investigue; não prossiga executando trechos avulsos.

A V1 foi preservada. A V2 não usa DROP/TRUNCATE e recusa períodos incompletos
(início sem fim, por exemplo). Se dados anteriores violarem essa regra, a atualização
falha e precisa de uma correção de dados previamente revisada.
Os scripts não são de reaplicação silenciosa: executar uma versão duas vezes gera erro.
O backend não aplica migrações automaticamente.
No Docker Compose, o contêiner PostgreSQL executa V1 a V9 quando o volume
está vazio. Em um volume já inicializado, aplique as versões pendentes manualmente
após backup. Para um volume Docker que já possui V4, abra
`docker compose exec postgres-db sh`, entre no
`psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"`, confirme a versão atual
em `SELECT * FROM versoes_schema ORDER BY versao;` e aplique, uma a uma:

```sql
\set ON_ERROR_STOP on
BEGIN;
\i /docker-entrypoint-initdb.d/V5__programacao_inscricoes_agenda.sql
COMMIT;
```

Repita com V6, V7, V8 e V9, nessa ordem, cada uma em transação própria; nunca
reaplique uma versão já registrada. Após atualizar, confirme que o máximo é 9.

Essa pasta está montada no contêiner pelo Compose. Não use `docker compose down -v`
para atualizar perfis, pois isso apagaria o banco persistido.

Depois, configure no processo Java `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e
`JWT_SECRET`, usando suas próprias credenciais. Use Java 21.

## Criar o primeiro administrador

Cadastre normalmente a conta pelo site e confirme que ela existe no banco. O
cadastro público nunca aceita ADMINISTRADOR. Quem tem acesso administrativo ao
PostgreSQL pode conceder esse perfil diretamente, de forma controlada. Com o
Compose em execução, abra o terminal do banco:

```powershell
docker compose exec postgres-db sh
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"
```

No prompt `psql`, substitua o e-mail pelo da conta cadastrada:

```sql
\set email 'admin@exemplo.com'
SELECT id, email FROM usuarios WHERE email = :'email';
BEGIN;
INSERT INTO usuario_perfis(usuario_id, perfil)
SELECT id, 'ADMINISTRADOR' FROM usuarios WHERE email = :'email'
ON CONFLICT DO NOTHING;
COMMIT;
SELECT u.email, p.perfil FROM usuarios u
JOIN usuario_perfis p ON p.usuario_id = u.id
WHERE u.email = :'email';
```

Se a primeira consulta não retornar exatamente a conta desejada, não execute o
`INSERT`. O comando preserva outros perfis da conta, sem modificar senha ou JWT.
Saia da sessão do site e entre novamente: o login emitirá token e menus com o
perfil ADMINISTRADOR. Execute esse procedimento apenas em um banco sob seu controle.

## Testar o fluxo

1. Cadastre uma conta como organizador. Crie um evento com período
   e local, e publique-o pelo painel.
2. Acesse Organizador → Frequência (`/organizador/frequencia`).
3. Selecione o evento e abra “Cadastrar atividade”. O período deve caber no evento.
4. Selecione a atividade e clique em “Gerar nova chamada”.
5. Em outra sessão, cadastre/entre como participante, abra o evento e clique em
   “Inscrever-me”. A inscrição ativa é idempotente: repetir não cria outra.
6. Abra o QR com a câmera nativa do celular ou entre em `/presenca` e digite os
   12 caracteres. Faça login se necessário e pressione “Confirmar minha presença”.
7. Repita dentro da validade: o servidor informa a presença já existente.
   Gere outra chamada: o código anterior deve ser rejeitado.

Contas novas já têm perfil PARTICIPANTE ou ORGANIZADOR. A inscrição adiciona
PARTICIPANTE a uma conta organizadora que também participe. O token atual não ganha
novos perfis sozinho: faça login novamente para atualizar os menus de perfil. A página
`/presenca` permite confirmar com qualquer sessão válida cuja conta tenha inscrição
ativa; ela não depende do menu nem confia em um perfil editado no navegador.

O organizador controla quando abrir a chamada em um evento publicado. Nesta versão,
o período da atividade organiza o cronograma, mas não restringe o horário de emissão:
a janela de cinco minutos começa no momento da emissão, segundo o relógio do banco.
Atividades com política manual, entrada/saída ou percentual de permanência usam
marcações auditáveis, não o QR; a política é escolhida ao criar cada atividade.

## Celular e QR

O QR é desenhado localmente usando
[qrcode](https://github.com/soldair/node-qrcode/blob/master/README.md), sem serviços
externos de geração. Não existe leitor embutido solicitando acesso à câmera:
a câmera nativa do celular abre o link do webapp. A opção de digitação usa a mesma API.

`localhost` no celular é o próprio celular, não o computador do organizador.
Em teste, computador e celular precisam alcançar o mesmo endereço do webapp.
É possível iniciar o Vite com `npm run dev -- --host 0.0.0.0` em uma rede de
desenvolvimento confiável e acessar o IP LAN do computador pela porta indicada.
Isso expõe o servidor de desenvolvimento à rede: não liberar para a Internet nem
desativar o firewall; use dados fictícios e encerre o servidor ao terminar.

Opcionalmente configure `VITE_PUBLIC_APP_URL` em `frontend/.env.local` com o
endereço acessível pelo celular, incluindo o subdiretório do app, se existir.
Reinicie o Vite. Sem essa variável, o QR usa o endereço pelo qual o organizador abriu
a tela. O proxy continua encaminhando `/api` para o Java, sem precisar expor o banco.
Use HTTPS e implantação adequada no ambiente real.

O código fica no fragmento do link (`#codigo=...`), não em query enviada ao servidor.
A tela o remove do endereço e o mantém temporariamente na sessionStorage para
atravessar o login, sem colocá-lo no redirecionamento. Abrir a página nunca registra
presença; a confirmação é um POST autenticado.

## Segurança e concorrência

- Geração e confirmação exigem JWT válido no backend.
- Somente dono organizador ou administrador pode gerar a chamada.
- O código tem 12 caracteres aleatórios (60 bits), gerados com SecureRandom.
  Somente SHA-256 do código é persistido; ele não é um JWT de login.
- O QR é compartilhado pela turma. Não é um token de uso único global: a unicidade
  é por participante/atividade. Não equivale a autenticação multifator.
- Gerar novamente revoga a chamada anterior da atividade na mesma transação.
- Confirmação exige evento publicado, chamada não revogada/não expirada e inscrição
  ativa. O relógio do banco decide a validade, não o contador exibido no celular.
- Emissão/validação usam locks e transações. A confirmação verifica novamente a
  validade após aguardar o lock da inscrição.
- Geração e confirmação têm limite de 10 tentativas por minuto por conta/operação.
  Tentativas inválidas também contam. O contador é gravado em transação separada
  e permanece mesmo quando a confirmação falha. Excesso retorna 429 e Retry-After.
- O campo origem QR/CODIGO é informativo, enviado pelo cliente; não comprova
  proximidade nem deve conceder permissões diferentes.
- Não registrar códigos, JWTs ou corpos de confirmação nos logs.

**Limitação importante:** alguém pode compartilhar foto/código durante a validade.
O QR, sozinho, não comprova presença física. Supervisão do organizador continua
necessária. Também faltam políticas operacionais de rate limiting global/IP e
proteção do cadastro/login, especialmente contra múltiplas contas.
Validade curta e controle de tentativas usam como referência as recomendações
para [códigos temporários da OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html#one-time-passwords-otps);
isso não transforma este mecanismo de frequência em autenticação multifator.

## Verificação automatizada

Na validação de 24/09/2026: as migrações V1 a V9, casos de uso e o frontend foram
cobertos por testes. Neste Windows, a suíte HTTP precisou do parâmetro de
diretório temporário do JDK mostrado abaixo; sem ele, o JDK falhava ao abrir
o loopback antes de iniciar os testes de transporte.

O audit do npm apontou dois avisos moderados no Vitest/@vitest/mocker já utilizado
no projeto (GHSA-82fw-gwwq-j7x9), não na biblioteca de QR. A atualização do conjunto
de testes permanece pendente; não foi executado um audit fix indiscriminado.
O Checkstyle não está configurado/disponível no cache Maven desta máquina.

- Frontend: `npm run test:run`, `npm run build`, Oxlint e ESLint.
- Java: `mvn test`, com os testes de transporte usando servidor/JWT reais e dublês
  das portas de persistência. Os testes dependentes de banco continuam condicionais.
- SQL: `db/tests/schema.mjs` testa V1 a V9 em banco vazio, atualização preservando
  registros, unicidade, FKs, períodos, frequência e limite de tentativas. Também
  verifica as consultas reais do adaptador por EXPLAIN, sem executar suas escritas.

Neste Windows, execute o Maven com
`-DargLine=-Djdk.net.unixdomain.tmpdir=C:\Users\marcos.portela\IdeaProjects\events-api\target`
para que os testes HTTP usem um diretório temporário gravável. Em outros sistemas,
o `mvn test` normal pode ser suficiente. O pacote Java foi gerado com `mvn package`
após baixar os plugins ausentes do cache local. O Compose não foi executado aqui.

Para executar os testes SQL sem acessar o PostgreSQL configurado na aplicação:

```powershell
npm install --prefix target/sql-validation --no-save --package-lock=false @electric-sql/pglite
node --test db/tests/schema.mjs
```

O [PGlite](https://pglite.dev/docs/) é utilizado exclusivamente como PostgreSQL
efêmero em memória para estes testes. A instalação fica em target, ignorado pelo Git;
não foi adicionada ao frontend ou ao pom.xml. Isso não substitui testes do adaptador
JDBC e de concorrência em um servidor PostgreSQL real, nem o teste com celular.

## O que continua faltando no produto

Continuam fora desta entrega: imagens no banco, edição/exclusão de eventos e atividades,
paginação de mensagens, moderação da comunidade e automação de envio em massa.
O envio individual de certificado exige `SMTP_HOST`, `SMTP_PORT` (465 por padrão),
`SMTP_USERNAME`, `SMTP_PASSWORD` e `SMTP_FROM` no servidor. O adaptador exige TLS
implícito; nenhum segredo é enviado ao frontend ou persistido no repositório.
Um ambiente sem SMTP continua permitindo baixar o PDF.
