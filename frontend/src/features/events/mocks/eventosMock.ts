import type { EventoDetalhe } from '../types/evento'

export const eventosMock: EventoDetalhe[] = [
  {
    id: 1,
    titulo: 'Simpósio de Cibersegurança e Defesa',
    local: 'Auditório Principal',
    endereco: 'Campus Central — Bloco A',
    vagas: 50,
    dataInicio: '2026-09-10T19:00:00-03:00',
    categoria: 'Cibersegurança',
    preco: 'A partir de R$ 45,00',
    destaque: true,
    banner: {
      id: 'banner-evento-1',
      url: 'https://images.unsplash.com/photo-1540575467063-178a50c2df87?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Pessoas assistindo a uma conferência em um auditório',
    },
    descricao:
      'Um encontro sobre segurança de aplicações, proteção de dados e desafios atuais de Blue Team e SOC.',
    pessoas: [
      { id: 1, nome: 'Ana Martins', papel: 'Palestrante' },
      { id: 2, nome: 'Carlos Souza', papel: 'Responsável pela atividade' },
    ],
    atividades: [
      {
        id: 1,
        titulo: 'Abertura e Tendências de Ameaças',
        horario: '19:00',
        local: 'Auditório Principal',
      },
      {
        id: 2,
        titulo: 'Segurança em APIs REST e OAuth2',
        horario: '19:45',
        local: 'Auditório Principal',
      },
      {
        id: 3,
        titulo: 'Mesa-redonda: Resposta a Incidentes',
        horario: '21:00',
        local: 'Auditório Principal',
      },
    ],
  },
  {
    id: 2,
    titulo: 'Oficina de Java e Spring Boot',
    local: 'Laboratório 3',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 0,
    dataInicio: '2026-09-12T14:00:00-03:00',
    categoria: 'Desenvolvimento',
    gratuito: true,
    destaque: true,
    banner: {
      id: 'banner-evento-2',
      url: 'https://images.unsplash.com/photo-1459749411175-04bf5292ceea?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Público em um evento com iluminação azul',
    },
    descricao:
      'Uma oficina prática para conhecer os fundamentos de arquitetura e aplicações web com Java e Spring Boot.',
    atividades: [
      { id: 4, titulo: 'Introdução ao Spring Data JPA', horario: '14:00', local: 'Laboratório 3' },
      {
        id: 5,
        titulo: 'Construindo e Testando uma API REST',
        horario: '15:45',
        local: 'Laboratório 3',
      },
    ],
  },
  {
    id: 3,
    titulo: 'Encontro de Design de Interfaces & UX',
    local: 'Sala Multiuso',
    endereco: 'Campus Central — Biblioteca',
    vagas: 24,
    dataInicio: '2026-09-18T18:30:00-03:00',
    categoria: 'Design',
    preco: 'A partir de R$ 30,00',
    destaque: true,
    banner: {
      id: 'banner-evento-3',
      url: 'https://images.unsplash.com/photo-1531403009284-440f080d1e12?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Equipe colaborando em uma oficina de design',
    },
    descricao:
      'Discussões e atividades sobre acessibilidade, experiência do usuário e construção de interfaces funcionais.',
    atividades: [
      {
        id: 6,
        titulo: 'Princípios de Interfaces Acessíveis',
        horario: '18:30',
        local: 'Sala Multiuso',
      },
      {
        id: 7,
        titulo: 'Laboratório de Prototipação Rápida',
        horario: '20:00',
        local: 'Sala Multiuso',
      },
    ],
  },
  {
    id: 4,
    titulo: 'Bootcamp de Linux: Shell & Produtividade',
    local: 'Laboratório 1',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 15,
    dataInicio: '2026-09-22T19:00:00-03:00',
    categoria: 'Infraestrutura',
    gratuito: true,
    banner: {
      id: 'banner-evento-4',
      url: 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Pessoas reunidas em um evento gastronômico',
    },
    descricao:
      'Domine a linha de comando, automações com Bash script e personalização eficiente de ambientes de desenvolvimento.',
    atividades: [
      {
        id: 8,
        titulo: 'Manipulação de Arquivos e Pipes',
        horario: '19:00',
        local: 'Laboratório 1',
      },
      { id: 9, titulo: 'Criando Scripts de Automação', horario: '20:30', local: 'Laboratório 1' },
    ],
  },
  {
    id: 5,
    titulo: 'Workshop de Redes Corporativas e Roteamento',
    local: 'Laboratório de Redes',
    endereco: 'Campus Tecnológico — Bloco C',
    vagas: 20,
    dataInicio: '2026-09-25T14:00:00-03:00',
    categoria: 'Redes',
    preco: 'A partir de R$ 25,00',
    banner: {
      id: 'banner-evento-5',
      url: 'https://images.unsplash.com/photo-1576091160550-2173dba999ef?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Profissional apresentando um tema de saúde e tecnologia',
    },
    descricao:
      'Configuração prática de VLANs, firewalls, sub-redes e políticas de controle de tráfego para ambientes empresariais.',
    atividades: [
      {
        id: 10,
        titulo: 'Fundamentos de Sub-redes e VLANs',
        horario: '14:00',
        local: 'Laboratório de Redes',
      },
      {
        id: 11,
        titulo: 'Prática de Roteamento e Firewall',
        horario: '16:00',
        local: 'Laboratório de Redes',
      },
    ],
  },
  {
    id: 6,
    titulo: 'Containers com Docker na Prática',
    local: 'Laboratório 4',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 18,
    dataInicio: '2026-09-29T19:00:00-03:00',
    categoria: 'DevOps',
    preco: 'A partir de R$ 35,00',
    banner: {
      id: 'banner-evento-6',
      url: 'https://images.unsplash.com/photo-1585699324551-f6c309eedeca?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Palco iluminado preparado para apresentação',
    },
    descricao:
      'Aprenda a empacotar, versionar e orquestrar aplicações modernas utilizando Docker e Docker Compose.',
    atividades: [
      {
        id: 12,
        titulo: 'Criando Dockerfiles Eficientes',
        horario: '19:00',
        local: 'Laboratório 4',
      },
      {
        id: 13,
        titulo: 'Orquestração com Docker Compose',
        horario: '20:30',
        local: 'Laboratório 4',
      },
    ],
  },
  {
    id: 7,
    titulo: 'Estruturas de Dados e Algoritmos com C',
    local: 'Sala 102',
    endereco: 'Campus Central — Bloco B',
    vagas: 30,
    dataInicio: '2026-10-02T16:00:00-03:00',
    categoria: 'Desenvolvimento',
    gratuito: true,
    banner: {
      id: 'banner-evento-7',
      url: 'https://images.unsplash.com/photo-1505373877841-8d25f7d46678?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Auditório com palco preparado para evento',
    },
    descricao:
      'Estudo detalhado sobre gerenciamento de memória, ponteiros, listas encadeadas, árvores binárias e análise de complexidade.',
    atividades: [
      { id: 14, titulo: 'Ponteiros e Alocação Dinâmica', horario: '16:00', local: 'Sala 102' },
      { id: 15, titulo: 'Árvores Binárias e Grafos', horario: '17:30', local: 'Sala 102' },
    ],
  },
  {
    id: 8,
    titulo: 'Painel: Cloud AWS e Arquitetura Serverless',
    local: 'Auditório Principal',
    endereco: 'Campus Central — Bloco A',
    vagas: 80,
    dataInicio: '2026-10-05T19:30:00-03:00',
    categoria: 'Cloud',
    gratuito: true,
    banner: {
      id: 'banner-evento-8',
      url: 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=1200&q=80',
      textoAlternativo: 'Participantes conversando durante um encontro profissional',
    },
    descricao:
      'Visão geral sobre arquiteturas escaláveis na nuvem, serviços de armazenamento de objetos e computação orientada a eventos.',
    atividades: [
      {
        id: 16,
        titulo: 'Armazenamento com S3 e CloudFront',
        horario: '19:30',
        local: 'Auditório Principal',
      },
      {
        id: 17,
        titulo: 'Microsserviços e Funções Serverless',
        horario: '20:45',
        local: 'Auditório Principal',
      },
    ],
  },
  {
    id: 9,
    titulo: 'Hackathon Universitário de Inovação',
    local: 'Ginásio Poliesportivo',
    endereco: 'Campus Central — Complexo Esportivo',
    vagas: 120,
    dataInicio: '2026-10-10T08:00:00-03:00',
    categoria: 'Inovação',
    descricao:
      'Uma maratona de 24 horas de desenvolvimento focada em solucionar problemas de logística, saúde e educação regional.',
    atividades: [
      { id: 18, titulo: 'Apresentação dos Desafios', horario: '08:00', local: 'Ginásio' },
      { id: 19, titulo: 'Mentoria Técnica de Projetos', horario: '14:00', local: 'Ginásio' },
      { id: 20, titulo: 'Pitch Final e Premiação', horario: '20:00', local: 'Auditório Principal' },
    ],
  },
  {
    id: 10,
    titulo: 'Oficina de Modelagem de Banco de Dados SQL',
    local: 'Laboratório 2',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 0,
    dataInicio: '2026-10-14T19:00:00-03:00',
    categoria: 'Banco de Dados',
    descricao:
      'Boas práticas de normalização, criação de relacionamentos, índices de performance e consultas analíticas avançadas.',
    atividades: [
      {
        id: 21,
        titulo: 'Normalização até 3FN na Prática',
        horario: '19:00',
        local: 'Laboratório 2',
      },
      {
        id: 22,
        titulo: 'Otimização de Queries com Índices',
        horario: '20:30',
        local: 'Laboratório 2',
      },
    ],
  },
  {
    id: 11,
    titulo: 'Seminário de Inteligência Artificial e LLMs',
    local: 'Auditório de Ciências',
    endereco: 'Campus Tecnológico — Bloco D',
    vagas: 60,
    dataInicio: '2026-10-17T15:00:00-03:00',
    categoria: 'Inteligência Artificial',
    descricao:
      'Como integrar modelos de linguagem em aplicações corporativas utilizando engenharia de prompt e RAG.',
    atividades: [
      {
        id: 23,
        titulo: 'Fundamentos de Modelos Generativos',
        horario: '15:00',
        local: 'Auditório de Ciências',
      },
      {
        id: 24,
        titulo: 'Arquiteturas RAG para Negócios',
        horario: '16:30',
        local: 'Auditório de Ciências',
      },
    ],
  },
  {
    id: 12,
    titulo: 'Palestra: Segurança Defensiva e SIEM',
    local: 'Sala Multiuso',
    endereco: 'Campus Central — Biblioteca',
    vagas: 35,
    dataInicio: '2026-10-21T19:00:00-03:00',
    categoria: 'Cibersegurança',
    descricao:
      'Monitoramento centralizado de logs, correlação de eventos e criação de regras de detecção para prevenção de invasões.',
    atividades: [
      {
        id: 25,
        titulo: 'Coleta e Análise de Logs de Sistema',
        horario: '19:00',
        local: 'Sala Multiuso',
      },
      {
        id: 26,
        titulo: 'Caça a Ameaças (Threat Hunting)',
        horario: '20:15',
        local: 'Sala Multiuso',
      },
    ],
  },
  {
    id: 13,
    titulo: 'Minicurso de TypeScript para Frontend',
    local: 'Laboratório 3',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 25,
    dataInicio: '2026-10-24T14:00:00-03:00',
    categoria: 'Desenvolvimento',
    descricao:
      'Transição prática de JavaScript para TypeScript: tipos primitivos, interfaces, generics e integração com frameworks reativos.',
    atividades: [
      { id: 27, titulo: 'Tipagem Estrita e Generics', horario: '14:00', local: 'Laboratório 3' },
      {
        id: 28,
        titulo: 'TypeScript Aplicado a Componentes',
        horario: '16:00',
        local: 'Laboratório 3',
      },
    ],
  },
  {
    id: 14,
    titulo: 'Mesa-Redonda: Mercado de TI e Carreiras',
    local: 'Auditório Principal',
    endereco: 'Campus Central — Bloco A',
    vagas: 100,
    dataInicio: '2026-10-28T19:30:00-03:00',
    categoria: 'Carreira',
    descricao:
      'Profissionais de empresas locais e globais discutem o mercado de desenvolvimento, suporte corporativo e infraestrutura.',
    atividades: [
      {
        id: 29,
        titulo: 'Transição de Estágio para Júnior',
        horario: '19:30',
        local: 'Auditório Principal',
      },
      {
        id: 30,
        titulo: 'Perguntas e Respostas com Especialistas',
        horario: '20:45',
        local: 'Auditório Principal',
      },
    ],
  },
  {
    id: 15,
    titulo: 'Workshop de Git e Estratégias de Branching',
    local: 'Laboratório 1',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 22,
    dataInicio: '2026-11-04T18:30:00-03:00',
    categoria: 'Desenvolvimento',
    descricao:
      'Domine fluxos de trabalho em equipe com Gitflow, resolução de conflitos de merge, rebase e padronização de commits.',
    atividades: [
      {
        id: 31,
        titulo: 'Fluxos de Trabalho: Trunk-based vs Gitflow',
        horario: '18:30',
        local: 'Laboratório 1',
      },
      {
        id: 32,
        titulo: 'Resolução de Conflitos e Rebase',
        horario: '20:00',
        local: 'Laboratório 1',
      },
    ],
  },
  {
    id: 16,
    titulo: 'Jornada de Automação de Infraestrutura',
    local: 'Laboratório 4',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 12,
    dataInicio: '2026-11-07T14:00:00-03:00',
    categoria: 'Infraestrutura',
    descricao:
      'Gerenciamento de configurações e provisionamento automatizado de servidores com Ansible e scripts de automação.',
    atividades: [
      {
        id: 33,
        titulo: 'Introdução a Playbooks do Ansible',
        horario: '14:00',
        local: 'Laboratório 4',
      },
      {
        id: 34,
        titulo: 'Configuração Automatizada de Servidores',
        horario: '16:00',
        local: 'Laboratório 4',
      },
    ],
  },
  {
    id: 17,
    titulo: 'Oficina de Testes Automatizados e TDD',
    local: 'Laboratório 2',
    endereco: 'Campus Central — Bloco de Laboratórios',
    vagas: 16,
    dataInicio: '2026-11-11T19:00:00-03:00',
    categoria: 'Desenvolvimento',
    descricao:
      'Escrevendo código confiável com testes unitários, testes de integração, mocks e desenvolvimento orientado a testes.',
    atividades: [
      { id: 35, titulo: 'Testes Unitários e Mocking', horario: '19:00', local: 'Laboratório 2' },
      { id: 36, titulo: 'Prática de TDD do Zero', horario: '20:30', local: 'Laboratório 2' },
    ],
  },
  {
    id: 18,
    titulo: 'Encontro de Hardware e Homelabs',
    local: 'Sala Multiuso',
    endereco: 'Campus Central — Biblioteca',
    vagas: 28,
    dataInicio: '2026-11-18T18:30:00-03:00',
    categoria: 'Hardware',
    descricao:
      'Montagem de servidores caseiros, virtualização com hypervisors, storage em rede (NAS) e automação residencial.',
    atividades: [
      {
        id: 37,
        titulo: 'Escolha de Peças e Virtualização',
        horario: '18:30',
        local: 'Sala Multiuso',
      },
      {
        id: 38,
        titulo: 'Showcase de Homelabs dos Alunos',
        horario: '20:15',
        local: 'Sala Multiuso',
      },
    ],
  },
  {
    id: 19,
    titulo: 'Seminário de Privacidade e LGPD',
    local: 'Auditório de Ciências',
    endereco: 'Campus Tecnológico — Bloco D',
    vagas: 45,
    dataInicio: '2026-11-21T10:00:00-03:00',
    categoria: 'Cibersegurança',
    descricao:
      'Impactos da Lei Geral de Proteção de Dados na arquitetura de sistemas e diretrizes para tratamento ético de dados.',
    atividades: [
      {
        id: 39,
        titulo: 'Princípios da LGPD para Desenvolvedores',
        horario: '10:00',
        local: 'Auditório de Ciências',
      },
      {
        id: 40,
        titulo: 'Anonimização e Políticas de Retenção',
        horario: '11:15',
        local: 'Auditório de Ciências',
      },
    ],
  },
  {
    id: 20,
    titulo: 'Feira Anual de Projetos e TCCs',
    local: 'Hall Central do Campus',
    endereco: 'Campus Central — Hall Principal',
    vagas: 200,
    dataInicio: '2026-11-27T18:00:00-03:00',
    categoria: 'Acadêmico',
    descricao:
      'Exposição de sistemas de software, protótipos de engenharia e pesquisas acadêmicas desenvolvidas pelos formandos.',
    atividades: [
      {
        id: 41,
        titulo: 'Abertura dos Stands de Apresentação',
        horario: '18:00',
        local: 'Hall Central',
      },
      { id: 42, titulo: 'Avaliação da Banca Examinadora', horario: '19:30', local: 'Hall Central' },
      {
        id: 43,
        titulo: 'Encerramento e Premiação de Destaque',
        horario: '21:30',
        local: 'Auditório Principal',
      },
    ],
  },
]
