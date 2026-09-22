# Integração do webapp Vue com a API Java

Referência: código do commit `c8ad62c`, revisado em 10/09/2026.

Este documento preserva o roteiro didático anterior à implementação. Em 22/09/2026,
o cliente autenticado, a consulta da conta e os fluxos principais de eventos foram
implementados. Consulte [contratos-webapp.md](contratos-webapp.md) para o estado atual,
os arquivos modificados e a configuração do proxy. As indicações de “proposto” e
“ainda não implementado” nas seções históricas abaixo descrevem a revisão anterior,
não o estado atual desses fluxos. Não reaplique estes exemplos sobre a implementação.

## 1. Primeira entrega: consultar minha conta

O cadastro e o login já possuem chamadas HTTP. A primeira integração nova será consultar o usuário autenticado no PostgreSQL e mostrar seus dados em `/conta`.

Essa escolha permite exercitar autenticação, requisição, erro, tipagem, caso de uso, adaptador e renderização sem depender da programação e das inscrições ainda não implementadas.

```text
AccountView.vue
  -> contaService.ts
  -> httpClient.ts
  -> GET /api/usuarios/me + Authorization: Bearer <token>
  -> proxy remove /api
  -> AutenticacaoFiltro valida o JWT
  -> Router seleciona MinhaContaHandler
  -> ConsultarMinhaContaUseCase
  -> UsuarioRepository / JdbcUsuarioRepository
  -> PostgreSQL
  -> dados seguros em JSON
  -> Vue atualiza a tela
```

`GET` consulta dados; `POST` normalmente cria um recurso ou executa uma operação; `PATCH` altera parte de um recurso. Os headers transportam metadados, como o token. O body transporta dados de entrada quando a operação precisa deles. A resposta combina um status HTTP e, normalmente, um corpo JSON.

O navegador não acessa o PostgreSQL diretamente. O servidor decide quais informações o usuário pode consultar.

## 2. Contrato antes da implementação

| Método e caminho externo | Autenticação | Estado |
|---|---|---|
| `POST /api/usuarios` | Pública | Já implementado; preservar resposta atual em texto e status 200 nesta etapa |
| `POST /api/auth/login` | Pública | Já implementado; preservar o campo existente `usuarioID` |
| `GET /api/usuarios/me` | JWT obrigatório | Proposto neste guia; ainda não existe no código executável |

A nova rota não recebe ID no caminho, query ou body. O backend obtém o identificador a partir do JWT validado e busca os dados atuais no banco.

Resposta proposta de `GET /api/usuarios/me`, com status `200`:

```json
{
  "usuarioId": "550e8400-e29b-41d4-a716-446655440000",
  "nome": "Ana Exemplo",
  "email": "ana@example.com",
  "perfis": ["VISITANTE"]
}
```

Os dados são fictícios. `VISITANTE` reflete o perfil que o cadastro atribui hoje. Consultar a própria conta exigirá autenticação, independentemente desse perfil. A revisão da política participante/organizador continua sendo uma tarefa separada; não concederemos privilégios no frontend.

O novo campo `usuarioId` não renomeia o campo `usuarioID` da resposta de login. Os contratos são separados e a compatibilidade existente é preservada.

| Situação | Resposta |
|---|---|
| Token válido e conta existente | 200 com os dados atuais |
| Token ausente, inválido ou expirado | 401 com `{ mensagem, instante }` |
| Conta removida, embora o token ainda seja válido | 401: iniciar uma nova sessão |
| Falha de banco | 500 com mensagem genérica, sem detalhes internos |

`403` significa falta de permissão para uma operação; não deve encerrar automaticamente uma sessão válida. A consulta da própria conta aceita qualquer perfil autenticado.

## 3. Pré-requisitos e endereço da API

O projeto configura Java 21. Na revisão, `java -version` neste terminal ainda apontou para Java 8; conferir JDK da IDE, `JAVA_HOME` e `PATH` antes de executar o backend. O Node identificado foi 24.19.0.

As variáveis `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e `JWT_SECRET` precisam estar configuradas no processo Java. `JWT_EXPIRATION_MINUTES` e `SERVER_PORT` são configurações opcionais com defaults no código. O `CompositionRoot` lê o ambiente; executar um JAR não carrega um arquivo `.env` automaticamente.

Para executar localmente, manter a base do frontend como `/api`. O exemplo a seguir substituiria **somente o destino do proxy** na configuração do Vite; os plugins, aliases e demais opções existentes continuam necessários:

```ts
server: {
  proxy: {
    '/api': {
      target: 'http://127.0.0.1:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
},
```

O endereço acima é o exemplo de desenvolvimento. Se a equipe precisar alternar ambientes, o destino pode ser configurado por variável, sem credenciais no código. Nenhuma alteração de infraestrutura foi aplicada por este guia.

Assim, o navegador chama `http://localhost:5173/api/usuarios/me`, e o Vite encaminha para `http://127.0.0.1:8080/usuarios/me`.

Se o destino escolhido for o domínio remoto que já possui Apache em `/api/`, o Vite deve preservar esse prefixo. Não remover `/api` antes de encaminhar ao Apache. Não usar `mode: 'no-cors'`: isso não libera a leitura da resposta. O proxy permite que o navegador trabalhe com a mesma origem. [Referência do proxy do Vite](https://vite.dev/config/server-options.html#server-proxy).

## 4. Cliente HTTP compartilhado

Implementação proposta para substituir futuramente `frontend/src/shared/services/httpClient.ts`:

```ts
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(public readonly status: number, message: string) {
    super(message)
    this.name = 'ApiError'
  }
}

interface ConfiguracaoHttp {
  obterToken: () => string | null
  aoNaoAutorizado: () => void
}

interface OpcoesRequisicao extends RequestInit {
  autenticada?: boolean
}

let configuracao: ConfiguracaoHttp = {
  obterToken: () => null,
  aoNaoAutorizado: () => {},
}

export function configurarHttpClient(nova: ConfiguracaoHttp): void {
  configuracao = nova
}

function mensagemDaApi(corpo: unknown): string {
  if (
    typeof corpo === 'object' && corpo !== null &&
    'mensagem' in corpo && typeof corpo.mensagem === 'string'
  ) {
    return corpo.mensagem
  }
  return 'Não foi possível concluir a operação.'
}

export async function apiRequest<T>(
  path: string,
  opcoes: OpcoesRequisicao = {},
): Promise<T> {
  const { autenticada = false, ...init } = opcoes
  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')
  const token = autenticada ? configuracao.obterToken() : null

  if (autenticada) {
    if (!token) {
      configuracao.aoNaoAutorizado()
      throw new ApiError(401, 'Entre na sua conta para continuar.')
    }
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...init, headers })

  if (!response.ok) {
    if (autenticada && response.status === 401 && token === configuracao.obterToken()) {
      configuracao.aoNaoAutorizado()
    }
    const corpo: unknown = await response.json().catch(() => null)
    throw new ApiError(response.status, mensagemDaApi(corpo))
  }

  if (response.status === 204) return undefined as T

  const tipo = response.headers.get('Content-Type')?.split(';')[0]?.trim()
  if (tipo === 'application/json' || tipo?.endsWith('+json')) {
    return (await response.json()) as T
  }
  if (tipo === 'text/plain') return (await response.text()) as T

  throw new ApiError(response.status, 'Resposta inesperada do servidor.')
}
```

Pontos importantes:

- O cliente recebe funções de configuração: não importa a store nem o router. Isso evita o ciclo `store -> authService -> httpClient -> store`.
- O token é lido no momento da requisição, então o cliente acompanha login e logout.
- `autenticada: true` é uma opção nossa. Ela é retirada antes de chamar `fetch`.
- Login e cadastro continuam funcionando sem alterar `authService.ts`: usam JSON na entrada, e suas respostas atuais continuam suportadas.
- Não encerrar a sessão para um 401 de login; isso pode significar apenas senha incorreta. Só respostas de chamadas marcadas como autenticadas acionam o callback.
- A comparação do token evita que uma resposta antiga apague uma sessão criada posteriormente.
- `Headers` aceita os diferentes formatos de `RequestInit.headers`, sem depender de espalhar um objeto com `...`.
- O `Content-Type` de entrada continua sendo definido pelo serviço que envia JSON. Não defini-lo globalmente: isso atrapalharia futuros uploads com `FormData`.
- HTML retornado pelo proxy não será aceito como resposta válida da API. O usuário vê mensagem genérica; a aba Network permite verificar o endereço e a resposta.
- Falhas de rede e cancelamentos rejeitam a promessa e chegam ao `catch` da tela. O exemplo não implementa repetição automática ou timeout; `signal` pode ser passado em `opcoes`.
- O exemplo suporta JSON, texto e status 204. Imagens/arquivos precisarão de leitura binária própria.
- `as T` informa o formato esperado ao TypeScript, mas não valida a estrutura recebida em execução. Testes de contrato e, conforme necessário, validadores de resposta devem cobrir essa fronteira.

`fetch` não lança uma exceção automaticamente para respostas 400/401/500; por isso verificamos `response.ok`. [Referência da Fetch API](https://developer.mozilla.org/en-US/docs/Web/API/Fetch_API/Using_Fetch).

## 5. Ligar o cliente HTTP à sessão

Implementação proposta de `frontend/src/main.ts`, preservando os estilos e a montagem atual:

```ts
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap'
import './assets/theme.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { pinia } from './stores'
import { useAuthStore } from './stores/auth'
import { configurarHttpClient } from './shared/services/httpClient'

const app = createApp(App)
app.use(pinia)
const auth = useAuthStore(pinia)

configurarHttpClient({
  obterToken: () => auth.token,
  aoNaoAutorizado: () => {
    auth.sair()
    const rotaAtual = router.currentRoute.value
    if (rotaAtual.name !== 'login') {
      void router.replace({
        name: 'login',
        query: { redirect: rotaAtual.fullPath },
      })
    }
  },
})

app.use(router)
app.mount('#app')
```

O callback limpa a sessão usando a store existente e preserva a página de destino. Instalar Pinia e configurar o cliente antes de iniciar a navegação evita usar a store fora de seu contexto. [Referência do Pinia](https://pinia.vuejs.org/core-concepts/outside-component-usage.html).

## 6. Serviço e contrato TypeScript da conta

Novo arquivo proposto: `frontend/src/features/account/services/contaService.ts`.

```ts
import { apiRequest } from '@/shared/services/httpClient'
import type { PerfilUsuario } from '@/features/auth/types/perfil'

export interface MinhaConta {
  usuarioId: string
  nome: string
  email: string
  perfis: PerfilUsuario[]
}

export function consultarMinhaConta(): Promise<MinhaConta> {
  return apiRequest<MinhaConta>('/usuarios/me', {
    method: 'GET',
    autenticada: true,
  })
}
```

O serviço sabe qual endpoint atende a funcionalidade. O componente não monta headers, não procura token e não conhece a URL do servidor. Não enviar `usuarioId` pelo navegador: `/me` significa a conta identificada pelo token validado.

## 7. Backend: resultado, porta e caso de uso

Os próximos blocos são arquivos novos propostos. Seus diretórios seguem os pacotes indicados, sob `src/main/java`.

### DadosMinhaConta.java

```java
package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.domain.model.Perfil;
import java.util.Set;
import java.util.UUID;

public record DadosMinhaConta(UUID usuarioId, String nome, String email, Set<Perfil> perfis) {
    public DadosMinhaConta {
        perfis = Set.copyOf(perfis);
    }
}
```

Este resultado contém somente os campos necessários para a tela. Nunca retornar a entidade `Usuario` diretamente: ela também possui o hash de senha. A cópia do conjunto impede alteração externa dos perfis desse resultado.

### ConsultarMinhaConta.java

```java
package br.com.eventsbymc.eventsapi.application.port.in;

import br.com.eventsbymc.eventsapi.application.usecase.DadosMinhaConta;
import java.util.UUID;

public interface ConsultarMinhaConta {
    DadosMinhaConta executar(UUID usuarioAutenticadoId);
}
```

### ConsultarMinhaContaUseCase.java

```java
package br.com.eventsbymc.eventsapi.application.usecase;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.port.out.UsuarioRepository;
import java.util.Objects;
import java.util.UUID;

public final class ConsultarMinhaContaUseCase implements ConsultarMinhaConta {
    private final UsuarioRepository usuarios;

    public ConsultarMinhaContaUseCase(UsuarioRepository usuarios) {
        this.usuarios = Objects.requireNonNull(usuarios);
    }

    @Override
    public DadosMinhaConta executar(UUID usuarioAutenticadoId) {
        if (usuarioAutenticadoId == null) {
            throw new TokenInvalidoException("Sessão inválida. Entre novamente.", null);
        }
        var usuario = usuarios.buscarPorId(usuarioAutenticadoId)
                .orElseThrow(() -> new TokenInvalidoException(
                        "Sessão inválida. Entre novamente.", null));

        return new DadosMinhaConta(
                usuario.getId(),
                usuario.getPessoa().getNome(),
                usuario.getPessoa().getEmail(),
                usuario.getPerfis());
    }
}
```

O caso de uso recebe um ID autenticado, consulta a porta `UsuarioRepository` e monta uma resposta segura. Não conhece `HttpExchange`, SQL ou Vue. `buscarPorId` já existe no contrato e no adaptador JDBC, portanto esta entrega não exige outra consulta SQL nem migração de banco.

A exceção utilizada já existe no projeto e já é mapeada para 401. Se a conta desapareceu, não usamos os dados antigos contidos no token como substituto.

## 8. Adaptador HTTP Java

Novo arquivo proposto: `src/main/java/br/com/eventsbymc/eventsapi/infrastructure/adapter/in/web/MinhaContaHandler.java`.

```java
package br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web;

import br.com.eventsbymc.eventsapi.application.exception.TokenInvalidoException;
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.seguranca.ContextoAutenticacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.HttpURLConnection;

public final class MinhaContaHandler implements HttpHandler {
    private final ConsultarMinhaConta consultarMinhaConta;
    private final ObjectMapper objectMapper;

    public MinhaContaHandler(ConsultarMinhaConta consultarMinhaConta, ObjectMapper objectMapper) {
        this.consultarMinhaConta = consultarMinhaConta;
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var claims = ContextoAutenticacao.obter()
                .orElseThrow(() -> new TokenInvalidoException(
                        "Sessão inválida. Entre novamente.", null));
        var conta = consultarMinhaConta.executar(claims.usuarioId());

        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        HttpRespostas.enviarJson(exchange, HttpURLConnection.HTTP_OK, conta, objectMapper);
    }
}
```

O adaptador traduz a requisição HTTP em uma chamada da aplicação. O filtro existente verifica o JWT e preenche `ContextoAutenticacao`; o handler usa apenas esse contexto validado. `Cache-Control: no-store` evita armazenar a resposta da conta em caches.

Não adicionar `/usuarios/me` à lista de rotas públicas. Ela é diferente de `/usuarios` na comparação exata do filtro atual. Não é necessário exigir `PARTICIPANTE`: essa consulta pertence a qualquer conta autenticada.

O roteador atual já suporta o caminho fixo `/usuarios/me`. Rotas variáveis, como `/eventos/{id}`, continuam precisando de uma evolução própria.

## 9. Montar e registrar o novo endpoint

Em `CompositionRoot.java`, adicionar estes imports:

```java
import br.com.eventsbymc.eventsapi.application.port.in.ConsultarMinhaConta;
import br.com.eventsbymc.eventsapi.application.usecase.ConsultarMinhaContaUseCase;
```

Adicionar este campo na classe, junto aos demais:

```java
public final ConsultarMinhaConta consultarMinhaConta;
```

Dentro do construtor, depois de inicializar `usuarioRepository`:

```java
this.consultarMinhaConta = new ConsultarMinhaContaUseCase(usuarioRepository);
```

Em `EventsApiApplication.java`, adicionar o import:

```java
import br.com.eventsbymc.eventsapi.infrastructure.adapter.in.web.MinhaContaHandler;
```

Substituir somente o trecho de registro das rotas pelo seguinte, preservando as duas rotas já existentes:

```java
Router router = new Router()
        .registrar("POST", "/usuarios", new UsuarioHandler(raiz.registrarUsuarioUseCase, raiz.objectMapper))
        .registrar("POST", "/auth/login", new AutenticacaoHandler(raiz.autenticarUsuario, raiz.objectMapper))
        .registrar("GET", "/usuarios/me", new MinhaContaHandler(raiz.consultarMinhaConta, raiz.objectMapper));
```

São trechos de inserção, não arquivos Java completos. Os demais campos, handlers, filtro e tratamento global permanecem necessários.

## 10. Abrir a conta para qualquer usuário autenticado

Atualmente `/conta` redireciona para uma área que exige `PARTICIPANTE`, mas o cadastro cria `VISITANTE`. Para o exemplo funcionar com uma conta nova, a rota da conta precisa depender de autenticação, não de um perfil operacional.

Em `frontend/src/router/meta.d.ts`, adicionar `requiresAuth?: boolean` à interface existente:

```ts
import 'vue-router'
import type { PerfilUsuario } from '@/features/auth/types/perfil'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    minRole?: PerfilUsuario
    dashboard?: boolean
    title?: string
  }
}

export {}
```

Em `frontend/src/router/index.ts`, substituir a rota raiz que hoje redireciona `/conta` por:

```ts
{
  path: '/conta',
  name: 'account',
  component: () => import('../views/AccountView.vue'),
  meta: { title: 'Minha conta', requiresAuth: true },
},
```

Substituir também a rota filha `conta` de `/participante`, removendo dela o nome `account` e o componente, para não duplicar nomes de rota:

```ts
{ path: 'conta', redirect: { name: 'account' } },
```

Substituir o guard atual por:

```ts
router.beforeEach((to) => {
  const auth = useAuthStore(pinia)
  const minimo = to.matched.reduce(
    (perfil, record) => record.meta.minRole ?? perfil,
    to.meta.minRole,
  )

  document.title = `${to.meta.title ?? 'EventsByMc'} | EventsByMc`

  if ((to.meta.requiresAuth || minimo) && !auth.autenticado) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (minimo && !possuiNivel(auth.perfil, minimo)) {
    return { name: 'access-denied', query: { destino: to.fullPath } }
  }
  return true
})
```

O guard melhora a navegação. A segurança continua no servidor, pois qualquer pessoa pode tentar chamar uma API sem passar pelo Vue Router. A hierarquia dos demais painéis continua como está; alinhar seus perfis com a API antes de integrar operações administrativas. [Referência de metadados do Vue Router](https://router.vuejs.org/guide/advanced/meta.html).

O exemplo pode ser acessado diretamente em `/conta`. Posteriormente, adicionar um `RouterLink` para `{ name: 'account' }` ao menu autenticado.

## 11. Tela Vue com carregamento, erro e dados reais

Proposta didática para `frontend/src/views/AccountView.vue`. Ela mantém Bootstrap e mostra apenas dados fornecidos por esta API. Edição, foto e contadores ainda não terão persistência nessa etapa.

```vue
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { consultarMinhaConta, type MinhaConta } from '@/features/account/services/contaService'
import { ApiError } from '@/shared/services/httpClient'

const conta = ref<MinhaConta | null>(null)
const carregando = ref(true)
const erro = ref('')

async function carregarConta(): Promise<void> {
  carregando.value = true
  erro.value = ''
  conta.value = null

  try {
    conta.value = await consultarMinhaConta()
  } catch (falha) {
    erro.value = falha instanceof ApiError
      ? falha.message
      : 'Não foi possível conectar ao servidor. Tente novamente.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregarConta)
</script>

<template>
  <div class="container py-5">
    <h1 class="h3 mb-4">Minha conta</h1>

    <p v-if="carregando" role="status">Carregando seus dados...</p>

    <div v-else-if="erro" class="alert alert-danger" role="alert">
      <p>{{ erro }}</p>
      <button class="btn btn-outline-danger" type="button" @click="carregarConta">
        Tentar novamente
      </button>
    </div>

    <section v-else-if="conta" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        <h2 class="h5">Dados pessoais</h2>
        <dl class="row mb-0">
          <dt class="col-sm-3">Nome</dt>
          <dd class="col-sm-9">{{ conta.nome }}</dd>
          <dt class="col-sm-3">E-mail</dt>
          <dd class="col-sm-9">{{ conta.email }}</dd>
        </dl>
      </div>
    </section>
  </div>
</template>
```

`ref` cria um estado reativo. `onMounted` inicia a consulta quando o componente entra na página. `await` espera a resposta sem bloquear a interface. `try/catch/finally` separa sucesso, erro e encerramento do carregamento. No script usamos `.value`; o template acessa o valor automaticamente.

Usar interpolação (`{{ conta.nome }}`), sem `v-html`, para os textos da conta. O frontend continua uma SPA: a navegação troca componentes e a consulta HTTP atualiza os dados na página.

## 12. Verificação quando os exemplos forem implementados

Os comandos abaixo são orientações futuras, não evidência de testes executados. Usar um PostgreSQL de desenvolvimento com as migrações aplicadas. Os testes de integração atuais escrevem dados e não devem apontar para o banco remoto de usuários reais.

Ordem de implementação e validação:

1. Configurar JDK 21 e ambiente local.
2. Criar resultado, porta, caso de uso e handler; registrar `/usuarios/me`.
3. Implementar cliente HTTP, configuração do `main.ts` e serviço de conta.
4. Ajustar a rota de conta e o guard.
5. Ligar a tela e verificar pelo navegador.
6. Executar os testes automatizados e o build.

| Cenário | Resultado esperado |
|---|---|
| Cadastro e login existentes | Mesmo contrato, sem regressão |
| Acessar `/conta` sem sessão | Login com `redirect=/conta` |
| Login e acesso à conta | GET com Bearer; dados vindos do banco |
| Recarregar a página | Nova consulta à API usando a sessão atual |
| Chamada direta sem token | 401 do backend |
| Token expirado | Sessão limpa e retorno ao login |
| Erro 403 de outra operação | Mensagem de acesso negado, sessão preservada |
| API indisponível | Mensagem de erro e possibilidade de tentar novamente |
| Proxy devolve HTML | Cliente rejeita o conteúdo inesperado |
| Conta removida | 401; nenhuma resposta com dados antigos do token |

Na aba Network, verificar URL, método, status e formato da resposta. Tokens e dados pessoais não devem ser copiados para logs ou compartilhados em capturas.

Testes a escrever junto da futura implementação:

- JUnit do caso de uso com um `UsuarioRepository` falso: busca pelo ID autenticado, consulta de conta inexistente e resultado sem hash.
- Teste HTTP local: `/usuarios/me` retorna 401 sem token e 200 com token válido, sem expor senha/hash.
- Vitest do cliente: Bearer somente em chamada autenticada, headers existentes preservados, cadastro em texto preservado, 401 aciona callback, 403 não aciona e resposta HTML é rejeitada.
- Teste da tela: carregamento, sucesso, erro e nova tentativa.

No frontend, `npm run test:run` executa o Vitest uma vez; `npm run build` confere tipos e gera o bundle. No backend, `./mvnw.cmd test` executa os testes com JDK adequado. A suíte de integração atual é condicional às variáveis do banco: conferir testes ignorados no relatório. O lint atual usa `--fix`; ao pedir apenas diagnóstico, executar as ferramentas sem esse parâmetro. Não instalar bibliotecas adicionais apenas para este exemplo.

## 13. Aplicar o padrão às próximas funcionalidades

Esta entrega implementaria somente leitura da conta autenticada. As rotas abaixo continuam sendo planejamento:

| Fluxo | Endpoint externo proposto | Pré-requisitos |
|---|---|---|
| Catálogo | `GET /api/eventos` | Caso de uso/consulta de publicados, DTO e liberação pública por método+caminho |
| Detalhe | `GET /api/eventos/{id}` | Roteador com UUID, DTO de detalhe, 404 para evento não visível |
| Atualização da conta | `PATCH /api/usuarios/me` | Validação no servidor, unicidade de e-mail e persistência |
| Criação de evento | `POST /api/eventos` | Perfil autorizado, regras e período completo |
| Inscrição | `POST /api/eventos/{id}/inscricoes` | Domínio/tabelas de inscrição e controle transacional de vagas |
| Foto da conta | `PUT /api/usuarios/me/foto` | Contrato de upload, validação de arquivo e armazenamento binário no banco |

Para integrar eventos, não basta trocar `eventosMock` por `fetch`:

- IDs Java são UUIDs; atualizar os tipos TypeScript e a leitura de `route.params.id` para string, incluindo mocks/testes remanescentes.
- Definir datas com fuso, início e fim, campos opcionais e o que a API realmente fornece. Não inventar vagas, categoria ou preço para preencher o layout.
- Expor DTOs com dados públicos; nunca serializar `Evento` com seu `Usuario` completo.
- O repositório atual recarrega atividades chamando uma operação bloqueada em eventos encerrados. Corrigir a reconstrução antes de usar essa listagem como base indiscriminada para o catálogo.
- O filtro atual libera rotas somente por caminho. Evoluir a regra para método+caminho ao abrir o catálogo e proteger operações de escrita.
- Preservar a busca sem acentos quando os filtros forem transferidos para o servidor; testar `simposio` encontrando `Simpósio`.
- Quando houver paginação no servidor, não filtrar somente a página já carregada no Vue como se ela representasse todo o catálogo.

As imagens continuarão planejadas para PostgreSQL. O frontend envia o arquivo à API e recebe uma referência de mídia. Não haverá URL de upload para S3 nesta etapa. Os atuais tipos `SessaoUpload` são resquícios do planejamento anterior e devem ser revistos quando essa funcionalidade for implementada.
