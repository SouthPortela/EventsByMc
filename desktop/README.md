# EventsByMc para Windows

O desktop utiliza o mesmo codigo Vue do site e a mesma API Java. Java, PostgreSQL
e site/Nginx rodam em containers Docker no computador servidor. O Electron pode
ser instalado nesse computador ou em outros computadores da rede, escolhendo o
servidor durante a instalacao. Esta versao nao implementa sincronizacao offline;
o servidor precisa estar ativo e acessivel.

## Preparacao

Na raiz do repositorio, utilizando Node compativel com frontend/package.json:

```powershell
npm install
npm --prefix frontend install
npm run desktop:install
Copy-Item .env.desktop.example .env.desktop
```

Edite `.env.desktop` para definir os padroes de desenvolvimento e do executavel
sem configuracao de instalacao:

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
Esses enderecos sao publicos e incorporados ao build como fallback. O instalador
permite escolher outro destino, sem recompilar. HTTP e aceito em localhost e IPs
privados da rede; os outros destinos exigem HTTPS.

No computador servidor, com o `.env` do Docker configurado, inicie os servicos:

```powershell
docker compose up -d --build
```

O site fica em `http://127.0.0.1/`. Nomes de servico como `app` e `postgres-db`
existem na rede dos containers e nao devem ser usados como endereco pelo Electron.
O desktop nao inicia nem encerra os containers automaticamente. Computadores que
acessam outro servidor precisam somente do aplicativo instalado e acesso a rede.

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
com o prefixo `/api/`. O QR substitui o loopback pelo IP de rede do computador,
preservando a porta do site Docker. A descoberta e refeita ao gerar uma chamada.
O celular precisa estar na mesma rede e conseguir acessar a porta do site.
Veja [QR na rede local](../docs/api/presenca-rede.md), incluindo o comando
`npm run presenca:rede` para quem gera chamadas pelo site Docker em localhost.

## Build e distribuicao

```powershell
npm run desktop:build
npm run desktop:start
```

O build verifica os tipos e gera a interface em `desktop/renderer/`, separada de
`frontend/dist/`. Gera tambem `desktop/runtime-config.json`, com o destino fixo da
API como fallback. Ambos sao artefatos ignorados pelo Git. O aplicativo instalado
prioriza `servidor.ini`, junto do executavel, que permite mudar IP, porta e HTTPS
sem gerar outro build. Mudancas do IP local por DHCP sao detectadas para o QR
quando o servidor configurado e localhost.

Depois do build, `npm run desktop:smoke` abre uma janela oculta do Electron e
verifica a tela de login, CSS, armazenamento, isolamento e acesso a uma API simulada.
Esse teste nao acessa o backend nem altera dados de producao.

```powershell
npm run desktop:pack
npm run desktop:dist
```

`desktop:pack` gera a pasta executavel e `desktop:dist` gera o instalador NSIS para
Windows em `release/`. O empacotador pode baixar ferramentas na primeira execucao.
O instalador apresenta a etapa **Servidor do EventsByMc**, com estes valores:

| Campo | Padrao | Exemplo de outro computador |
|---|---|---|
| IP ou nome do servidor | localhost | 192.168.1.4 |
| Porta do site | 80 | 8081, se WEB_PORT=8081 no servidor |
| Usar HTTPS | Desmarcado | Marque quando o servidor tiver HTTPS configurado |

O mesmo instalador serve para todos os computadores. Informe o IP sem `http://`,
sem caminhos e sem a porta; a porta tem seu proprio campo. Consultas e operacoes
usam o site do servidor, que encaminha `/api/` ao Java. A porta escolhida e a mesma
utilizada para abrir o site no navegador. HTTPS sugere 443 ao marcar a opcao;
portas personalizadas podem ser informadas no campo.

O instalador grava `servidor.ini` na pasta do aplicativo:

```ini
[servidor]
host=192.168.1.4
porta=8081
https=0
```

`https=1` habilita HTTPS. Para mudar o destino depois, execute o instalador novamente
ou feche o app, edite esse arquivo e reabra. A configuracao existente e carregada
na reinstalacao e preservada em atualizacoes silenciosas. Uma instalacao nova
comeca em localhost. Configuracao invalida impede a inicializacao e mostra como
corrigir; o app nao troca silenciosamente de servidor.

O QR usa o mesmo servidor escolhido no instalador. Se o app estiver instalado em
um computador cliente, o QR aponta para o servidor, preservando a porta. Com
localhost, a descoberta do IP local para acesso pelo celular continua funcionando.

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
bloqueio e auditoria. O banco deve estar na versão lógica 11; veja
[administração](../docs/api/administracao.md).
