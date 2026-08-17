# Mapa de Responsabilidades

O mapa de responsabilidades define quais conceitos do domínio são responsáveis por realizar cada comportamento do sistema.

A distribuição busca evitar classes anêmicas, concentrando as regras nos objetos que possuem as informações necessárias para executá-las.

---

# 1. Evento

## Responsabilidade

Representar e controlar o ciclo de vida de um evento.

## Comportamentos

- Criar evento;
- Alterar informações do evento;
- Alterar estado do evento;
- Publicar evento;
- Encerrar evento;
- Controlar suas atividades;
- Definir regras gerais de inscrição;
- Verificar condições relacionadas ao período do evento.

## Não deve ser responsabilidade

- Gerenciar diretamente a interface;
- Acessar diretamente o banco de dados;
- Gerar respostas HTTP.

---

# 2. Atividade

## Responsabilidade

Representar uma atividade pertencente a um evento e proteger suas regras específicas.

## Comportamentos

- Definir horário;
- Definir local;
- Definir capacidade;
- Vincular pessoas;
- Definir política de frequência;
- Definir questionário;
- Verificar disponibilidade de vagas;
- Participar da validação de conflitos.

---

# 3. Usuário

## Responsabilidade

Representar uma conta de acesso à plataforma.

## Comportamentos

- Alterar dados básicos;
- Alterar credenciais conforme regras;
- Identificar seu perfil;
- Manter estado da conta.

---

# 4. Participante

## Responsabilidade

Representar o comportamento de uma pessoa que participa de eventos.

## Comportamentos

- Realizar inscrição;
- Cancelar inscrição quando permitido;
- Selecionar atividades;
- Remover atividades da agenda;
- Consultar agenda;
- Solicitar registro de presença;
- Responder avaliações quando elegível.

---

# 5. Organizador

## Responsabilidade

Representar o usuário responsável pela operação do evento.

## Comportamentos

- Criar eventos;
- Configurar atividades;
- Configurar inscrições;
- Configurar frequência;
- Configurar avaliações;
- Registrar frequência manualmente;
- Corrigir frequência;
- Consultar relatórios.

---

# 6. Inscrição

## Responsabilidade

Representar o vínculo entre participante e evento.

## Comportamentos

- Controlar situação da inscrição;
- Confirmar inscrição;
- Cancelar inscrição;
- Verificar se a inscrição está ativa;
- Participar da validação das regras de acesso às atividades.

---

# 7. Agenda

## Responsabilidade

Controlar as atividades escolhidas pelo participante.

## Comportamentos

- Adicionar atividade;
- Remover atividade;
- Verificar conflito de horário;
- Organizar atividades cronologicamente;
- Consultar atividades selecionadas.

---

# 8. Política de Inscrição

## Responsabilidade

Definir como as inscrições serão tratadas.

## Possíveis comportamentos

- Verificar necessidade de inscrição no evento;
- Verificar necessidade de escolha de atividades;
- Verificar limite de vagas;
- Verificar prazo de cancelamento.

A política deverá ser substituível para permitir diferentes comportamentos sem espalhar condicionais pelo sistema.

---

# 9. Política de Frequência

## Responsabilidade

Definir como a presença de uma atividade será registrada e calculada.

## Possíveis estratégias

- Check-in único;
- Entrada e saída;
- Validação manual;
- Percentual de permanência.

A escolha da política deverá ser realizada pela atividade.

---

# 10. Registro de Frequência

## Responsabilidade

Representar uma marcação individual de presença.

## Comportamentos

- Registrar data e hora;
- Identificar participante;
- Identificar atividade;
- Identificar origem;
- Identificar responsável quando necessário.

---

# 11. Questionário

## Responsabilidade

Gerenciar uma avaliação configurada para uma atividade.

## Comportamentos

- Adicionar perguntas;
- Remover perguntas;
- Validar respostas;
- Controlar respostas do participante;
- Impedir respostas duplicadas conforme regra definida.

---

# 12. Pergunta

## Responsabilidade

Representar uma pergunta e validar respostas de acordo com seu tipo.

## Possíveis tipos

- Texto;
- Escolha única;
- Escala numérica.

O comportamento de validação deverá ser polimórfico.

---

# 13. Avaliação

## Responsabilidade

Controlar a avaliação realizada por um participante.

## Comportamentos

- Verificar elegibilidade;
- Associar participante ao questionário;
- Registrar respostas;
- Impedir duplicidade conforme política;
- Permitir consulta dos resultados.

---

# 14. Relatório

## Responsabilidade

Representar informações consolidadas para consulta ou exportação.

## Exemplos

- Relatório de inscrições;
- Relatório de frequência;
- Relatório de participação.

---

# 15. Repositórios

Os repositórios serão responsáveis pela persistência dos objetos.

Eles não deverão conter regras centrais do domínio.

Exemplo conceitual:

```text
EventoRepository
AtividadeRepository
UsuarioRepository
InscricaoRepository
FrequenciaRepository
QuestionarioRepository