# Banco, migrações e dados demonstrativos

PostgreSQL 16, acesso por JDBC. A API não aplica migrações automaticamente.
As alterações ficam em src/main/resources/db/migration e o estado aplicado em
versoes_schema. Não há ORM nem Flyway.

## Versões

| Versão lógica | Conteúdo |
|---|---|
| 1 | Usuários, perfis, eventos e atividades |
| 2 | Inscrições, chamadas temporárias, presenças e limites |
| 3 | Perfis autenticados; VISITANTE passa a ser apenas anônimo |
| 4 | Categoria explícita de evento |
| 5 | Programação, trilhas, espaços, pessoas, agenda e política de inscrição |
| 6 | Questionários, respostas e mensagens |
| 7 | Certificados |
| 8 | Políticas de frequência e marcações manuais |
| 9 | Questionários por atividade |
| 10 | Administração, bloqueio de eventos e auditoria |
| 11 | Edição, conflitos de espaços, ajustes de frequência e histórico |

A versão lógica 10 está em V9a__administracao_e_bloqueio_eventos.sql; a 11 está em
V9b__edicao_frequencia_historico.sql.
O nome preserva a ordenação alfabética dos scripts do init do PostgreSQL.
Não renomeie migrações já distribuídas.

## Banco vazio

Crie o banco e configure a conexão antes de executar. Na raiz, substituindo os
parâmetros da conexão pelo ambiente de desenvolvimento:

    psql -X -h localhost -U SEU_USUARIO -d SEU_BANCO -W -f db/inicializar.sql

O inicializador inclui V1 até a versão lógica 11 em uma transação. Não cria o banco,
usuários administrativos ou mocks. No Docker, as migrações são montadas em
/docker-entrypoint-initdb.d e executadas automaticamente apenas em volume vazio.

## Banco existente

Faça backup e consulte:

    SELECT * FROM versoes_schema ORDER BY versao;

Execute somente as versões pendentes, em ordem: db/atualizar-v2.sql até
db/atualizar-v11.sql. Por exemplo, banco na versão 8 precisa das entradas 9, 10 e 11.
Cada entrada é para psql e usa transação com interrupção em erro. Se o schema foi
alterado manualmente, compare antes de aplicar. Não reaplique versões registradas.

No Docker, use um terminal do banco:

    docker compose exec postgres-db sh
    psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"

Dentro do psql, aplique uma migração pendente por vez, por exemplo a versão 11:

    \set ON_ERROR_STOP on
    BEGIN;
    \i /docker-entrypoint-initdb.d/V9b__edicao_frequencia_historico.sql
    COMMIT;

Confirme que a versão máxima é 11. Nunca use docker compose down -v para atualizar:
isso remove o banco persistido. Os comandos \i, \ir e \set são específicos do psql;
no pgAdmin execute os conteúdos SQL pendentes, na ordem, dentro de transação.
Em erro, faça ROLLBACK e investigue antes de continuar.

## Primeiro administrador

Cadastre uma conta normalmente pelo site. Com acesso administrativo ao banco,
confirme o usuário e conceda o perfil, usando variável psql:

    \set email 'admin@exemplo.test'
    SELECT id, email FROM usuarios WHERE email = :'email';
    BEGIN;
    INSERT INTO usuario_perfis(usuario_id, perfil)
    SELECT id, 'ADMINISTRADOR' FROM usuarios WHERE email = :'email'
    ON CONFLICT DO NOTHING;
    COMMIT;

Só prossiga se a consulta identificar a conta pretendida. Saia e entre novamente
no site para atualizar token e menus. Depois da preparação inicial, a administração
pode alterar perfis pela API/tela; a operação é auditada e protege o último administrador.
O cadastro público nunca concede ADMINISTRADOR.

## Dados demonstrativos

Use uma instância de desenvolvimento com schema 11, nunca dados reais de participantes.
Na raiz, configure .env conforme .env.example e disponibilize JDK 21 e as dependências
Java (por exemplo, compilando com o Maven Wrapper):

    .\mvnw.cmd package
    .\db\Popular-Demo.ps1

O script compila/executa DemoDataSeeder e cria 100 participantes, 20 organizadores,
50 eventos e 100 atividades, com UUIDs estáveis. Lê DB_NAME, DB_USERNAME, DB_PASSWORD
e DB_PORT do .env local, acessando o PostgreSQL em loopback. A senha das contas
fictícias é gerada em .env.demo, que não deve ser versionado.

Depois da carga inicial, aplique o complemento:

    psql -X -h localhost -U SEU_USUARIO -d SEU_BANCO -W -f db/mocks.sql -v ON_ERROR_STOP=1

mocks.sql não cria as contas/eventos iniciais: verifica sua existência e adiciona
inscrições ativas/canceladas, agenda, frequência completa/parcial, faltas, avaliações,
certificados elegíveis e mensagens. Preserva senhas e evita duplicar registros.
A API não executa essas ferramentas ao iniciar. Não há conta administrativa no seeder.

## Presença e conclusão

O QR usa código temporário de 12 caracteres, separado do JWT de login. O banco guarda
somente SHA-256. Chamada dura cinco minutos; nova emissão revoga a anterior.
Geração e confirmação exigem JWT, autorização e validações transacionais. A presença
é única por participante/atividade. Compartilhar foto do QR ainda é uma limitação física.

Políticas manuais usam registros de confirmação ou entrada/saída. A conclusão é
calculada pelo relatório: inscrição ativa, evento encerrado, atividades obrigatórias
e frequência mínima. Não altere o cadastro do usuário para simular conclusão.

O celular precisa alcançar o site: localhost aponta para o próprio celular. Configure
VITE_PUBLIC_APP_URL com um endereço acessível quando necessário. O código do QR fica
no fragmento do link; abrir o endereço não grava presença sem confirmação explícita.

## Testes SQL e integração

Os testes SQL usam PostgreSQL efêmero em memória e não leem credenciais do banco:

    npm install --prefix target/sql-validation --no-save --package-lock=false @electric-sql/pglite
    node --test db/tests/schema.mjs db/tests/edicao-historico.mjs

A dependência de teste fica em target, não no produto. Uma limpeza Maven remove essa
pasta; reinstale antes de repetir os testes SQL. A suíte cobre migrações, vínculos,
restrições e consultas dos adaptadores, incluindo administração. Não substitui testes
de concorrência com PostgreSQL real ou a conferência de QR em celular.

Testes JDBC/HTTP condicionais exigem DB_URL, DB_USERNAME e DB_PASSWORD; configure
somente um banco dedicado a testes, pois criam e removem registros. Para problemas
de diretório temporário do JDK em Windows, use um caminho gravável em
-DargLine=-Djdk.net.unixdomain.tmpdir=CAMINHO_TEMPORARIO.

Contratos: [presença](../docs/api/presenca.md),
[funcionalidades](../docs/api/requisitos-funcionais-webapp.md) e
[administração](../docs/api/administracao.md).

Detalhes e contratos novos: [edição e histórico](../docs/api/edicao-historico.md).
