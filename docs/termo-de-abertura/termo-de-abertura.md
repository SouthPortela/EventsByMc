# TERMO DE ABERTURA

# Events by MC

**Projeto:** Plataforma de Gestão de Eventos  
**Disciplina:** Programação Orientada a Objetos II  
**Versão do Documento:** 1.0  
**Data:** 13/08/2026

---

# Histórico de Revisão

| Data | Versão do Documento | Descrição | Autor |
|---|---|---|---|
| 13/08/2026 | 1.0 | Criação do Termo de Abertura e definição inicial do projeto. | Equipe Events by MC |

---

# Sumário

1. Apresentação do Projeto  
2. Um resumo das condições que definem o projeto  
3. Matriz de Responsabilidades  
4. Necessidades básicas do trabalho a ser realizado  
5. Descrição do Projeto  
   5.1 Escopo do Projeto  
   5.2 Não-Escopo do Projeto  
   5.3 Premissas do Projeto  
   5.4 Produto do Projeto  
   5.5 Requisitos do Usuário  
   5.6 Diagrama Geral de Caso de Uso  
   5.7 Restrições do Produto  
   5.8 Critério de Qualidade do Produto  
   5.9 Lista de Riscos  
   5.10 Prioridades  
   5.11 Referências  
   5.12 Cronograma Básico do Projeto  
   5.13 Estimativas Iniciais de Custo  
6. Administração  
   6.1 Necessidade Inicial do Projeto  
   6.1.1 Necessidade de Suporte pela Organização  
   6.1.2 Controle e Gerenciamento das Informações do Projeto  
7. Assinaturas  

---

# 1. Apresentação do Projeto

O **Events by MC** é uma plataforma de gestão de eventos desenvolvida no contexto da disciplina de Programação Orientada a Objetos II.

A proposta é desenvolver uma solução capaz de centralizar as principais atividades relacionadas à organização e participação em eventos, permitindo o gerenciamento de eventos, atividades, programação, inscrições, agenda dos participantes, controle de frequência, avaliações e relatórios.

A plataforma será inicialmente utilizada como referência para um simpósio acadêmico, porém sua estrutura será projetada de maneira configurável, permitindo sua utilização em diferentes tipos de eventos acadêmicos e profissionais.

Além de atender às necessidades funcionais do sistema, o projeto tem como objetivo demonstrar a aplicação prática dos conceitos de Programação Orientada a Objetos, incluindo encapsulamento, composição, polimorfismo, interfaces, princípios SOLID, padrões de projeto, testes automatizados e separação de responsabilidades.

O produto será desenvolvido de forma incremental, permitindo que a equipe apresente funcionalidades integradas ao longo das etapas de desenvolvimento.

---

# 2. Um resumo das condições que definem o projeto

O projeto será utilizado por diferentes perfis de usuários envolvidos na realização e participação de eventos.

Os principais usuários do sistema são:

- **Administrador:** responsável pelo gerenciamento geral da plataforma e dos acessos administrativos.
- **Organizador:** responsável pela configuração e gerenciamento dos eventos, atividades, inscrições, frequência, avaliações e relatórios.
- **Participante:** usuário que realiza inscrição, consulta a programação, monta sua agenda, registra presença e realiza avaliações quando estiver elegível.
- **Visitante:** usuário que acessa o site público para consultar eventos e iniciar o processo de cadastro e inscrição.

O sistema será utilizado em ambiente acadêmico e poderá ser acessado por meio de uma aplicação desktop destinada às operações administrativas e por meio de um site público destinado aos visitantes e participantes.

A aplicação desktop e o site deverão utilizar a mesma API e o mesmo banco de dados, garantindo que as informações utilizadas pelos diferentes canais sejam compartilhadas.

O núcleo da aplicação será desenvolvido em Java, conforme exigência da disciplina.

---

# 3. Matriz de Responsabilidades

A equipe do projeto será composta pelos seguintes integrantes:

| Integrante | Papel | Responsabilidades |
|---|---|---|
| Brandon Handes | Desenvolvimento | Participação na análise, modelagem, implementação, testes e documentação do sistema. |
| Brendha Alves | Desenvolvimento | Participação na análise, modelagem, implementação, testes e documentação do sistema. |
| Gabriel Brito | Desenvolvimento | Participação na análise, modelagem, implementação, testes e documentação do sistema. |
| Luciano Godoi | Desenvolvimento | Participação na análise, modelagem, implementação, testes e documentação do sistema. |
| Marcos Portela | Desenvolvimento | Participação na análise, modelagem, implementação, testes e documentação do sistema. |

A equipe trabalhará de forma colaborativa, distribuindo as atividades conforme o backlog e as necessidades de cada incremento.

As decisões importantes de arquitetura, modelagem e escopo deverão ser discutidas e registradas pela equipe.

---

# 4. Necessidades básicas do trabalho a ser realizado

Para a realização do projeto serão necessárias condições que permitam o desenvolvimento, documentação, testes e demonstração da plataforma.

As principais necessidades são:

- Computadores com acesso à internet;
- Ambiente de desenvolvimento compatível com Java;
- Ferramentas para desenvolvimento e documentação;
- Banco de dados relacional;
- Ambiente para execução da API;
- Ambiente para execução da aplicação desktop;
- Ambiente para execução do site público;
- Ferramentas para modelagem UML;
- Ferramentas para testes da API;
- Sistema de controle de versão;
- Conhecimentos de Programação Orientada a Objetos;
- Conhecimentos básicos de banco de dados;
- Conhecimentos de desenvolvimento de APIs;
- Conhecimentos de desenvolvimento web;
- Disponibilidade dos integrantes para as atividades semanais;
- Acompanhamento e orientação do professor.

O projeto será desenvolvido de forma incremental, com entregas e demonstrações semanais.

---

# 5. Descrição do Projeto

## 5.1 Escopo do Projeto

Fazem parte do escopo inicial do Events by MC:

- Cadastro de usuários;
- Autenticação por e-mail e senha;
- Controle de permissões;
- Gerenciamento de eventos;
- Gerenciamento de atividades;
- Cadastro de trilhas e categorias;
- Cadastro de locais;
- Cadastro e vinculação de pessoas às atividades;
- Consulta da programação;
- Publicação de eventos no site;
- Cadastro e inscrição de participantes;
- Controle de vagas;
- Cancelamento de inscrições;
- Seleção de atividades;
- Agenda personalizada do participante;
- Tratamento de conflitos de horários;
- Configuração de regras de frequência;
- Controle de presença por QR Code;
- Lançamento manual de frequência;
- Avaliação de atividades;
- Questionários configuráveis;
- Relatórios de inscrições;
- Relatórios de frequência;
- Exportação de pelo menos um relatório em formato portátil;
- Testes automatizados das principais regras de negócio;
- Aplicação de conceitos de Programação Orientada a Objetos.

---

## 5.2 Não-Escopo do Projeto

Não fazem parte do escopo obrigatório da primeira versão:

- Aplicativo móvel nativo;
- Recuperação de senha;
- Login por redes sociais;
- Autenticação multifator;
- Processamento de pagamentos;
- Emissão de notas fiscais;
- Infraestrutura de alta disponibilidade;
- Integração obrigatória com Google Drive;
- Integração obrigatória com equipamentos físicos específicos;
- Integrações institucionais que não sejam necessárias ao funcionamento do núcleo do sistema.

Funcionalidades adicionais poderão ser desenvolvidas posteriormente caso os requisitos obrigatórios estejam estabilizados.

---

## 5.3 Premissas do Projeto

São consideradas premissas para a realização do projeto:

1. O núcleo do sistema será desenvolvido utilizando Java.
2. O sistema utilizará um banco de dados relacional.
3. A aplicação desktop e o site público utilizarão a mesma API.
4. A API e as aplicações utilizarão o mesmo banco de dados.
5. O site público não terá acesso direto ao banco de dados.
6. As regras centrais do negócio deverão estar independentes da interface.
7. As senhas serão armazenadas utilizando mecanismo adequado de hash.
8. Os integrantes terão acesso às ferramentas necessárias ao desenvolvimento.
9. O professor realizará o acompanhamento e a orientação do projeto durante as etapas previstas.
10. Os requisitos definidos no documento-base serão utilizados como referência para o desenvolvimento.
11. As decisões importantes de arquitetura e modelagem serão documentadas pela equipe.
12. O sistema deverá considerar o fuso horário definido para o evento.

---

## 5.4 Produto do Projeto

O produto final será uma plataforma de gestão de eventos composta por:

- Núcleo de domínio desenvolvido em Java;
- API responsável pela comunicação entre as aplicações;
- Aplicação desktop para operações administrativas;
- Site público para consulta e inscrição;
- Banco de dados relacional;
- Sistema de autenticação e autorização;
- Controle de inscrições;
- Agenda personalizada;
- Controle de frequência;
- Sistema de avaliações;
- Relatórios;
- Testes automatizados;
- Documentação técnica;
- Documentação de execução e configuração;
- Dados preparados para demonstração.

---

# 5.5 Requisitos do Usuário

## RU-01 — Cadastro de usuário

O sistema deverá permitir que uma pessoa realize seu cadastro utilizando nome, e-mail e senha.

## RU-02 — Autenticação

O sistema deverá permitir que usuários cadastrados realizem autenticação e tenham acesso às funcionalidades compatíveis com suas permissões.

## RU-03 — Gerenciamento de eventos

O organizador deverá conseguir criar, editar, consultar e encerrar eventos.

## RU-04 — Gerenciamento de atividades

O organizador deverá conseguir cadastrar diferentes tipos de atividades dentro de um evento.

## RU-05 — Programação

O sistema deverá permitir a organização e consulta da programação por diferentes critérios, como data, horário, local, trilha ou tipo de atividade.

## RU-06 — Inscrição

O visitante deverá conseguir realizar cadastro e inscrição em eventos disponibilizados pela plataforma.

## RU-07 — Controle de vagas

O sistema deverá controlar vagas quando essa regra estiver configurada para o evento ou atividade.

## RU-08 — Agenda pessoal

O participante deverá conseguir selecionar atividades e consultar sua agenda personalizada em ordem cronológica.

## RU-09 — Controle de frequência

O sistema deverá registrar a presença dos participantes utilizando QR Code e também permitir lançamento manual autorizado.

## RU-10 — Avaliação

O participante inscrito e com presença validada deverá conseguir responder avaliações configuradas para a atividade.

## RU-11 — Relatórios

O organizador deverá conseguir consultar informações consolidadas sobre inscrições, frequência e participação.

## RU-12 — Site público

O sistema deverá disponibilizar uma página pública contendo informações dos eventos e suas programações.

---

# 5.6 Diagrama Geral de Caso de Uso

O diagrama geral de casos de uso será elaborado durante a etapa de modelagem do projeto.

Os principais atores identificados são:

- Administrador;
- Organizador;
- Participante;
- Visitante.

## CU01 — Gerenciar usuários

Permite ao administrador gerenciar usuários e suas permissões.

## CU02 — Gerenciar evento

Permite ao organizador criar, editar, consultar e encerrar eventos.

## CU03 — Gerenciar atividades

Permite ao organizador cadastrar e configurar atividades relacionadas a um evento.

## CU04 — Consultar programação

Permite ao visitante e participante consultar a programação dos eventos.

## CU05 — Realizar inscrição

Permite ao visitante realizar cadastro e inscrição em um evento.

## CU06 — Gerenciar agenda

Permite ao participante selecionar atividades e consultar sua agenda.

## CU07 — Registrar frequência

Permite registrar a presença do participante por QR Code ou lançamento manual autorizado.

## CU08 — Avaliar atividade

Permite que participantes elegíveis respondam avaliações.

## CU09 — Consultar relatórios

Permite ao organizador consultar informações de inscrições, frequência e participação.

---

# 5.7 Restrições do Produto

O produto deverá atender às seguintes restrições:

- O núcleo avaliado deverá ser desenvolvido em Java;
- O sistema deverá utilizar banco de dados relacional;
- O site e a aplicação desktop deverão consumir a mesma API;
- O site não deverá acessar diretamente o banco de dados;
- Senhas não poderão ser armazenadas em texto puro;
- Operações protegidas deverão ser autorizadas no servidor;
- O sistema deverá preservar a integridade dos dados;
- As regras de negócio deverão ser independentes da interface;
- O sistema deverá possuir testes automatizados para regras centrais;
- O sistema deverá ser executável em outro computador mediante instruções documentadas.

---

# 5.8 Critério de Qualidade do Produto

## Robustez

O sistema deverá manter a integridade das informações mesmo diante de entradas inválidas ou operações repetidas.

## Segurança

As senhas deverão ser armazenadas utilizando hash apropriado.

Operações administrativas deverão possuir controle de autorização.

## Usabilidade

Os fluxos principais deverão apresentar mensagens compreensíveis e permitir que usuários concluam as operações sem conhecimento técnico.

## Manutenibilidade

O código deverá apresentar responsabilidades bem definidas, nomes expressivos e baixo acoplamento.

## Testabilidade

As principais regras de negócio deverão possuir testes automatizados independentes da interface.

## Desempenho

O sistema deverá apresentar desempenho adequado para uma base de demonstração contendo pelo menos 500 participantes e 100 atividades.

## Confiabilidade

Operações críticas não deverão produzir registros duplicados em decorrência de repetições acidentais.

## Portabilidade

O projeto deverá possuir instruções suficientes para ser executado em outro computador.

---

# 5.9 Lista de Riscos

| Risco | Impacto | Probabilidade | Mitigação |
|---|---|---|---|
| Atraso no desenvolvimento | Alto | Média | Dividir o projeto em incrementos semanais e priorizar requisitos obrigatórios. |
| Falta de integração entre os módulos | Alto | Média | Definir arquitetura e contratos da API antecipadamente. |
| Dificuldade na modelagem orientada a objetos | Alto | Média | Realizar revisões frequentes do modelo e buscar orientação durante as aulas. |
| Conflitos entre integrantes | Médio | Baixa | Dividir responsabilidades e registrar decisões da equipe. |
| Perda de código ou documentação | Alto | Baixa | Utilizar controle de versão e manter cópias dos documentos. |
| Falhas de integração com banco de dados | Médio | Média | Criar scripts reproduzíveis de banco e realizar testes frequentes. |
| Escopo excessivo | Alto | Alta | Priorizar requisitos obrigatórios antes das extensões. |
| Falta de tempo para testes | Alto | Média | Criar testes durante o desenvolvimento e não somente na etapa final. |
| Dificuldades técnicas com ferramentas | Médio | Média | Utilizar ferramentas conhecidas e buscar alternativas quando necessário. |

---

# 5.10 Prioridades

As funcionalidades serão priorizadas de acordo com sua importância para o núcleo do projeto.

## Versão 1 — Núcleo

- Estrutura inicial do domínio;
- Usuários;
- Autenticação;
- Eventos;
- Atividades;
- Programação;
- Inscrições;
- Agenda;
- Frequência;
- Avaliações;
- Relatórios.

## Versão 2 — Extensões desejáveis

- Certificados;
- Geração de PDF;
- Envio de certificados por e-mail;
- Certificados para palestrantes e apresentadores.

## Versão 3 — Extensões opcionais

- Lista de espera;
- Interação entre participantes;
- Recursos adicionais de análise;
- Notificações;
- Personalização.

A equipe deverá priorizar a conclusão e integração dos requisitos obrigatórios antes da implementação de extensões.

---

# 5.11 Referências

1. Documento-base do Projeto Integrador — Programação Orientada a Objetos II, versão 1.1, agosto de 2026.
2. Requisitos funcionais e regras de negócio fornecidos pela disciplina.
3. Conteúdos apresentados nas aulas de Programação Orientada a Objetos II.
4. Materiais de estudo relacionados a Programação Orientada a Objetos, SOLID, padrões de projeto e arquitetura de software.

---

# 5.12 Cronograma Básico do Projeto

O desenvolvimento será realizado de forma incremental, seguindo o cronograma definido para a disciplina.

| Semana | Período | Foco | Incremento |
|---|---|---|---|
| 1 | 11/08 a 13/08 | Domínio, responsabilidades e backlog | Repositório, glossário, responsabilidades e modelo inicial |
| 2 | 18/08 a 20/08 | Objetos, invariantes e persistência | Evento e atividade persistidos |
| 3 | 25/08 a 27/08 | Casos de uso, portas e autenticação | Cadastro, login e autorização |
| 4 | 01/09 a 03/09 | Adaptadores, site e inscrição | Site consumindo API e inscrição |
| 5 | 08/09 a 10/09 | Inscrição e agenda | Agenda e tratamento de conflitos |
| 6 | 15/09 a 17/09 | Frequência | QR Code e frequência manual |
| 7 | 22/09 a 24/09 | Avaliações e relatórios | Questionários, avaliações e relatórios |
| 8 | 29/09 a 01/10 | Integração e entrega | Cenários completos, testes e documentação |

---

# 5.13 Estimativas Iniciais de Custo

Por se tratar de um projeto acadêmico desenvolvido pelos integrantes da equipe, não está previsto custo financeiro direto com mão de obra.

Os principais recursos utilizados serão ferramentas de desenvolvimento, documentação, banco de dados e hospedagem que possuam versões gratuitas ou disponíveis para uso acadêmico.

Caso sejam identificados custos durante o desenvolvimento, estes deverão ser avaliados previamente pela equipe.

---

# 6. Administração

## 6.1 Necessidade Inicial do Projeto

O projeto necessita inicialmente dos seguintes recursos:

### Recursos humanos

- 5 integrantes da equipe;
- Orientação do professor;
- Participação dos integrantes nas atividades de análise, desenvolvimento, testes e documentação.

### Recursos tecnológicos

- Computadores;
- Acesso à internet;
- Ambiente de desenvolvimento Java;
- Banco de dados relacional;
- Ferramentas de modelagem;
- Ferramentas de testes;
- Sistema de controle de versão.

### Recursos de conhecimento

- Programação Orientada a Objetos;
- Java;
- Banco de dados;
- APIs REST;
- Desenvolvimento web;
- Testes automatizados;
- SOLID;
- Padrões de projeto;
- Arquitetura de software.

---

## 6.1.1 Necessidade de Suporte pela Organização

O projeto será desenvolvido como atividade acadêmica e contará com a orientação do professor da disciplina.

A equipe poderá solicitar orientação para esclarecimento de requisitos, decisões de arquitetura, modelagem orientada a objetos e validação dos incrementos.

---

## 6.1.2 Controle e Gerenciamento das Informações do Projeto

As informações do projeto serão organizadas digitalmente.

A documentação será mantida em arquivos estruturados por categorias, incluindo:

- Termo de Abertura;
- Documentação do domínio;
- Backlog;
- Decisões técnicas;
- Modelos e diagramas;
- Requisitos;
- Testes;
- Documentação técnica.

O código-fonte será mantido em ambiente de controle de versão quando definido pela equipe.

As decisões relevantes do projeto deverão ser registradas para permitir o acompanhamento da evolução da solução.

---

