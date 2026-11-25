# EventsByMc para Windows

O desktop utiliza o mesmo codigo Vue do site e a mesma API Java. Java e PostgreSQL
rodam em containers Docker no mesmo computador, junto com o site/Nginx. O Electron
roda como aplicativo Windows fora do Docker e conecta pelas portas publicadas no host.
Esta versao nao implementa sincronizacao offline; os containers precisam estar ativos.

## Preparacao

Na raiz do repositorio, utilizando Node compativel com frontend/package.json:

```powershell
npm install
npm --prefix frontend install
npm run desktop:install
Copy-Item .env.desktop.example .env.desktop
```

Edite `.env.desktop` antes de gerar a distribuicao:

- `DESKTOP_API_URL`: raiz da API, incluindo `/api/` se o proxy do servidor usar esse prefixo.
- `DESKTOP_PUBLIC_APP_URL`: raiz do site utilizada nos links de QR Code.

O exemplo e os padroes usam o Docker Compose local, sem dominio externo:

```env
DESKTOP_API_URL=http://127.0.0.1/api/
DESKTOP_PUBLIC_APP_URL=http://127.0.0.1/
```

O frontend Docker publica `WEB_PORT=80` por padrao e encaminha `/api/` para o Java.
Se o `.env` do Docker definir outra WEB_PORT, informe a mesma porta nos dois
enderecos (exemplo: `http://127.0.0.1:8081/api/` e `http://127.0.0.1:8081/`).
O desktop nao le o `.env` do backend nem recebe credenciais de banco ou JWT_SECRET.
Enderecos sao publicos e incorporados ao build. HTTP em loopback e aceito tanto
em desenvolvimento quanto no aplicativo empacotado; destinos remotos exigem HTTPS.

Com o `.env` do Docker ja configurado, inicie os servicos antes de abrir o desktop:

```powershell
docker compose up -d --build
```

O site fica em `http://127.0.0.1/`. Nomes de servico como `app` e `postgres-db`
existem na rede dos containers e nao devem ser usados como endereco pelo Electron.
O desktop nao inicia nem encerra os containers automaticamente.

`desktop:install` baixa o executavel oficial do Electron, necessario para abrir a
janela. O pacote npm pode estar presente sem esse binario. Se ocorrer `fetch failed`,
confira a conexao e o acesso ao servidor de downloads e repita esse comando;
nao e necessario apagar as dependencias do projeto.

## Desenvolvimento

```powershell
npm run desktop:dev
```

O comando inicia o Vite em `127.0.0.1:5173` e abre o Electron. Ao fechar o aplicativo,
o processo Vite e encerrado. A porta precisa estar livre. O backend nao e iniciado
por este comando. Sem `.env.desktop`, utiliza o Nginx Docker em `127.0.0.1:80`,
com o prefixo `/api/`. Os QR Codes apontam para `127.0.0.1` e so abrem no proprio
computador; a confirmacao por codigo pode ser testada no navegador local. Testar
QR no celular exigira uma etapa posterior de configuracao de acesso pela rede.

## Build e distribuicao

```powershell
npm run desktop:build
npm run desktop:start
```

O build verifica os tipos e gera a interface em `desktop/renderer/`, separada de
`frontend/dist/`. Gera tambem `desktop/runtime-config.json`, com o destino fixo da
API. Ambos sao artefatos ignorados pelo Git. Alteracoes nos enderecos exigem novo build.

Depois do build, `npm run desktop:smoke` abre uma janela oculta do Electron e
verifica a tela de login, CSS, armazenamento, isolamento e acesso a uma API simulada.
Esse teste nao acessa o backend nem altera dados de producao.

```powershell
npm run desktop:pack
npm run desktop:dist
```

`desktop:pack` gera a pasta executavel e `desktop:dist` gera o instalador NSIS para
Windows em `release/`. O empacotador pode baixar ferramentas na primeira execucao.
O instalador inicial utiliza o icone padrao do Electron; assinatura de codigo,
icone definitivo e atualizacao automatica ainda precisam ser configurados para
distribuicao publica. Um instalador sem assinatura pode exibir avisos do Windows.

## Integracao e seguranca

A interface empacotada utiliza `eventsbymc://app/`. O protocolo entrega arquivos
locais e faz fallback para `index.html` nas rotas Vue, preservando `createWebHistory`.
Requisicoes `/api/` sao encaminhadas pelo processo principal apenas para a API
configurada, preservando o JWT. Nao ha acesso direto ao banco nem alteracao de CORS
no servidor. Redirecionamentos da API sao rejeitados para evitar encaminhar tokens
a outro destino. O frontend continua responsavel por enviar JWT nas rotas protegidas,
e o backend continua responsavel por valida-lo e verificar permissoes.

A janela utiliza sandbox, isolamento de contexto, Node desabilitado no renderer,
politica de conteudo e bloqueio de navegacao externa, novas janelas e permissoes.
Nao existe preload ou IPC: as funcionalidades atuais nao precisam de APIs nativas
expostas ao Vue. Downloads PDF/CSV usam o dialogo de salvamento do Chromium.

O login e armazenado em sessionStorage, como no site. Nao ha compartilhamento
automatico da sessao com o navegador nem implementacao de "lembrar de mim".

## Validacao

```powershell
npm test
npm run lint
npm --prefix frontend run test:run
npm --prefix frontend run build
```

O lint da raiz valida sintaxe dos arquivos Electron; as regras ESLint/Oxlint do
frontend continuam em `frontend/`. Os testes desktop verificam configuracao,
isolamento de arquivos, destino da API, JWT, downloads, erros e redirecionamentos.

Antes de distribuir, valide com uma API de desenvolvimento: login, perfis,
criacao/publicacao de evento no desktop aparecendo no site, QR aberto pelo celular,
presenca visivel nos relatorios, downloads PDF/CSV, expiracao de sessao e perda de rede.
As telas administrativas consultam a API real: usuários/perfis, visão global de eventos,
bloqueio e auditoria. O banco deve estar na versão lógica 10; veja
[administração](../docs/api/administracao.md).
