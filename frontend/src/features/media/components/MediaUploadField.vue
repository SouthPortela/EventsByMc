<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import type { FinalidadeMidia } from '../types/media'
import { validarImagem } from '../utils/validarImagem'

const props = defineProps<{
  id: string
  label: string
  finalidade: FinalidadeMidia
  ajuda: string
}>()

const emit = defineEmits<{
  selecionar: [arquivo: File | null]
}>()

const previewUrl = ref('')
const nomeArquivo = ref('')
const erro = ref('')

function liberarPreview(): void {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
    previewUrl.value = ''
  }
}

function selecionarArquivo(event: Event): void {
  const input = event.target as HTMLInputElement
  const arquivo = input.files?.[0] ?? null
  erro.value = ''

  if (!arquivo) {
    removerArquivo()
    return
  }

  const mensagemErro = validarImagem(arquivo, props.finalidade)

  if (mensagemErro) {
    erro.value = mensagemErro
    input.value = ''
    return
  }

  liberarPreview()
  previewUrl.value = URL.createObjectURL(arquivo)
  nomeArquivo.value = arquivo.name
  emit('selecionar', arquivo)
}

function removerArquivo(): void {
  liberarPreview()
  nomeArquivo.value = ''
  erro.value = ''
  emit('selecionar', null)
}

onUnmounted(liberarPreview)
</script>

<template>
  <div>
    <label class="form-label fw-semibold" :for="id">{{ label }}</label>
    <div class="card border border-2 bg-light">
      <div class="card-body p-4">
        <div v-if="previewUrl" class="row align-items-center g-3">
          <div class="col-sm-5">
            <div class="ratio ratio-16x9 rounded overflow-hidden bg-primary-subtle">
              <img
                class="w-100 h-100 object-fit-cover"
                :src="previewUrl"
                alt="Prévia da imagem selecionada"
              />
            </div>
          </div>
          <div class="col-sm-7">
            <p class="fw-semibold text-break mb-1">{{ nomeArquivo }}</p>
            <p class="small text-success mb-3">Imagem pronta para envio.</p>
            <button class="btn btn-outline-danger btn-sm" type="button" @click="removerArquivo">
              Remover imagem
            </button>
          </div>
        </div>

        <div v-else class="text-center py-3">
          <p class="fw-semibold mb-1">Selecione uma imagem</p>
          <p class="small text-muted mb-3">{{ ajuda }}</p>
          <input
            :id="id"
            class="form-control"
            :class="{ 'is-invalid': erro }"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            @change="selecionarArquivo"
          />
          <div v-if="erro" class="invalid-feedback text-start">{{ erro }}</div>
        </div>
      </div>
    </div>
  </div>
</template>
