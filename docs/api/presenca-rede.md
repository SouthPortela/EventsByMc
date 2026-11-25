# QR Code de presença na rede local

O QR abre o site no celular usando o endereço do computador que hospeda o sistema.
Por exemplo, se o DHCP entregar `192.168.1.4` e o site usar a porta 8081,
o destino será `http://192.168.1.4:8081/presenca#codigo=...`.
A porta e o caminho do site são preservados. O IP não é fixado no código.

## Site no Docker

Na raiz do projeto, depois de iniciar ou reconstruir os contêineres:

```powershell
docker compose up -d --build
npm run presenca:rede
```

O segundo comando requer Node.js e Docker Compose no computador que hospeda o site.
Ele detecta a interface de rede do host e grava somente o IP em
`presenca-rede.json` no frontend em execução. Não altera o Compose, o banco,
as credenciais ou a configuração da API. Nenhuma dependência npm é necessária
para executar esse comando.

O Java dentro do Docker vê as interfaces do contêiner, por isso não é usado para
descobrir o endereço físico do computador. O comando deve ser executado no próprio
host do Docker, usando o mesmo projeto/contexto Compose.

Repita `npm run presenca:rede` após mudança de IP por DHCP ou recriação do
contêiner frontend. Não é necessário reconstruir as imagens somente para trocar
o IP. Depois gere uma nova chamada; o QR já exibido mantém seu endereço original.

Se o site já foi aberto pelo IP da rede, esse endereço é usado diretamente.
Também é possível preencher **Endereço do site para o celular** na tela de chamada,
incluindo a porta quando diferente de 80/443. Esse campo vale para a tela aberta
e prevalece sobre a descoberta automática. Deixe-o vazio para usar a descoberta.

## Aplicativo Windows e desenvolvimento

No Electron empacotado, o processo principal consulta novamente a interface do
computador a cada geração, inclusive após mudanças de DHCP. O Vue recebe somente
o IP por uma leitura local de `presenca-rede.json`, sem acesso a Node, IPC ou tokens.
Recompile o aplicativo para incorporar esta funcionalidade.

`DESKTOP_PUBLIC_APP_URL` continua definindo protocolo, porta e caminho do site.
Com o padrão `http://127.0.0.1/`, apenas o host é substituído pelo IP da rede;
o celular acessa o Nginx Docker na porta 80, não o protocolo interno do Electron.
Se `WEB_PORT` for diferente, mantenha a mesma porta em `DESKTOP_PUBLIC_APP_URL`.
Um domínio público configurado é preservado.

No desenvolvimento web, o Vite atende na rede local e fornece a mesma descoberta.
O desenvolvimento desktop continua apontando para o site Docker configurado em
`.env.desktop`, mesmo quando sua interface é servida pelo Vite em loopback.

A descoberta prioriza a interface da rota do sistema e ignora adaptadores
identificados como VPN, Docker ou virtuais. Sem uma escolha segura, a tela pede
um endereço manual em vez de gerar QR de loopback. A consulta de rota usa um
socket UDP sem enviar pacotes.

## Acesso e segurança

Computador e celular precisam ter conectividade entre si. A porta do site deve
estar acessível no firewall; redes Wi-Fi com isolamento de clientes podem impedir
o acesso. O QR não abre portas nem altera o firewall automaticamente.

O endereço final é exibido abaixo do QR. Não são aceitos endereços de loopback,
protocolos diferentes de HTTP/HTTPS, credenciais, query ou fragmento no campo manual.
`VITE_PUBLIC_APP_URL`, quando define um endereço público, continua tendo prioridade
sobre a descoberta automática.

A API de presença e o banco não mudam: geração e confirmação continuam exigindo
JWT, autorização e código temporário. O código permanece no fragmento do link,
sem token de sessão no QR. Abrir o link continua exigindo login do participante
e confirmação da presença.
