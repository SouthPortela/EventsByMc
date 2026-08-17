# Modelo de Domínio — Events by MC

## 1. Visão geral

O domínio da plataforma Events by MC é organizado a partir do conceito de Evento.

Um Evento possui atividades, participantes, regras de inscrição, programação,
frequência e avaliações.

Os principais objetos do domínio são:

- Evento
- Atividade
- Usuário
- Participante
- Organizador
- Inscrição
- Agenda
- Local
- Trilha
- Pessoa
- Frequência
- Política de Frequência
- Questionário
- Pergunta
- Resposta
- Avaliação
- Relatório

---

## 2. Relacionamentos principais

### Evento

Um Evento:

- possui várias Atividades;
- possui regras de inscrição;
- possui período;
- possui estado;
- pode receber inscrições de Participantes.

### Atividade

Uma Atividade:

- pertence a um Evento;
- possui horário;
- possui Local;
- pode pertencer a uma Trilha;
- pode possuir Pessoas vinculadas;
- possui uma política de frequência;
- pode possuir um Questionário;
- pode possuir controle de vagas.

### Participante

Um Participante:

- possui uma conta de Usuário;
- pode realizar Inscrições;
- pode selecionar Atividades;
- possui uma Agenda;
- pode registrar Frequência;
- pode realizar Avaliações quando elegível.

### Inscrição

Uma Inscrição:

- relaciona um Participante a um Evento;
- possui uma situação;
- pode permitir a seleção de atividades;
- pode ser cancelada conforme as regras do Evento.

### Agenda

Uma Agenda:

- pertence a um Participante;
- contém atividades selecionadas;
- mantém as atividades em ordem cronológica;
- verifica conflitos de horário.

### Frequência

A Frequência:

- relaciona Participante e Atividade;
- possui registros de presença;
- pode utilizar QR Code;
- pode utilizar lançamento manual;
- é calculada conforme a política definida para a atividade.

### Questionário

Um Questionário:

- pertence a uma Atividade;
- possui várias Perguntas;
- recebe Respostas;
- pode possuir diferentes tipos de perguntas.

---

## 3. Modelo conceitual

```text
                         ┌─────────────────┐
                         │     EVENTO      │
                         └────────┬────────┘
                                  │
                           possui várias
                                  │
                                  ▼
                         ┌─────────────────┐
                         │    ATIVIDADE    │
                         └───────┬─────────┘
                                 │
             ┌───────────────────┼────────────────────┐
             │                   │                    │
             ▼                   ▼                    ▼
        ┌─────────┐        ┌─────────────┐      ┌──────────────┐
        │  LOCAL  │        │   TRILHA    │      │    PESSOA    │
        └─────────┘        └─────────────┘      └──────────────┘
                                 
                         ┌─────────────────┐
                         │     EVENTO      │
                         └────────┬────────┘
                                  │
                              recebe
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   INSCRIÇÃO     │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │   PARTICIPANTE  │
                         └────────┬────────┘
                                  │
                       possui uma │
                                  ▼
                         ┌─────────────────┐
                         │     AGENDA      │
                         └────────┬────────┘
                                  │
                           seleciona
                                  │
                                  ▼
                         ┌─────────────────┐
                         │    ATIVIDADE    │
                         └─────────────────┘


        ATIVIDADE
            │
            ├──────────────► POLÍTICA DE FREQUÊNCIA
            │
            ├──────────────► QUESTIONÁRIO
            │                    │
            │                    ▼
            │                PERGUNTA
            │                    │
            │                    ▼
            │                RESPOSTA
            │
            └──────────────► REGISTRO DE FREQUÊNCIA