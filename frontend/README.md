# Frontend EventsByMc

Interface Vue 3 + TypeScript compartilhada pelo site e pelo aplicativo Electron.
Usa Composition API com script setup lang="ts", Vue Router, Pinia e Bootstrap.
O frontend consome a API HTTP; nunca acessa diretamente o PostgreSQL.

## Preparação e desenvolvimento

Requer Node.js ^22.18.0 ou >=24.12.0. Na pasta frontend:

    npm install
    npm run dev

O backend precisa estar ativo e o banco atualizado até a versão lógica 11.
Copie .env.example para .env.local se precisar alterar os padrões.

| Variável | Uso |
|---|---|
| VITE_API_BASE_URL | Base das chamadas do navegador; padrão /api |
| API_PROXY_TARGET | Destino do proxy de desenvolvimento; padrão http://127.0.0.1:8080 |
| API_PROXY_KEEP_PREFIX | false para Java direto; true se o destino já atende em /api |
| VITE_PUBLIC_APP_URL | URL pública do site utilizada no QR; opcional no navegador; loopback pode ser substituído pelo IP do host |

API_PROXY_* são configurações do Vite em desenvolvimento. Não são a configuração
do Nginx de produção. Variáveis VITE_* são públicas e incorporadas ao build;
não inclua credenciais, JWT_SECRET ou dados de conexão ao banco.

## Estrutura e fluxos

| Diretório | Conteúdo |
|---|---|
| src/features | Serviços, tipos, componentes e regras por funcionalidade |
| src/views | Telas de participante, organizador e administração |
| src/components | Layout, marca e componentes compartilhados |
| src/shared/services | Cliente HTTP e tratamento de respostas |
| src/stores | Sessão Pinia, armazenada em sessionStorage |
| src/router | Rotas e requisitos de acesso |
| src/tests e arquivos *.spec.ts | Testes Vitest |

As telas administrativas consultam a API real. VISITANTE é navegação anônima;
PARTICIPANTE, ORGANIZADOR e ADMINISTRADOR são perfis de contas autenticadas.
A autorização efetiva sempre ocorre no backend. Alterações de perfil exigem novo login
para atualizar menus e token.

Não existe upload de imagens nesta versão. Entradas textuais são renderizadas por
interpolação Vue, sem v-html para conteúdo informado pelos usuários.

## Verificação e build

    npm run test:run
    npm run type-check
    npm exec -- oxlint .
    npm exec -- eslint .
    npm run build

Os dois comandos npm exec acima verificam sem corrigir arquivos. npm run lint
executa as regras existentes com --fix; npm run format também altera os arquivos.
O build gera dist/. npm run preview serve essa saída para conferência local.
O Dockerfile usa Nginx para entregar a SPA e encaminhar /api ao Java.

O gerador de QR utiliza o endereço do site na rede e preserva sua porta. No site
Docker aberto por localhost, execute `npm run presenca:rede` na raiz após subir
os contêineres e após mudanças de IP. No desktop e no Vite, a descoberta ocorre
a cada geração. A tela também aceita um endereço manual. Veja
[QR na rede local](../docs/api/presenca-rede.md).

O build desktop usa modo desktop e grava em desktop/renderer/, por scripts da raiz.
Veja [desktop](../desktop/README.md), [contratos](../docs/api/contratos-webapp.md)
e [funcionalidades adicionais](../docs/api/requisitos-funcionais-webapp.md).

Edição de conta em Minha conta; edição de eventos nas listas de gestão; retificações
na gestão de frequência; detalhes por atividade nos relatórios. Veja os
[contratos novos](../docs/api/edicao-historico.md).
