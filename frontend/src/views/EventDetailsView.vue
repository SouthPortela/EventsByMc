<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import { buscarEventoPorId } from '@/features/events/services/eventoService'
import type { EventoDetalhe } from '@/features/events/types/evento'
import { formatarData } from '@/features/events/utils/formatarData'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const evento = ref<EventoDetalhe | null>(null)
const carregando = ref(true)
const mensagemErro = ref('')
const mensagemInscricao = ref('')

function solicitarInscricao(): void {
  mensagemInscricao.value =
    'Solicitação validada na interface. A confirmação será feita pela API REST.'
}

async function carregarEvento(): Promise<void> {
  const id = Number(route.params.id)

  if (!Number.isInteger(id) || id <= 0) {
    mensagemErro.value = 'O identificador do evento é inválido.'
    carregando.value = false
    return
  }

  try {
    evento.value = (await buscarEventoPorId(id)) ?? null

    if (!evento.value) {
      mensagemErro.value = 'Evento não encontrado.'
    }
  } catch {
    mensagemErro.value = 'Não foi possível carregar os detalhes do evento.'
  } finally {
    carregando.value = false
  }
}

onMounted(carregarEvento)
</script>

<template>
  <div class="container py-5">
    <div v-if="carregando" class="d-flex justify-content-center py-5" role="status">
      <div class="spinner-border text-primary">
        <span class="visually-hidden">Carregando evento...</span>
      </div>
    </div>

    <div v-else-if="mensagemErro" class="text-center py-5">
      <div class="alert alert-warning border-0" role="alert">{{ mensagemErro }}</div>
      <RouterLink class="btn btn-primary" to="/">Voltar para os eventos</RouterLink>
    </div>

    <div v-else-if="evento">
      <nav aria-label="Navegação estrutural">
        <ol class="breadcrumb mb-4">
          <li class="breadcrumb-item"><RouterLink to="/">Eventos</RouterLink></li>
          <li class="breadcrumb-item active" aria-current="page">Detalhes</li>
        </ol>
      </nav>

      <section class="card border-0 shadow-sm overflow-hidden mb-5">
        <div class="row g-0">
          <div class="col-lg-6 bg-primary-subtle event-detail-banner">
            <img
              v-if="evento.banner"
              class="w-100 h-100 object-fit-cover"
              :src="evento.banner.url"
              :alt="evento.banner.textoAlternativo"
            />
            <div v-else class="h-100 d-flex align-items-center justify-content-center">
              <AppIcon class="text-primary-custom" name="calendar" :size="72" />
            </div>
          </div>
          <div class="col-lg-6">
            <div class="card-body p-4 p-lg-5">
              <span class="badge rounded-pill text-bg-primary-subtle text-primary-custom mb-3">{{
                evento.categoria
              }}</span>
              <h1 class="display-6 fw-bold mb-3">{{ evento.titulo }}</h1>
              <p class="lead text-secondary">{{ evento.descricao }}</p>

              <div class="row g-3 mt-2">
                <div class="col-sm-6">
                  <p class="small text-muted text-uppercase fw-semibold mb-1">Data e horário</p>
                  <p class="fw-semibold d-flex align-items-start gap-2 mb-0">
                    <AppIcon class="text-primary-custom mt-1" name="calendar" :size="17" />
                    {{ formatarData(evento.dataInicio) }}
                  </p>
                </div>
                <div class="col-sm-6">
                  <p class="small text-muted text-uppercase fw-semibold mb-1">Local</p>
                  <p class="fw-semibold d-flex align-items-start gap-2 mb-0">
                    <AppIcon class="text-primary-custom mt-1" name="map-pin" :size="17" />
                    <span
                      >{{ evento.local
                      }}<small class="text-muted d-block">{{ evento.endereco }}</small></span
                    >
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div class="row g-5">
        <div class="col-lg-8">
          <section class="mb-5">
            <p class="text-primary-custom fw-semibold small text-uppercase mb-1">Cronograma</p>
            <h2 class="h3 fw-bold mb-4">Programação do evento</h2>
            <div class="vstack gap-3">
              <article
                v-for="atividade in evento.atividades"
                :key="atividade.id"
                class="card border-0 shadow-sm"
              >
                <div class="card-body p-4">
                  <div class="row align-items-center g-3">
                    <div class="col-sm-2">
                      <span class="badge text-bg-primary-subtle text-primary-custom px-3 py-2">
                        {{ atividade.horario }}
                      </span>
                    </div>
                    <div class="col-sm-7">
                      <h3 class="h6 fw-bold mb-0">{{ atividade.titulo }}</h3>
                    </div>
                    <div class="col-sm-3 text-sm-end text-muted small">{{ atividade.local }}</div>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section>
            <p class="text-primary-custom fw-semibold small text-uppercase mb-1">Convidados</p>
            <h2 class="h3 fw-bold mb-4">Pessoas em destaque</h2>
            <div v-if="evento.pessoas?.length" class="row g-3">
              <div v-for="pessoa in evento.pessoas" :key="pessoa.id" class="col-md-6">
                <article class="card border-0 shadow-sm h-100">
                  <div class="card-body d-flex align-items-center gap-3 p-4">
                    <div
                      class="bg-primary-subtle text-primary-custom rounded-circle d-flex align-items-center justify-content-center p-3"
                    >
                      <span class="fw-bold">{{ pessoa.nome.charAt(0) }}</span>
                    </div>
                    <div>
                      <h3 class="h6 fw-bold mb-1">{{ pessoa.nome }}</h3>
                      <p class="small text-muted mb-0">{{ pessoa.papel }}</p>
                    </div>
                  </div>
                </article>
              </div>
            </div>
            <p v-else class="text-muted">As pessoas vinculadas serão divulgadas em breve.</p>
          </section>
        </div>

        <aside class="col-lg-4">
          <div class="card border-0 shadow-sm event-sticky-card">
            <div class="card-body p-4">
              <p class="text-primary-custom fw-semibold small text-uppercase mb-1">Participação</p>
              <h2 class="h4 fw-bold">Garanta sua vaga</h2>
              <p class="h5 fw-bold text-dark">
                {{ evento.gratuito ? 'Gratuito' : (evento.preco ?? 'Consulte o organizador') }}
              </p>
              <p v-if="evento.vagas > 0" class="text-success fw-semibold">
                {{ evento.vagas }} vagas disponíveis
              </p>
              <p v-else class="text-danger fw-semibold">Vagas esgotadas</p>
              <p class="small text-muted">
                A inscrição será confirmada após autenticação e validação das regras do evento.
              </p>

              <div v-if="mensagemInscricao" class="alert alert-success small" role="status">
                {{ mensagemInscricao }}
              </div>

              <RouterLink
                v-if="evento.vagas > 0 && !auth.autenticado"
                class="btn btn-primary-custom btn-lg w-100"
                :to="{ name: 'login', query: { redirect: route.fullPath } }"
              >
                Entrar para se inscrever
              </RouterLink>
              <button
                v-else-if="evento.vagas > 0"
                class="btn btn-primary-custom btn-lg w-100"
                type="button"
                @click="solicitarInscricao"
              >
                Confirmar inscrição
              </button>
              <button v-else class="btn btn-secondary btn-lg w-100" type="button" disabled>
                Inscrições encerradas
              </button>
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>
