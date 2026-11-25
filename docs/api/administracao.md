# Administracao e bloqueio de eventos

As quatro telas administrativas usam dados reais. O mesmo frontend atende web e
Electron. Usuarios, eventos, responsaveis e registros de auditoria usam UUID.
O cadastro publico continua aceitando ORGANIZADOR sem aprovacao.

## Banco existente no Docker

Esta alteracao exige a versao 10 do esquema. Scripts de inicializacao do Postgres
nao sao reaplicados em volumes existentes. Nao apague o volume para atualizar.
Na raiz do projeto, com o banco Docker ativo, execute uma vez no PowerShell:

```powershell
Get-Content -Raw -Encoding utf8 src/main/resources/db/migration/V9a__administracao_e_bloqueio_eventos.sql | docker compose exec -T postgres-db sh -c 'psql -1 -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose up -d --build app frontend
```

O `psql -1` executa a migracao em uma unica transacao. Se ocorrer erro, ela nao e
aplicada parcialmente. A versao aparece em `versoes_schema` com numero 10.
O nome V9a garante ordenacao depois de V9 na inicializacao alfabetica do Docker;
nao existe Flyway no projeto. O script `db/atualizar-v10.sql` e a entrada equivalente
para psql executado localmente. Bancos vazios recebem a migracao pelo init do Docker
ou por `db/inicializar.sql`.

Para o Electron instalado, gere e distribua um novo build. Em desenvolvimento,
reinicie `npm run desktop:dev` depois de atualizar os containers.

## Rotas novas

Todos os caminhos abaixo exigem JWT e ADMINISTRADOR. Os casos de uso consultam o
perfil atual no banco; um token antigo nao mantem permissoes administrativas revogadas.
O prefixo externo `/api` e removido pelo proxy.

| Metodo | Caminho | Funcao |
|---|---|---|
| GET | /admin/resumo | Totais de usuarios, organizadores, eventos publicados/bloqueados e registros de presenca |
| GET | /admin/usuarios | Contas com UUID, nome, email e perfis; sem hash de senha |
| PUT | /admin/usuarios/{uuid}/perfis | Define a lista `perfis` da conta |
| GET | /admin/eventos | Todos os eventos com organizador e motivo de bloqueio |
| POST | /admin/eventos/{uuid}/bloqueio | Bloqueia; corpo `{"motivo":"Justificativa"}` |
| DELETE | /admin/eventos/{uuid}/bloqueio | Desbloqueia e restaura o estado anterior |
| GET | /admin/auditoria | Ultimas 500 alteracoes administrativas, mais recentes primeiro |
| GET | /admin/auditoria.csv | Exporta os mesmos 500 registros sem o filtro local da tela |

Publicacao e encerramento continuam usando as rotas existentes de eventos.
As telas de inscricoes, frequencia e relatorios utilizam a lista global quando o
usuario autenticado e administrador. "Meus eventos" continua sendo a lista do dono.

## Regras

- BLOQUEADO preserva o estado anterior (RASCUNHO, PUBLICADO ou ENCERRADO).
- Motivo obrigatorio, sem espacos em branco apenas, com ate 2000 caracteres.
- O motivo aparece em "Meus eventos" do organizador e na lista administrativa.
- Bloqueados saem do catalogo e do detalhe publico; novas inscricoes, chamadas e
  presencas exigem evento publicado. A gestao de programacao/questionarios,
  categoria e publicacao/encerramento rejeita eventos bloqueados.
- Relatorios historicos continuam consultaveis. Nao sao apagadas inscricoes ou presencas.
- Desbloquear restaura o estado anterior, inclusive PUBLICADO. A interface pede
  confirmacao antes de executar essa operacao.
- Perfis sao multiplos. A conta deve manter pelo menos um perfil autenticado e o
  ultimo administrador nao pode perder esse perfil. Essa verificacao e serializada
  na transacao do banco.
- Alteracoes de perfil exigem novo login para atualizar o menu/token. Alterar os
  proprios perfis encerra a sessao do cliente. A autorizacao do servidor considera
  os perfis atuais, independentemente do menu antigo.
- Bloqueio, desbloqueio e alteracao de perfis registram auditoria na mesma transacao
  da escrita. Nao ha historico retroativo. Publicacao/encerramento e demais acoes
  anteriores nao fazem parte desta trilha administrativa inicial.
- Valores informados pelos usuarios sao interpolados como texto pelo Vue, nunca HTML.
  O CSV escapa aspas e neutraliza prefixos de formula.

## Validacao

Testes novos: regras de bloqueio/desbloqueio, motivo, visibilidade do organizador,
catalogo, alteracoes impedidas, autorizacao atual, contrato UUID, ausencia de hash,
exportacao CSV e protecao do ultimo administrador. O frontend testa as chamadas
HTTP autenticadas e a selecao global de eventos.

Os testes SQL em `db/tests/schema.mjs` usam PostgreSQL embarcado temporario;
nao utilizam as credenciais nem o banco do usuario. Consulte o cabecalho do arquivo
para preparar essa ferramenta de teste, se necessario.
