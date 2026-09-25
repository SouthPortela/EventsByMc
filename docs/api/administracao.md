# Administração e moderação

Todas as rotas abaixo exigem JWT com perfil `ADMINISTRADOR`. A API consulta o perfil
atual no banco também: revogar o perfil bloqueia a próxima operação, mesmo com JWT
ainda válido. O frontend usa o prefixo `/api`; o servidor Java registra os caminhos
sem esse prefixo.

| Método e rota do webapp | Função |
|---|---|
| `GET /api/admin/eventos` | Todos os eventos, inclusive rascunhos, suspensos e excluídos |
| `GET /api/admin/eventos/{id}` | Detalhes e atividades do evento |
| `GET /api/admin/usuarios` | Usuários, perfis e e-mail mascarado |
| `GET /api/admin/moderacoes` | Histórico de decisões de moderação |
| `POST /api/admin/eventos/{id}/suspensao` | Retira evento de circulação |
| `POST /api/admin/eventos/{id}/restauracao` | Devolve evento suspenso ao estado de rascunho |
| `DELETE /api/admin/eventos/{id}` | Exclui logicamente e remove conteúdo textual |

As três operações de escrita exigem JSON `{"motivo":"Justificativa com 10 a 500 caracteres"}`.
Suspender, restaurar e excluir registram ator, estado anterior/novo, motivo e horário
na mesma transação da mudança. Concorrência gera `409`. Restaurar nunca republica
automaticamente; o organizador precisa revisar o rascunho. A exclusão é terminal.
O administrador pode consultar inscritos e frequência de qualquer evento pelos
endpoints de relatório já existentes, usando o ID obtido na lista global.

O estado `EXCLUIDO` preserva o ID e os relacionamentos históricos necessários a
inscrições, presenças e certificados, mas remove título, descrição e local do evento,
atividades, questionários e mensagens. O registro de auditoria permanece. Eventos
`SUSPENSO` e `EXCLUIDO` não aparecem no catálogo nem podem receber novas inscrições
ou confirmações de presença. O backend ainda não armazena banners/fotos: ao criar
essa persistência, a exclusão deve também remover os binários associados.

A migração é V10 (`db/atualizar-v10.sql`). Em volumes PostgreSQL Docker já
inicializados, `git pull` e `docker compose up` **não** executam essa migração.
Faça backup, verifique `versoes_schema` e aplique somente V10 antes de iniciar a
API atualizada. Em banco vazio, `db/inicializar.sql` inclui a versão; o arquivo
da migração tem prefixo `V9z` apenas para seguir V9 na ordem alfabética do
inicializador Docker existente.
