<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppIcon, { type IconName } from '@/components/icons/AppIcon.vue'
import EventGrid from '@/features/events/components/EventGrid.vue'
import { listarEventos } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'

interface CategoriaHome {
  nome: string
  icon: IconName
  termos: string[]
}

const categorias: CategoriaHome[] = [
  {
    nome: 'Tecnologia',
    icon: 'monitor',
    termos: [
      'ciberseguranca',
      'desenvolvimento',
      'devops',
      'cloud',
      'redes',
      'infraestrutura',
      'hardware',
      'banco de dados',
      'inteligencia artificial',
    ],
  },
  {
    nome: 'Cursos e workshops',
    icon: 'clipboard',
    termos: ['oficina', 'workshop', 'minicurso', 'bootcamp'],
  },
  {
    nome: 'Negócios e carreiras',
    icon: 'briefcase',
    termos: ['carreira', 'inovacao', 'mercado'],
  },
  {
    nome: 'Acadêmico',
    icon: 'book',
    termos: ['academico', 'simposio', 'seminario', 'feira'],
  },
]

const route = useRoute()
const router = useRouter()
const eventos = ref<EventoResumo[]>([])
const carregando = ref(true)
const mensagemErro = ref('')
const limite = ref(9)

const busca = computed(() => (typeof route.query.busca === 'string' ? route.query.busca : ''))
const categoriaAtiva = computed(() =>
  typeof route.query.categoria === 'string' ? route.query.categoria : '',
)
const destaques = computed(() => eventos.value.filter((evento) => evento.destaque).slice(0, 3))
const eventosFiltrados = computed(() => {
  const termo = normalizarTexto(busca.value)
  const categoria = categorias.find((item) => item.nome === categoriaAtiva.value)

  return eventos.value.filter((evento) => {
    const conteudo = normalizarTexto(`${evento.titulo} ${evento.local} ${evento.categoria}`)
    const correspondeBusca = !termo || conteudo.includes(termo)
    const correspondeCategoria =
      !categoria || categoria.termos.some((item) => conteudo.includes(normalizarTexto(item)))

    return correspondeBusca && correspondeCategoria
  })
})
const eventosVisiveis = computed(() => eventosFiltrados.value.slice(0, limite.value))

async function carregarEventos(): Promise<void> {
  try {
    eventos.value = await listarEventos()
  } catch {
    mensagemErro.value = 'Não foi possível carregar os eventos.'
  } finally {
    carregando.value = false
  }
}

function selecionarCategoria(nome: string): void {
  const proximaCategoria = categoriaAtiva.value === nome ? undefined : nome
  void router.replace({
    name: 'home',
    query: { ...route.query, categoria: proximaCategoria },
    hash: '#eventos',
  })
}

watch([busca, categoriaAtiva], () => {
  limite.value = 9
})

onMounted(carregarEventos)
</script>

<template>
  <main>
    <section class="hero-section text-white">
      <img
        class="hero-bg-image"
        src="https://images.unsplash.com/photo-1492684223066-81342ee5ff30?auto=format&fit=crop&w=1920&q=85"
        alt="Grande público reunido em um evento"
      />
      <div class="hero-overlay"></div>
      <div class="container hero-content">
        <div class="col-lg-8 col-xl-7">
          <span
            class="btn-glass d-inline-flex align-items-center gap-2 px-3 py-2 small fw-semibold mb-4"
          >
            <AppIcon name="calendar" :size="17" /> Encontros que transformam ideias
          </span>
          <h1 class="display-3 fw-bold lh-sm mb-4">
            Viva experiências.<br />Conecte-se com pessoas.
          </h1>
          <p class="lead text-white-50 col-lg-10 mb-4">
            Descubra eventos, cursos e encontros profissionais. Organize sua agenda e acompanhe sua
            participação em um só lugar.
          </p>
          <div class="d-flex flex-wrap gap-3">
            <a class="btn btn-light rounded-pill fw-bold px-4 py-3" href="#eventos">
              Encontrar eventos
            </a>
            <RouterLink class="btn btn-glass fw-bold px-4 py-3" to="/login?redirect=/organizador">
              Criar meu evento
            </RouterLink>
          </div>
        </div>
      </div>
    </section>

    <section class="bg-white py-5">
      <div class="container py-lg-4">
        <div class="d-flex justify-content-between align-items-end gap-3 mb-4">
          <div>
            <p class="small text-primary-custom fw-bold text-uppercase mb-2">
              Escolhidos para você
            </p>
            <h2 class="h2 fw-bold mb-0">Eventos em destaque</h2>
          </div>
          <a
            class="d-none d-sm-flex align-items-center gap-1 text-primary-custom fw-bold text-decoration-none"
            href="#eventos"
          >
            Ver todos <AppIcon name="chevron-right" :size="18" />
          </a>
        </div>
        <div v-if="carregando" class="text-center py-5" role="status">
          <span class="spinner-border text-primary-custom">
            <span class="visually-hidden">Carregando...</span>
          </span>
        </div>
        <EventGrid v-else :eventos="destaques" />
      </div>
    </section>

    <section class="py-5">
      <div class="container py-lg-4">
        <div class="text-center mb-5">
          <p class="small text-primary-custom fw-bold text-uppercase mb-2">
            Explore seus interesses
          </p>
          <h2 class="h2 fw-bold mb-2">Encontre por categoria</h2>
          <p class="text-secondary mb-0">
            Escolha o tema e encontre a experiência certa para você.
          </p>
        </div>
        <div class="row g-3 g-lg-4">
          <div v-for="categoria in categorias" :key="categoria.nome" class="col-6 col-lg-3">
            <button
              class="category-box border-0 w-100 h-100"
              :class="{ active: categoriaAtiva === categoria.nome }"
              type="button"
              :aria-pressed="categoriaAtiva === categoria.nome"
              @click="selecionarCategoria(categoria.nome)"
            >
              <span class="category-icon-wrapper text-primary-custom">
                <AppIcon :name="categoria.icon" :size="27" />
              </span>
              <span class="category-text d-block fw-bold text-dark">{{ categoria.nome }}</span>
            </button>
          </div>
        </div>
      </div>
    </section>

    <section id="eventos" class="bg-white py-5">
      <div class="container py-lg-4">
        <div
          class="d-flex flex-column flex-sm-row justify-content-between align-items-sm-end gap-3 mb-4"
        >
          <div>
            <p class="small text-primary-custom fw-bold text-uppercase mb-2">Próximos de você</p>
            <h2 class="h2 fw-bold mb-1">
              {{ busca ? `Resultados para “${busca}”` : 'Próximos eventos' }}
            </h2>
            <p class="text-secondary mb-0">{{ eventosFiltrados.length }} evento(s) encontrado(s)</p>
          </div>
          <button
            v-if="busca || categoriaAtiva"
            class="btn btn-outline-secondary"
            type="button"
            @click="router.replace({ name: 'home', hash: '#eventos' })"
          >
            Limpar filtros
          </button>
        </div>

        <div v-if="mensagemErro" class="alert alert-danger" role="alert">
          {{ mensagemErro }}
        </div>
        <EventGrid v-else-if="!carregando" :eventos="eventosVisiveis" />
        <div v-if="eventosFiltrados.length > eventosVisiveis.length" class="text-center mt-5">
          <button
            class="btn btn-outline-primary-custom rounded-pill px-5 py-2"
            type="button"
            @click="limite += 6"
          >
            Carregar mais eventos
          </button>
        </div>
      </div>
    </section>

    <section class="bg-primary-custom text-white py-5">
      <div class="container py-lg-4">
        <div class="row align-items-center g-4">
          <div class="col-lg-8">
            <h2 class="display-6 fw-bold mb-3">Tem uma ideia? Transforme-a em um grande evento.</h2>
            <p class="lead text-white-50 mb-0">
              Publique, gerencie inscrições e acompanhe a frequência da sua programação.
            </p>
          </div>
          <div class="col-lg-4 text-lg-end">
            <RouterLink
              class="btn btn-light rounded-pill text-primary-custom fw-bold px-4 py-3"
              to="/login?redirect=/organizador"
            >
              Começar agora <AppIcon name="chevron-right" :size="18" />
            </RouterLink>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
