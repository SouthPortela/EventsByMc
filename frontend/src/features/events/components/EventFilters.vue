<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { FiltrosEvento } from '../utils/filtrarEventos'

const props = defineProps<{
  categorias: string[]
  locais: string[]
  valoresIniciais: FiltrosEvento
}>()

const emit = defineEmits<{
  filtrar: [filtros: FiltrosEvento]
}>()

const filtros = reactive<FiltrosEvento>({ termo: '', categoria: '', local: '' })

watch(
  () => props.valoresIniciais,
  (valores) => Object.assign(filtros, valores),
  { deep: true, immediate: true },
)

function aplicarFiltros(): void {
  emit('filtrar', { ...filtros })
}

function limparFiltros(): void {
  filtros.termo = ''
  filtros.categoria = ''
  filtros.local = ''
  aplicarFiltros()
}
</script>

<template>
  <form class="card border-0 shadow-sm" role="search" @submit.prevent="aplicarFiltros">
    <div class="card-body p-3 p-lg-4">
      <div class="row g-3 align-items-end">
        <div class="col-lg-5">
          <label class="form-label fw-semibold" for="filtro-termo">O que você procura?</label>
          <input
            id="filtro-termo"
            v-model="filtros.termo"
            class="form-control form-control-lg"
            type="search"
            placeholder="Evento, atividade ou local"
          />
        </div>
        <div class="col-md-5 col-lg-3">
          <label class="form-label fw-semibold" for="filtro-categoria">Categoria</label>
          <select
            id="filtro-categoria"
            v-model="filtros.categoria"
            class="form-select form-select-lg"
          >
            <option value="">Todas as categorias</option>
            <option v-for="categoria in categorias" :key="categoria" :value="categoria">
              {{ categoria }}
            </option>
          </select>
        </div>
        <div class="col-md-5 col-lg-2">
          <label class="form-label fw-semibold" for="filtro-local">Local</label>
          <select id="filtro-local" v-model="filtros.local" class="form-select form-select-lg">
            <option value="">Todos os locais</option>
            <option v-for="local in locais" :key="local" :value="local">{{ local }}</option>
          </select>
        </div>
        <div class="col-md-2 col-lg-2 d-grid gap-2">
          <button class="btn btn-primary btn-lg" type="submit">Buscar</button>
          <button
            class="btn btn-link btn-sm text-decoration-none"
            type="button"
            @click="limparFiltros"
          >
            Limpar
          </button>
        </div>
      </div>
    </div>
  </form>
</template>
