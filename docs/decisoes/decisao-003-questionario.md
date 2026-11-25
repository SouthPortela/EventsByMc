# Decisão 003 — Questionários configuráveis

## Decisão

Há no máximo um questionário geral por evento e um por atividade. O vínculo com a
atividade é opcional e validado no mesmo evento. A migração V6 introduz avaliações;
a V9 permite questionários por atividade sem perder os gerais existentes.

O organizador dono ou administrador cria o questionário, adiciona questões e publica.
Após publicação, a estrutura fica imutável. Os tipos são TEXTO, ESCOLHA_UNICA e ESCALA.
A aplicação valida obrigatoriedade, opções permitidas e limites da escala.

## Participação e integridade

Responder exige inscrição ativa e frequência válida. No questionário de atividade,
a presença deve ser válida na mesma atividade. Cada usuário responde uma única vez
por questionário; restrições de unicidade e a transação também protegem concorrência.

As respostas ficam ligadas ao questionário e à questão por chaves estrangeiras
compostas. Resultados incluem contagens, distribuição de valores e textos, com
acesso de gestão autorizado. O cliente renderiza respostas textuais sem injetar HTML.

Avaliação não substitui a frequência mínima para certificado. A conclusão é calculada
pelas atividades obrigatórias e regras do evento, conforme o relatório de frequência.

## Contratos

O prefixo geral é /eventos/{id}; o específico é /atividades/{id}. Consulte os
sufixos de criação, publicação, resposta e resultados em
[requisitos-funcionais-webapp.md](../api/requisitos-funcionais-webapp.md).
