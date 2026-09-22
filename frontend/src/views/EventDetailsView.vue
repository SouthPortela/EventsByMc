<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import { buscarEventoPorId } from '@/features/events/services/eventoService'
import type { EventoDetalhe } from '@/features/events/types/evento'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'
import { inscrever } from '@/features/attendance/services/presencaService'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const inscrevendo = ref(false)
const inscricaoConfirmada = ref(false)
const erroInscricao = ref('')
async function realizarInscricao(): Promise<void> {
  if (inscrevendo.value || !evento.value) return
  const id = evento.value.id
  erroInscricao.value = ''
  inscrevendo.value = true
  try {
    await inscrever(id)
    if (evento.value?.id === id) inscricaoConfirmada.value = true
  } catch (e) {
    if (evento.value?.id === id)
      erroInscricao.value =
        e instanceof ApiError
          ? e.message
          : 'Não foi possível confirmar a inscrição. Você pode tentar novamente.'
  } finally {
    inscrevendo.value = false
  }
}
const evento = ref<EventoDetalhe | null>(null)
const carregando = ref(true)
const mensagemErro = ref('')
let consultaAtual = 0

async function carregarEvento(): Promise<void> {
  inscricaoConfirmada.value = false
  erroInscricao.value = ''
  const consulta = ++consultaAtual
  const id = String(route.params.id ?? '')
  evento.value = null
  mensagemErro.value = ''
  carregando.value = true
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(id)) {
    mensagemErro.value = 'O identificador do evento é inválido.'
    carregando.value = false
    return
  }
  try {
    const resultado = await buscarEventoPorId(id)
    if (consulta === consultaAtual) evento.value = resultado
  } catch (e) {
    if (consulta === consultaAtual)
      mensagemErro.value =
        e instanceof ApiError && e.status === 404
          ? 'Evento não encontrado ou indisponível para consulta pública.'
          : 'Não foi possível carregar os detalhes do evento.'
  } finally {
    if (consulta === consultaAtual) carregando.value = false
  }
}
watch(() => route.params.id, carregarEvento, { immediate: true })
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
              <span
                v-if="evento.categoria"
                class="badge rounded-pill text-bg-primary-subtle text-primary-custom mb-3"
                >{{ evento.categoria }}</span
              >
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
            <p v-if="!evento.atividades.length" class="text-muted">
              Programação ainda não cadastrada.
            </p>
            <div class="vstack gap-3">
              <article
                v-for="atividade in evento.atividades"
                :key="atividade.id"
                class="card border-0 shadow-sm"
              >
                <div class="card-body p-4">
                  <div class="row align-items-center g-3">
                    <!-- Horário ajustado para col-sm-auto -->
                    <div class="col-sm-auto">
                <span class="badge text-bg-primary-subtle text-primary-custom px-3 py-2">
                  {{ atividade.horario }}
                </span>
                    </div>
                    <!-- Título ajustado para col-sm (preenche o meio) e text-sm-center -->
                    <div class="col-sm text-sm-center">
                      <h3 class="h6 fw-bold mb-0">{{ atividade.titulo }}</h3>
                    </div>
                    <!-- Local ajustado para col-sm-auto -->
                    <div class="col-sm-auto text-sm-end text-muted small">
                      {{ atividade.local }}
                    </div>
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
              <h2 class="h4 fw-bold">Inscrições</h2>
              <p class="small text-muted">
                Inscreva-se no evento para confirmar sua presença nas atividades.
              </p>
              <div v-if="erroInscricao" class="alert alert-warning" role="alert">
                {{ erroInscricao }}
              </div>
              <div v-if="inscricaoConfirmada" class="alert alert-success" role="status">
                Inscrição ativa confirmada.
              </div>
              <RouterLink
                v-if="!auth.autenticado"
                class="btn btn-primary-custom w-100"
                :to="{ name: 'login', query: { redirect: route.fullPath } }"
                >Entrar para se inscrever</RouterLink
              >
              <button
                v-else
                class="btn btn-primary-custom w-100"
                type="button"
                :disabled="inscrevendo || inscricaoConfirmada"
                @click="realizarInscricao"
              >
                {{
                  inscrevendo
                    ? 'Inscrevendo...'
                    : inscricaoConfirmada
                      ? 'Inscrição confirmada'
                      : 'Inscrever-me'
                }}
              </button>
              <RouterLink class="btn btn-outline-primary w-100 mt-3" to="/presenca"
                >Confirmar presença com código</RouterLink
              >
            </div>
          </div>
        </aside>
      </div>
    </div>
  </div>
</template>
