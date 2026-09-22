# Banco e chamada de presença

## Estado atual

A V1 tinha quatro tabelas: usuários, perfis, eventos e atividades. A V2 acrescenta
a base de inscrição e presença acordada: uma confirmação por atividade, inscrição
ativa no evento e chamada válida por cinco minutos.

| Tabela nova | Responsabilidade |
|---|---|
| inscricoes | Vínculo único entre usuário e evento, com estado e datas |
| chamadas_presenca | Chamada de uma atividade, emissor, hash do código, validade e revogação |
| presencas | Uma marcação por usuário/atividade, ligada à inscrição e à chamada corretas |
| limites_presenca | Contador persistente de tentativas por usuário/operação |
| versoes_schema | Registro das versões aplicadas manualmente |

Chaves estrangeiras compostas impedem associar presença, inscrição e chamada de
eventos diferentes. UNIQUE protege contra duplicidade, inclusive se o navegador
enviar duas requisições. Histórico de presença impede exclusões físicas em cascata
que apagariam evidências. Não foram introduzidos gatilhos nem um framework de migração.
As regras de unicidade e referência seguem os mecanismos nativos do
[PostgreSQL](https://www.postgresql.org/docs/16/ddl-constraints.html).

## Qual arquivo executar?

**Nenhum destes scripts foi executado contra o seu banco pelo agente.**
Confirme a conexão selecionada e faça backup antes de alterar um banco existente.

- Banco vazio: execute `db/inicializar.sql` pelo psql. Ele inclui V1 e V2 em uma
  única transação; não cria contas nem insere senhas ou dados demonstrativos.
- Banco que já possui exatamente a V1: execute `db/atualizar-v2.sql`.
- Banco com V2 aplicada: não execute novamente. Consulte
  `SELECT * FROM versoes_schema ORDER BY versao;`.
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

Para atualizar um banco V1, use os mesmos parâmetros de conexão e troque o último
arquivo por `db/atualizar-v2.sql`. A senha é solicitada interativamente; não colocar
senha no SQL, no frontend ou no histórico do terminal.

Os comandos `\ir` e `\set` são do psql, não do Query Tool do pgAdmin.
No pgAdmin, para um banco vazio, execute o conteúdo de V1 e V2, nessa ordem, dentro
de `BEGIN;` / `COMMIT;`. Para um banco V1, execute somente V2 dentro da transação.
Se houver erro, use `ROLLBACK;` e investigue; não prossiga executando trechos avulsos.

A V1 foi preservada. A V2 não usa DROP/TRUNCATE e recusa períodos incompletos
(início sem fim, por exemplo). Se dados anteriores violarem essa regra, a atualização
falha e precisa de uma correção de dados previamente revisada.
Os scripts não são de reaplicação silenciosa: executar uma versão duas vezes gera erro.
O backend não aplica migrações automaticamente.

Depois, configure no processo Java `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e
`JWT_SECRET`, usando suas próprias credenciais. Use Java 21.

## Testar o fluxo

1. Use uma conta de organizador autorizada pela equipe. Crie um evento com período
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

Inscrever-se adiciona o perfil PARTICIPANTE no banco. O token atual não ganha novos
perfis sozinho: faça login novamente para atualizar os menus de perfil. A página
`/presenca` permite confirmar com qualquer sessão válida cuja conta tenha inscrição
ativa; ela não depende do menu nem confia em um perfil editado no navegador.

O organizador controla quando abrir a chamada em um evento publicado. Nesta versão,
o período da atividade organiza o cronograma, mas não restringe o horário de emissão:
a janela de cinco minutos começa no momento da emissão, segundo o relógio do banco.
Não implementamos controle de entrada/saída ou cálculo de percentual de frequência.

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

Na validação de 22/09/2026: 37 testes frontend aprovados; build, Oxlint e ESLint
aprovados; 32 testes Java aprovados e 6 testes dependentes de banco ignorados;
2 cenários SQL aprovados em PostgreSQL embarcado.

O audit do npm apontou dois avisos moderados no Vitest/@vitest/mocker já utilizado
no projeto (GHSA-82fw-gwwq-j7x9), não na biblioteca de QR. A atualização do conjunto
de testes permanece pendente; não foi executado um audit fix indiscriminado.
O Checkstyle não está configurado/disponível no cache Maven desta máquina.

- Frontend: `npm run test:run`, `npm run build`, Oxlint e ESLint.
- Java: `mvn test`, com os testes de transporte usando servidor/JWT reais e dublês
  das portas de persistência. Os testes dependentes de banco continuam condicionais.
- SQL: `db/tests/schema.mjs` testa V1+V2 em banco vazio, atualização preservando
  registros, unicidade, FKs, períodos, validade e limite de tentativas. Também
  verifica as consultas reais do adaptador por EXPLAIN, sem executar suas escritas.

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

Esta etapa não completa todo o modelo do sistema: imagens no banco, agenda individual,
controle de vagas por atividade, cancelamento via API, correção manual auditada de
presença, relatórios, questionários/avaliações e certificados permanecem pendentes.
Digitar o código pelo participante não é o lançamento manual feito por um organizador.
As telas de listagem de inscrições e relatórios ainda são demonstrativas.
