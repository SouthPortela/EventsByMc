# Decisões de Modelagem — Semana 1

## Projeto

**Events by MC — Plataforma de Gestão de Eventos**

## Disciplina

Programação Orientada a Objetos II

## Integrantes

- Brandon Handes
- Brendha Alves
- Gabriel Brito
- Luciano Godoi
- Marcos Portela

---

## 1. Objetivo da modelagem

A primeira versão do modelo de domínio tem como objetivo representar os
principais conceitos da Plataforma de Gestão de Eventos e distribuir suas
responsabilidades entre objetos.

A equipe priorizou um modelo com responsabilidades claras, encapsulamento
e baixo acoplamento, evitando concentrar as regras do sistema em uma única
classe ou em classes de serviço excessivamente grandes.

---

## 2. Principais objetos identificados

Os primeiros objetos identificados no domínio foram:

- Evento
- Atividade
- Participante
- Inscrição
- Local

Esses objetos representam conceitos diretamente relacionados ao problema
apresentado nos requisitos do projeto.

---

## 3. Responsabilidade da classe Evento

A classe `Evento` representa um evento cadastrado na plataforma.

Entre suas responsabilidades está controlar seu próprio estado.

Estados inicialmente considerados:

- RASCUNHO
- PUBLICADO
- ENCERRADO

A alteração do estado ocorre por meio de comportamentos do próprio objeto,
como:

- `publicar()`
- `encerrar()`

A equipe optou por não permitir alteração direta do estado por outras
classes.

Essa decisão busca preservar o encapsulamento e impedir alterações
inconsistentes no estado do evento.

---

## 4. Responsabilidade da classe Atividade

A classe `Atividade` representa uma atividade pertencente ao contexto de
um evento.

Uma atividade possui inicialmente:

- título;
- tipo;
- local.

A equipe optou por manter o tipo da atividade como uma característica do
objeto neste primeiro modelo, evitando criar subclasses exclusivamente para
representar categorias como palestra, oficina, pôster ou apresentação.

Essa decisão poderá ser revisada caso o comportamento de diferentes tipos
de atividade exija tratamento polimórfico no decorrer do projeto.

---

## 5. Responsabilidade da classe Participante

A classe `Participante` representa a pessoa que utiliza a plataforma para
participar dos eventos.

O objeto mantém inicialmente:

- nome;
- e-mail.

As informações básicas são protegidas pelo próprio objeto, evitando a
criação de participantes sem os dados mínimos necessários.

---

## 6. Responsabilidade da classe Inscricao

A classe `Inscricao` representa a relação entre um participante e um evento.

Ela mantém referências para:

- Participante;
- Evento.

A equipe optou por representar a inscrição como um objeto próprio porque
ela poderá receber comportamentos e informações adicionais no decorrer do
projeto, como:

- situação da inscrição;
- data da inscrição;
- cancelamento;
- atividades escolhidas;
- regras relacionadas à participação.

---

## 7. Responsabilidade da classe Local

A classe `Local` representa o espaço onde uma atividade será realizada.

Possui inicialmente:

- nome;
- endereço;
- capacidade.

A capacidade é validada no momento da criação do objeto e não pode ser
alterada diretamente.

Essa decisão protege uma regra básica do domínio e evita estados inválidos.

---

## 8. Encapsulamento

As informações internas dos objetos são mantidas como atributos privados.

As alterações que representam regras de negócio devem ocorrer por meio de
comportamentos definidos pelas próprias classes.

Exemplo:

```java
evento.publicar();