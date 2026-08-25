<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import type { EventoResumo } from '../types/evento'

const props = defineProps<{ evento: EventoResumo }>()
const favorito = ref(false)

const formatadorData = new Intl.DateTimeFormat('pt-BR', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
})

function alternarFavorito(): void {
  favorito.value = !favorito.value
}

async function compartilhar(): Promise<void> {
  const url = `${window.location.origin}/eventos/${props.evento.id}`

  try {
    if (navigator.share) {
      await navigator.share({ title: props.evento.titulo, url })
      return
    }

    await navigator.clipboard?.writeText(url)
  } catch {
    // Cancelar o compartilhamento não deve interromper a navegação do cartão.
  }
}
</script>

<template>
  <article class="event-card">
    <div class="event-card-img-wrapper">
      <img
        v-if="evento.banner"
        class="event-card-img"
        :src="evento.banner.url"
        :alt="evento.banner.textoAlternativo"
      />
      <div
        v-else
        class="w-100 h-100 bg-primary-subtle d-flex align-items-center justify-content-center"
      >
        <AppIcon class="text-primary-custom" name="calendar" :size="48" />
      </div>
      <span class="event-category-badge">{{ evento.categoria }}</span>
      <button
        class="event-heart-btn"
        :class="{ 'text-danger': favorito }"
        type="button"
        :aria-label="favorito ? 'Remover dos favoritos' : 'Adicionar aos favoritos'"
        :aria-pressed="favorito"
        @click="alternarFavorito"
      >
        <AppIcon name="heart" :size="17" />
      </button>
    </div>

    <div class="d-flex flex-column flex-grow-1 p-3 p-lg-4">
      <p class="small fw-bold text-primary-custom text-uppercase mb-2">
        {{ formatadorData.format(new Date(evento.dataInicio)) }}
      </p>
      <h3 class="h5 fw-bold text-truncate-2 mb-3">{{ evento.titulo }}</h3>
      <p class="d-flex align-items-start gap-2 small text-secondary mb-3">
        <AppIcon class="flex-shrink-0 mt-1" name="map-pin" :size="15" />
        <span>{{ evento.local }}</span>
      </p>
      <div class="d-flex align-items-center justify-content-between gap-3 mt-auto pt-2">
        <span class="fw-bold text-dark">
          {{ evento.gratuito ? 'Gratuito' : (evento.preco ?? 'Consulte') }}
        </span>
        <div class="d-flex align-items-center gap-2">
          <button
            class="btn btn-link text-secondary p-1"
            type="button"
            aria-label="Compartilhar evento"
            @click="compartilhar"
          >
            <AppIcon name="share" :size="18" />
          </button>
          <RouterLink class="btn btn-primary-custom btn-sm px-3" :to="`/eventos/${evento.id}`">
            Ver evento
          </RouterLink>
        </div>
      </div>
    </div>
  </article>
</template>
