# Glossário do Domínio

## 1. Evento

Representa um evento organizado pela plataforma, contendo informações como título, descrição, período, local ou modalidade, estado e regras gerais.

Um evento pode possuir diversas atividades.

---

## 2. Atividade

Representa uma atividade que ocorre dentro de um evento.

Pode representar uma palestra, oficina, apresentação, pôster, mesa, produto ou outro tipo de atividade.

Uma atividade possui horário, local, capacidade opcional, pessoas vinculadas e regras próprias de frequência e avaliação.

---

## 3. Usuário

Representa uma pessoa que possui uma conta na plataforma.

Um usuário possui dados de identificação, credenciais de acesso e um perfil de autorização.

---

## 4. Administrador

Representa o usuário responsável pelas operações administrativas gerais da plataforma.

Pode gerenciar usuários organizadores e consultar informações dos eventos.

---

## 5. Organizador

Representa o usuário responsável pela administração operacional de um evento.

Pode criar e manter eventos, atividades, inscrições, regras de frequência, avaliações e relatórios.

---

## 6. Participante

Representa o usuário que participa de um evento.

Pode realizar inscrições, selecionar atividades, consultar sua agenda, registrar presença e responder avaliações quando atender aos critérios de elegibilidade.

---

## 7. Visitante

Representa uma pessoa que acessa as informações públicas da plataforma sem estar autenticada.

Pode consultar eventos e iniciar o processo de cadastro e inscrição.

---

## 8. Inscrição

Representa a relação entre um participante e um evento.

A inscrição possui uma situação que permite identificar, por exemplo, se está ativa ou cancelada.

---

## 9. Atividade Inscrita

Representa a participação de um participante em uma atividade específica.

É utilizada quando o evento permite que o participante escolha atividades individualmente.

---

## 10. Agenda

Representa o conjunto de atividades selecionadas pelo participante.

A agenda deve apresentar as atividades em ordem cronológica e respeitar as regras de conflito de horários.

---

## 11. Local

Representa o espaço físico ou modalidade onde uma atividade será realizada.

Pode representar, por exemplo, um auditório, sala, laboratório ou atividade remota.

---

## 12. Trilha

Representa um agrupamento de atividades relacionadas por tema, área ou categoria.

Permite organizar e filtrar a programação.

---

## 13. Pessoa

Representa uma pessoa vinculada a uma atividade.

Uma pessoa pode exercer diferentes papéis, como palestrante, apresentador ou responsável.

---

## 14. Papel na Atividade

Representa a função exercida por uma pessoa dentro de uma atividade.

Exemplos:

- Palestrante;
- Apresentador;
- Responsável.

---

## 15. Frequência

Representa os registros de participação de um participante em determinada atividade.

A frequência contém informações como participante, atividade, data, hora, origem e responsável quando o lançamento for manual.

---

## 16. Política de Frequência

Define como a presença de um participante será registrada e calculada em uma atividade.

Exemplos:

- Check-in único;
- Entrada e saída;
- Validação manual;
- Percentual de permanência.

A política deverá ser configurável por atividade.

---

## 17. Registro de Frequência

Representa uma ocorrência individual de marcação de presença.

Pode ser originada por QR Code ou lançamento manual.

---

## 18. QR Code

Representa o mecanismo utilizado para iniciar ou validar uma operação de frequência.

O QR Code não deverá armazenar senha ou dados pessoais sensíveis em texto aberto.

---

## 19. Questionário

Representa um conjunto de perguntas utilizado para avaliar uma atividade ou apresentação.

Um questionário pode possuir diferentes tipos de perguntas.

---

## 20. Pergunta

Representa uma questão pertencente a um questionário.

O sistema deverá suportar pelo menos:

- Resposta textual;
- Escolha única;
- Escala numérica.

---

## 21. Resposta

Representa a informação fornecida por um participante para uma pergunta de um questionário.

A resposta deverá respeitar o tipo e as regras da pergunta.

---

## 22. Avaliação

Representa o processo pelo qual um participante elegível responde a um questionário relacionado a uma atividade.

Somente participantes inscritos e com presença validada poderão avaliar.

---

## 23. Relatório

Representa uma consolidação de informações do sistema para consulta ou exportação.

Exemplos:

- Relatório de inscritos;
- Relatório de frequência;
- Relatório de participação.

---

## 24. Certificado

Documento que comprova a participação do usuário em determinado evento ou atividade.

A geração de certificados é considerada uma funcionalidade desejável da primeira versão.

---

## 25. Regra de Inscrição

Define as condições necessárias para que um participante possa se inscrever em um evento ou atividade.

Pode determinar se:

- Basta a inscrição no evento;
- É necessário escolher atividades;
- Existem limites de vagas;
- Existe prazo para cancelamento.

---

## 26. Capacidade

Representa a quantidade máxima de participantes permitida em determinada atividade ou evento quando o controle de vagas estiver ativado.

---

## 27. Conflito de Horário

Ocorre quando duas atividades selecionadas pelo participante possuem horários incompatíveis.

O sistema deverá seguir uma política uniforme para impedir ou alertar sobre a situação.

---

## 28. Estado do Evento

Representa a situação atual de um evento.

O estado influencia sua visibilidade e a possibilidade de realização de inscrições.

---

## 29. Elegibilidade

Representa a condição necessária para que um participante possa realizar determinada ação.

Exemplo:

Um participante somente poderá avaliar uma atividade se estiver inscrito e possuir presença validada.

---

## 30. Política

Representa uma regra configurável que define como determinado comportamento do sistema deverá funcionar.

No Events by MC, políticas poderão ser utilizadas para representar variações de inscrição, frequência, avaliação e certificação.

---

# Relação entre os principais conceitos

O domínio pode ser inicialmente compreendido da seguinte forma:

**Evento**
→ possui **Atividades**

**Atividade**
→ possui **Local**
→ possui **Horário**
→ pode possuir **Trilha**
→ possui **Pessoas vinculadas**
→ possui **Política de Frequência**
→ pode possuir **Questionário**

**Participante**
→ possui **Inscrição**
→ possui **Agenda**
→ possui **Registros de Frequência**
→ pode possuir **Respostas**

**Questionário**
→ possui **Perguntas**
→ recebe **Respostas**

**Frequência**
→ determina a situação de presença

**Presença validada**
→ permite **Avaliação**

**Participação + regra de elegibilidade**
→ pode permitir **Certificado**