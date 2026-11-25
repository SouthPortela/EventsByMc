# Agent Directives

## 1. Visão Geral do Projeto
- **Linguagem/Stack:** Java 21 com HttpServer do JDK e JDBC / Vue.js 3 com TypeScript e Bootstrap / PostgreSQL 16 (sem Spring Boot)
- **Objetivo:** Este projeto tem como objetivo criar uma aplicação Web similar ao Sympla porém com controle de cronograma de palestras e frequencia, com foco na funcionalidade de registrar presença via QR Code.

## 2. Comandos Operacionais
Sempre valide suas alterações executando os comandos abaixo:
- **Instalação:** `npm install` (desktop), `npm --prefix frontend install` (web) e `.\mvnw.cmd clean install` (Java; `./mvnw` em Linux/macOS)
- **Executar Testes:** `npm test` (desktop), `npm --prefix frontend run test:run` e `.\mvnw.cmd test`. Testes com banco devem usar instância dedicada.
- **Lint / Formatação:** `npm run lint` (desktop); em `frontend/`, `npm exec -- oxlint .` e `npm exec -- eslint .`. `npm run lint` do frontend usa correção automática. Checkstyle não está configurado no Maven.

## 3. Padrões de Código e Arquitetura
- Todo componente deve usar obrigatoriamente a sintaxe <script setup lang="ts"> (Composition API) e manter a tipagem estrita de variáveis.
- Siga estritamente o padrão de Arquitetura Hexagonal em `src/`.
- A camada de domínio não deve conter nenhuma dependência do Spring Framework ou anotações de persistência (como @Entity ou @Table). Toda a comunicação externa deve ser feita estritamente via Ports e Adapters.
- Nunca escreva logs (via System.out ou bibliotecas de log) que exponham dados sensíveis dos participantes do evento, como CPF, e-mail ou tokens.
- As rotas de geração e validação de QR Code devem exigir validação estrita de tokens JWT.
- Exija parametrização nas consultas de banco de dados e sanitização de inputs no front-end para evitar vulnerabilidades de XSS.
- Nunca adicione bibliotecas externas sem necessidade explícita.
- Mantenha funções pequenas e adicione tratamento adequado de exceções.
- Toda nova funcionalidade deve incluir testes unitários correspondentes em `src/test/java`, `frontend/src/**/*.spec.ts` ou `desktop/tests`, conforme a camada.
- Ao sugerir ou modificar arquivos de infraestrutura, utilize as melhores práticas para Dockerfile e docker-compose.yml, garantindo imagens limpas e prontas para ambiente de produção.

## 4. Regras Críticas e Restrições
- **Não altere** arquivos em `.github/workflows/` ou configurações de infraestrutura sem solicitação explícita.
- Nunca faça hardcode de credenciais, chaves de API ou URLs de banco de dados.
- Mantenha compatibilidade retroativa com as APIs existentes.