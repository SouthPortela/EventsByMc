# Modelo de domínio — EventsByMc

Este modelo distingue conceitos do produto das classes efetivamente implementadas.
A persistência relacional está nas migrações; o domínio Java não depende de JDBC ou HTTP.

## Conceitos e implementação

| Conceito | Representação atual |
|---|---|
| Conta e perfis | Usuario, Pessoa, Perfil e PoliticaPerfis; perfis múltiplos |
| Visitante | Navegação anônima, sem perfil persistido em usuario_perfis |
| Evento | Evento, CategoriaEvento e EstadoEvento |
| Programação | Programacao e Atividade; tipos, espaços, trilhas e pessoas nos contratos de programação |
| Inscrição e agenda | Registros SQL coordenados por PresencaUseCase e ProgramacaoUseCase |
| Frequência | RegrasChamada, PoliticaFrequencia, presencas e registros_frequencia |
| Questionários e avaliações | Contratos DadosAvaliacao, AvaliacoesUseCase e tabelas correspondentes |
| Relatórios e certificados | RelatoriosUseCase, CertificadosUseCase e portas de saída |
| Administração | AdministracaoUseCase, PoliticaPerfis e auditoria_administrativa |

Participante e organizador são papéis de Usuario, não classes obrigatórias separadas.
Inscrição, agenda e relatório são conceitos do modelo, sem exigir uma entidade Java
com o mesmo nome de cada tabela.

## Relacionamentos e regras

- Um organizador possui eventos; cada evento compõe atividades.
- Atividades podem vincular trilha, espaço e pessoas com papéis.
- Uma inscrição relaciona usuário e evento, com estado ATIVA ou CANCELADA.
- Agenda seleciona atividades de inscrições ativas, respeitando vagas e conflitos.
- Frequência relaciona usuário, atividade e inscrição; registros preservam o histórico.
- Um questionário pertence ao evento e pode especificar uma atividade desse evento.
- Avaliação reúne respostas de um participante elegível, uma vez por questionário.
- Certificados de participante exigem evento encerrado, inscrição ativa e frequência
  mínima; declarações de palestrante/apresentador possuem destinatário próprio.

## Estados de evento

RASCUNHO pode ser publicado; PUBLICADO pode ser encerrado. BLOQUEADO guarda o
estado anterior e o motivo. Desbloquear restaura esse estado, sem apagar inscrições,
presenças ou outros registros históricos. O catálogo público só mostra publicados.

## Frequência e conclusão

CHECKIN_UNICO usa presença via QR/código. VALIDACAO_MANUAL usa CONFIRMACAO.
ENTRADA_SAIDA exige ambas as marcações e saída posterior à entrada.
PERCENTUAL_PERMANENCIA também compara a duração com o percentual da atividade.

O relatório calcula a proporção de atividades obrigatórias confirmadas. APROVADO
exige inscrição ativa, evento encerrado, ao menos uma atividade obrigatória e
percentual mínimo atingido. Outras situações são CANCELADA, EM_ANDAMENTO e
INSUFICIENTE. Não existe coluna de conclusão no cadastro do usuário.

Referências: [responsabilidades](mapa-responsabilidades.md),
[modelagem](../decisoes/decisao-001-modelagem.md) e
[contratos](../api/requisitos-funcionais-webapp.md).

## Evolução da frequência e histórico

OcupacaoEspaco representa o conflito de intervalos de uma sala. A view de frequência
efetiva considera os registros originais e o último ajuste justificado. Eventos
encerrados preservam dados, programação e percentual mínimo; retificações são
novos fatos auditáveis, sem apagar o passado. Veja os [contratos](../api/edicao-historico.md).
