# Validação da limpeza — 25/11/2026

Registro desta revisão; não substitui nova execução após alterações posteriores.
Não foram aplicadas migrações nem cargas no banco da aplicação.

| Verificação | Resultado |
|---|---|
| Instalação npm da raiz, sem scripts | Concluída; lockfile atualizado após retirar Bootstrap e concurrently da raiz |
| Testes do desktop (node:test) | 7 aprovados |
| Verificação de sintaxe desktop | Aprovada |
| Vitest frontend | 51 testes aprovados em 13 arquivos |
| Oxlint e ESLint sem correções automáticas | Aprovados |
| Build web com verificação de tipos | Aprovado |
| Build desktop com verificação de tipos | Aprovado |
| Compilação Java 21 de produção e testes com javac | Aprovada |
| JUnit Platform Launcher com dependências já disponíveis | 50 aprovados, 7 ignorados, nenhuma falha |
| Links locais, documentos vazios e fechamento de blocos Markdown | Verificados |
| git diff --check | Sem erros de whitespace |

## Limitações deste ambiente

O Maven Wrapper não conseguiu escrever no cache local. A tentativa pelo Maven já
instalado, em modo offline, também não completou o ciclo: faltam dependências dos
plugins clean/resources. A compilação e os testes Java foram executados diretamente
com JDK 21 e JUnit Platform Launcher usando os JARs existentes. Isso não valida o
empacotamento Maven. Os 7 testes condicionais de banco permaneceram ignorados.

A preparação do PGlite falhou com ENOTFOUND ao resolver registry.npmjs.org. A suíte
SQL não foi executada nesta revisão. Nenhum script SQL funcional foi alterado.

O smoke da janela Electron encerrou com código 2147483651, sem diagnóstico em stderr.
A conferência com Electron real continua pendente; builds e testes de protocolo
passaram. Reexecute npm run desktop:smoke em um ambiente desktop funcional.

## Concluir a limpeza do índice Git

Os arquivos anteriormente versionados de node_modules foram removidos da árvore de
trabalho. A pasta já é ignorada, mas a retirada explícita do índice foi impedida
por falta de permissão para criar .git/index.lock. Na raiz, com Git gravável, execute:

    git rm -r --cached --ignore-unmatch node_modules

O comando altera o índice e preserva dependências que estiverem instaladas. Preserve
os lockfiles da raiz e do frontend; depois revise e versione as mudanças da limpeza.

## Reproduzir as verificações

Use os comandos do [README](../../README.md),
[frontend](../../frontend/README.md), [desktop](../../desktop/README.md) e
[banco](../../db/README.md). Use PostgreSQL dedicado para os testes condicionais.
