# Mapa de responsabilidades

Mapa da implementação atual, complementando o modelo conceitual.

| Camada/componente | Responsabilidade |
|---|---|
| Evento | Período, categoria, estado, publicação, encerramento e bloqueio |
| Atividade e Programacao | Dados da atividade, período e composição da programação |
| Usuario, Pessoa e Senha | Identidade, dados pessoais e invariantes de credencial |
| PoliticaPerfis | Validade do conjunto de perfis autenticados |
| RegrasChamada | Validade e regras da chamada temporária |
| AutenticarUsuarioUseCase / RegistrarUsuarioUseCase | Autenticação e cadastro pelas portas de usuário, hash e token |
| EventosUseCase | Catálogo e gestão autorizada de eventos |
| PresencaUseCase | Inscrição, cancelamento, chamada e confirmação de presença |
| ProgramacaoUseCase | Política de inscrição, metadados, atividades e seleção de agenda |
| FrequenciaUseCase | Marcações manuais autorizadas e auditáveis |
| AvaliacoesUseCase | Questionários, publicação, respostas e resultados |
| RelatoriosUseCase | Consulta autorizada de inscrições e frequência |
| CertificadosUseCase | Elegibilidade, emissão, download e envio individual |
| InteracaoUseCase | Leitura e publicação autorizadas de mensagens |
| AdministracaoUseCase | Contas/perfis, visão global, bloqueios e auditoria |
| Portas de saída | Contratos de persistência, segurança e envio de e-mail |
| Adaptadores JDBC | Consultas parametrizadas, transações, locks e mapeamento |
| Handlers HTTP / Router / filtros | Transporte, identidade autenticada, conversão e respostas |
| CompositionRoot | Escolha e ligação das implementações |
| Frontend Vue | Interação, validação de formulário e renderização segura |
| Electron | Janela isolada, arquivos empacotados e encaminhamento à API configurada |

Domínio e aplicação não acessam diretamente componentes Vue, Electron ou tabelas.
O frontend não determina permissões efetivas. Organizador e participante são perfis,
e os casos de uso consultam a identidade e os acessos conforme seus contratos.

O banco protege referências e unicidade; regras transacionais de vagas e agenda
ficam no adaptador JDBC. Os relatórios calculam a conclusão a partir dos registros.
A mesma API atende web e desktop, preservando os contratos existentes.

Veja [modelo](modelo-dominio.md), [API](../api/requisitos-funcionais-webapp.md)
e [administração](../api/administracao.md).
