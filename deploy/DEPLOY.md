# Deploy — eventos.monkeycorp.com.br

Documenta o setup REAL em produção: Debian 12, disco limitado (algumas
centenas de MB livres — checar com `df -h /` antes de instalar algo novo).
Backend roda como serviço `systemd` (jar), PostgreSQL é nativo (`apt`), Apache2
serve o frontend estático e faz proxy reverso de `/api/*` pro backend, HTTPS
via `certbot`. **Não há Docker em produção** — não coube no disco disponível.
`Dockerfile`/`docker-compose.yml` na raiz do repo servem só pra desenvolvimento
local (ex.: subir um Postgres local rapidamente com `docker compose up postgres-db`).

## Visão geral

```
Internet → Apache (porta 80 redireciona tudo pra 443; 443 termina TLS)
             ├─ /            → estático em /var/www/eventos.monkeycorp.com.br
             │                  (frontend/dist, com FallbackResource pro SPA)
             └─ /api/* → strip /api → 127.0.0.1:8080 (backend, systemd)
                                        └─ Postgres nativo, 127.0.0.1:5432
```

Valores reais confirmados no servidor:
- Serviço systemd do backend: `eventos-api` (`/etc/systemd/system/eventos-api.service`)
- Jar em execução: `/opt/eventos-api.jar`, rodando como usuário de sistema `eventos`
- Variáveis de ambiente do backend: `/etc/eventos-api.env` (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`)
- Banco: `eventos`, usuário dedicado `eventos_user`
- Vhosts Apache: `/etc/apache2/sites-available/eventos.conf` (porta 80) e `eventos-le-ssl.conf` (porta 443, parcialmente gerenciado pelo certbot) — ver `deploy/eventos.monkeycorp.com.br.conf` neste repo para o conteúdo de referência
- Frontend estático: `/var/www/eventos.monkeycorp.com.br`

## 1. Backend — atualizar

```bash
ssh usuario@servidor   # via chave SSH ou senha, conforme configurado
cd ~/apps/eventos-api  # ou onde o repo estiver clonado no servidor
git checkout dev
git pull

# Build local (recomendado) ou no próprio servidor se tiver Java/Maven:
./mvnw -q -DskipTests package
# gera target/gestao-eventos-0.0.1-SNAPSHOT.jar
```

Se o build for feito **localmente** (fora do servidor, para não gastar disco
com Maven/JDK completo lá — só um JRE é necessário pra rodar):

```bash
scp -P <porta> target/gestao-eventos-0.0.1-SNAPSHOT.jar usuario@servidor:/opt/eventos-api.jar
ssh usuario@servidor "chown eventos:eventos /opt/eventos-api.jar && systemctl restart eventos-api"
ssh usuario@servidor "systemctl status eventos-api --no-pager"
ssh usuario@servidor "journalctl -u eventos-api -n 50 --no-pager"
```

## 2. Frontend — build local e deploy dos estáticos

O servidor **não tem Node/npm instalado** (e não deve ter — sem espaço). O
build é feito localmente e só o resultado (`frontend/dist/`, algumas
centenas de KB) é copiado.

```bash
cd frontend
npm install       # ou npm ci
npm run build     # gera frontend/dist/ — usa VITE_API_BASE_URL=/api (padrão do .env.example)
```

Copiar para o servidor:

```bash
scp -P <porta> -r frontend/dist/* usuario@servidor:/var/www/eventos.monkeycorp.com.br/
ssh usuario@servidor "chown -R www-data:www-data /var/www/eventos.monkeycorp.com.br"
```

Não precisa reiniciar nada no servidor depois disso — o Apache serve os
arquivos estáticos diretamente, sem cache de aplicação no meio.

## 3. Testar localmente (antes de expor mudanças)

Para testar o frontend contra a API real sem sofrer bloqueio de CORS
(a API não tem CORS configurado — não precisa, porque em produção tudo fica
atrás do mesmo domínio), `frontend/vite.config.ts` já tem um proxy de
desenvolvimento (`server.proxy['/api']` → `https://eventos.monkeycorp.com.br`,
removendo o prefixo `/api`) — o mesmo comportamento que o Apache faz em
produção. Basta rodar:

```bash
cd frontend
npm run dev
```

E testar cadastro/login normalmente pelo navegador, em `http://localhost:5173`
(ou a porta que o Vite escolher). Qualquer usuário de teste criado deve ser
removido do banco depois (ver seção "Limpeza" abaixo).

## 4. Apache — vhost (só quando mudar algo estrutural, não a cada deploy)

Ver `deploy/eventos.monkeycorp.com.br.conf` neste repositório para a
referência completa dos dois vhosts (porta 80 e 443). Resumo do que faz o
vhost `:443` funcionar:

- `DocumentRoot /var/www/eventos.monkeycorp.com.br` + `FallbackResource /index.html`
  (necessário para rotas do Vue Router em modo history).
- `ProxyPass /api/ http://127.0.0.1:8080/` + `ProxyPassReverse /api/ http://127.0.0.1:8080/`
  (a barra final em ambos os lados remove o prefixo `/api` antes de encaminhar
  pro backend, que não tem esse prefixo nas rotas).

```bash
cp /etc/apache2/sites-available/eventos-le-ssl.conf ~/eventos-le-ssl.conf.bak-$(date +%Y%m%d%H%M%S)   # backup antes de editar
nano /etc/apache2/sites-available/eventos-le-ssl.conf
apache2ctl configtest
systemctl reload apache2   # reload, não restart — não derruba conexões em andamento
```

## 5. Testar de fora

```bash
curl -s https://eventos.monkeycorp.com.br/ | head -5                       # deve devolver o index.html do Vue
curl -s -X POST https://eventos.monkeycorp.com.br/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste Deploy","email":"teste-deploy@example.com","senha":"senha1234"}'
curl -s -X POST https://eventos.monkeycorp.com.br/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste-deploy@example.com","senha":"senha1234"}'
```

## Limpeza — usuários de teste

Qualquer conta criada durante testes (locais ou em produção) deve ser
removida do banco depois:

```bash
ssh usuario@servidor "PGPASSWORD='<senha>' psql -h 127.0.0.1 -U eventos_user -d eventos -c \"DELETE FROM usuarios WHERE email = 'email-de-teste@example.com';\""
```

## Rollback rápido

Frontend: reenviar a versão anterior de `frontend/dist/` (mantenha uma cópia
local antes de sobrescrever, já que não há backup automático no servidor).

Apache: `cp ~/eventos-le-ssl.conf.bak-<timestamp> /etc/apache2/sites-available/eventos-le-ssl.conf && apache2ctl configtest && systemctl reload apache2` — leva segundos.

Backend: `systemctl status eventos-api` — mantenha uma cópia do `.jar`
anterior (`cp /opt/eventos-api.jar /opt/eventos-api.jar.bak` antes de
sobrescrever) para poder reverter rápido com `systemctl restart` apontando
pro jar antigo.
