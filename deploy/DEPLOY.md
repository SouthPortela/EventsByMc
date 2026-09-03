# Deploy — eventos.monkeycorp.com.br

Passos para rodar no servidor (via SSH), depois que este repositório tiver sido
atualizado no GitHub com o Dockerfile, docker-compose.yml e pom.xml corrigidos.

## 1. Confirmar Docker disponível

```bash
docker --version
docker compose version
```

## 2. Clonar o projeto

```bash
mkdir -p ~/apps && cd ~/apps
git clone https://github.com/SouthPortela/EventsByMc.git eventos-api
cd eventos-api
git checkout dev
```

Se o projeto já existir no servidor, use `git pull` em vez de clonar de novo.

## 3. Criar o `.env` de produção

**Nunca reaproveite os valores de `.env.example`.**

```bash
cp .env.example .env
nano .env
```

Preencha assim:

```
DB_NAME=eventos
DB_USERNAME=eventos_user
DB_PASSWORD=<gerar com: openssl rand -base64 24>
DB_URL=jdbc:postgresql://postgres-db:5432/eventos
JWT_SECRET=<gerar com: openssl rand -hex 32>
JWT_EXPIRATION_MINUTES=60
```

Note que `DB_URL` usa `postgres-db` como host (nome do serviço no `docker-compose.yml`),
não `localhost` — os dois containers conversam pela rede interna do Docker.

## 4. Subir os containers

```bash
docker compose --env-file .env up -d --build
docker compose ps
docker logs events_api --tail 30
```

## 5. Testar localmente no servidor (antes de expor pra internet)

```bash
curl -X POST http://localhost:8080/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@servidor.com","senha":"senha1234"}'
```

## 6. Configurar o Nginx

```bash
sudo cp deploy/eventos.monkeycorp.com.br.conf /etc/nginx/sites-available/eventos.monkeycorp.com.br
sudo ln -s /etc/nginx/sites-available/eventos.monkeycorp.com.br /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

## 7. Liberar as portas (firewall do servidor, se `ufw` estiver ativo)

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw status
```

Se o servidor estiver atrás de um roteador doméstico (NAT), também é preciso
redirecionar as portas **80** e **443** do roteador para o IP local do servidor
(o mesmo `192.168.8.99` usado no SSH) — sem isso, o tráfego externo nunca chega
no Nginx, mesmo com o DNS e o firewall do servidor corretos.

## 8. HTTPS com Let's Encrypt

```bash
sudo certbot --nginx -d eventos.monkeycorp.com.br
```

## 9. Testar de fora (do seu computador, não do servidor)

```bash
curl -X POST https://eventos.monkeycorp.com.br/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste Externo","email":"externo@teste.com","senha":"senha1234"}'
```

## Atualizações futuras

```bash
cd ~/apps/eventos-api
git pull
docker compose --env-file .env up -d --build
```
