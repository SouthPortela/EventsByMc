# Decisão 002 — Chamada de presença por atividade

## Decisão confirmada com o responsável pelo frontend

O organizador exibe um QR Code e um código digitável. O participante abre o link
pela câmera nativa do celular ou digita o código no webapp. A confirmação é única
por atividade, exige inscrição ativa no evento e chamada válida por cinco minutos.

O QR não identifica um participante: é compartilhado pelos presentes à atividade.
O usuário é identificado pelo JWT validado no servidor. Abrir o link é somente
uma navegação; gravar presença exige uma confirmação explícita por POST.

O organizador abre a chamada quando julgar adequado em um evento publicado.
A primeira versão não restringe a emissão ao horário da atividade, pois ainda
falta definir a política de fuso do cronograma. A janela efetiva começa no relógio
do banco no instante da emissão.

## Consequências

- Uma atividade pode ter várias chamadas históricas, mas somente a mais recente
  permanece não revogada. Emitir novamente invalida o código anterior.
- Uma chamada atende vários participantes. A presença é única por usuário/atividade,
  não por usuário/chamada: renovar o código não permite duplicar a marcação.
- Código aleatório de 12 caracteres, hash SHA-256 persistido, validade curta e limite
  de tentativas por conta. O código não substitui autenticação e não é usado como JWT.
- Digitação pelo participante e leitura do QR têm as mesmas regras. Origem é um
  metadado informativo, não prova de localização.
- SQL preserva vínculos e unicidade; a operação transacional verifica estado do
  evento, validade/revogação e inscrição ativa antes de gravar.
- Uma foto do código pode ser compartilhada durante a validade. Supervisão continua
  necessária; não se promete prova técnica de presença física.

Entrada/saída, cálculo de percentual, correção manual auditada por organizador,
relatórios e seleção/reserva de atividades ficam para etapas seguintes.

Detalhes operacionais: [banco e QR](../../db/README.md).
Contratos: [API de presença](../api/presenca.md).
