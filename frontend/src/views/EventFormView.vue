<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import MediaUploadField from '@/features/media/components/MediaUploadField.vue'
import {
  validarEvento,
  type DadosFormularioEvento,
  type ErrosEvento,
} from '@/features/events/utils/validarEvento'

const dados = reactive<DadosFormularioEvento>({
  titulo: '',
  descricao: '',
  dataInicio: '',
  local: '',
  controlaVagas: true,
  capacidade: null,
})
const erros = ref<ErrosEvento>({})
const banner = ref<File | null>(null)
const mensagem = ref('')

function salvarRascunho(): void {
  mensagem.value = ''
  erros.value = validarEvento(dados)

  if (Object.keys(erros.value).length === 0) {
    mensagem.value = banner.value
      ? 'Evento e banner validados. A persistência e o upload S3 aguardam a API REST.'
      : 'Evento validado. A persistência aguarda a API REST; o banner permanece opcional.'
  }
}
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <nav aria-label="Navegação estrutural">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><RouterLink to="/organizador">Organizador</RouterLink></li>
        <li class="breadcrumb-item active" aria-current="page">Novo evento</li>
      </ol>
    </nav>

    <div class="row g-4">
      <div class="col-lg-8">
        <div class="mb-4">
          <p class="text-primary-custom fw-semibold small text-uppercase mb-1">Gestão de eventos</p>
          <h1 class="h2 fw-bold mb-1">Criar novo evento</h1>
          <p class="text-muted mb-0">Comece pelas informações essenciais e salve como rascunho.</p>
        </div>

        <div v-if="mensagem" class="alert alert-info" role="status">{{ mensagem }}</div>

        <form novalidate @submit.prevent="salvarRascunho">
          <section class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <h2 class="h5 fw-bold mb-4">Informações básicas</h2>

              <div class="mb-3">
                <label class="form-label fw-semibold" for="evento-titulo">Título</label>
                <input
                  id="evento-titulo"
                  v-model="dados.titulo"
                  class="form-control form-control-lg"
                  :class="{ 'is-invalid': erros.titulo }"
                  placeholder="Ex.: Simpósio de Tecnologia 2026"
                />
                <div v-if="erros.titulo" class="invalid-feedback">{{ erros.titulo }}</div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-semibold" for="evento-descricao">Descrição</label>
                <textarea
                  id="evento-descricao"
                  v-model="dados.descricao"
                  class="form-control"
                  :class="{ 'is-invalid': erros.descricao }"
                  rows="5"
                ></textarea>
                <div v-if="erros.descricao" class="invalid-feedback">{{ erros.descricao }}</div>
              </div>

              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold" for="evento-data">Data e horário</label>
                  <input
                    id="evento-data"
                    v-model="dados.dataInicio"
                    class="form-control"
                    :class="{ 'is-invalid': erros.dataInicio }"
                    type="datetime-local"
                  />
                  <div v-if="erros.dataInicio" class="invalid-feedback">{{ erros.dataInicio }}</div>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-semibold" for="evento-local"
                    >Local ou modalidade</label
                  >
                  <input
                    id="evento-local"
                    v-model="dados.local"
                    class="form-control"
                    :class="{ 'is-invalid': erros.local }"
                  />
                  <div v-if="erros.local" class="invalid-feedback">{{ erros.local }}</div>
                </div>
              </div>
            </div>
          </section>

          <section class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <h2 class="h5 fw-bold mb-4">Inscrições e capacidade</h2>
              <div class="form-check form-switch mb-3">
                <input
                  id="controla-vagas"
                  v-model="dados.controlaVagas"
                  class="form-check-input"
                  type="checkbox"
                  role="switch"
                />
                <label class="form-check-label fw-semibold" for="controla-vagas">
                  Controlar limite de vagas
                </label>
              </div>
              <div v-if="dados.controlaVagas" class="col-md-5">
                <label class="form-label fw-semibold" for="evento-capacidade">Capacidade</label>
                <input
                  id="evento-capacidade"
                  v-model.number="dados.capacidade"
                  class="form-control"
                  :class="{ 'is-invalid': erros.capacidade }"
                  type="number"
                  min="1"
                />
                <div v-if="erros.capacidade" class="invalid-feedback">{{ erros.capacidade }}</div>
              </div>
            </div>
          </section>

          <section class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <MediaUploadField
                id="banner-evento"
                label="Banner do evento"
                finalidade="BANNER_EVENTO"
                ajuda="Proporção recomendada 16:9. JPEG, PNG ou WebP, até 5 MB."
                @selecionar="banner = $event"
              />
            </div>
          </section>

          <div class="d-flex flex-column flex-sm-row justify-content-end gap-2">
            <RouterLink class="btn btn-outline-secondary btn-lg" to="/organizador"
              >Cancelar</RouterLink
            >
            <button class="btn btn-primary-custom btn-lg" type="submit">Salvar rascunho</button>
          </div>
        </form>
      </div>

      <aside class="col-lg-4">
        <div class="card border-0 bg-primary-subtle sticky-lg-top">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold text-primary-custom">Fluxo preparado para REST + S3</h2>
            <ol class="small text-secondary ps-3 mb-0">
              <li class="mb-2">A API cria o evento como rascunho.</li>
              <li class="mb-2">A API autoriza um upload específico.</li>
              <li class="mb-2">O navegador envia o banner pela URL pré-assinada.</li>
              <li>O backend associa a mídia ao evento.</li>
            </ol>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
