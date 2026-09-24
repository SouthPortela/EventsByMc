<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { criarEvento } from '@/features/events/services/eventoService'
import { categoriasEvento, ehCategoriaEvento } from '@/features/events/types/categoria'
import {
  validarEvento,
  type DadosFormularioEvento,
  type ErrosEvento,
} from '@/features/events/utils/validarEvento'
import { ApiError } from '@/shared/services/httpClient'

const router = useRouter()
const dados = reactive<DadosFormularioEvento>({
  titulo: '',
  descricao: '',
  dataInicio: '',
  dataFim: '',
  local: '',
  categoria: '',
})
const erros = ref<ErrosEvento>({})
const mensagem = ref('')
const salvando = ref(false)

async function salvarRascunho(): Promise<void> {
  if (salvando.value) return
  mensagem.value = ''
  erros.value = validarEvento(dados)
  if (Object.keys(erros.value).length) return
  if (!ehCategoriaEvento(dados.categoria)) return
  salvando.value = true
  try {
    const evento = await criarEvento({
      ...dados,
      titulo: dados.titulo.trim(),
      descricao: dados.descricao.trim(),
      local: dados.local.trim(),
      categoria: dados.categoria,
    })
    await router.push({ name: 'organizer-dashboard', query: { criado: evento.id } })
  } catch (e) {
    mensagem.value =
      e instanceof ApiError
        ? e.message
        : 'Não foi possível confirmar o salvamento. Consulte seu painel antes de tentar novamente.'
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <nav aria-label="Navegação estrutural" class="mb-4">
      <RouterLink to="/organizador">Meus eventos</RouterLink> / Novo evento
    </nav>
    <div class="row g-4">
      <div class="col-lg-8">
        <h1 class="h2 fw-bold">Criar novo evento</h1>
        <p class="text-muted">Salve um rascunho e publique quando estiver pronto.</p>
        <div v-if="mensagem" class="alert alert-warning" role="alert">{{ mensagem }}</div>
        <form novalidate @submit.prevent="salvarRascunho">
          <fieldset :disabled="salvando" class="card border-0 shadow-sm">
            <div class="card-body p-4">
              <div class="mb-3">
                <label for="titulo" class="form-label">Título</label>
                <input
                  id="titulo"
                  v-model="dados.titulo"
                  class="form-control"
                  :class="{ 'is-invalid': erros.titulo }"
                  maxlength="200"
                  required
                />
                <div class="invalid-feedback">{{ erros.titulo }}</div>
              </div>
              <div class="mb-3">
                <label for="descricao" class="form-label">Descrição</label>
                <textarea
                  id="descricao"
                  v-model="dados.descricao"
                  class="form-control"
                  :class="{ 'is-invalid': erros.descricao }"
                  rows="5"
                  maxlength="10000"
                  required
                ></textarea>
                <div class="invalid-feedback">{{ erros.descricao }}</div>
              </div>
              <div class="row g-3 mb-3">
                <div class="col-md-6">
                  <label for="inicio" class="form-label">Início</label>
                  <input
                    id="inicio"
                    v-model="dados.dataInicio"
                    type="datetime-local"
                    class="form-control"
                    :class="{ 'is-invalid': erros.dataInicio }"
                    required
                  />
                  <div class="invalid-feedback">{{ erros.dataInicio }}</div>
                </div>
                <div class="col-md-6">
                  <label for="fim" class="form-label">Término</label>
                  <input
                    id="fim"
                    v-model="dados.dataFim"
                    type="datetime-local"
                    class="form-control"
                    :class="{ 'is-invalid': erros.dataFim }"
                    required
                  />
                  <div class="invalid-feedback">{{ erros.dataFim }}</div>
                </div>
              </div>
              <div class="mb-4">
                <label for="categoria" class="form-label">Categoria</label>
                <select id="categoria" v-model="dados.categoria" class="form-select" :class="{ 'is-invalid': erros.categoria }" required>
                  <option value="" disabled>Selecione uma categoria</option>
                  <option v-for="categoria in categoriasEvento" :key="categoria.codigo" :value="categoria.codigo">
                    {{ categoria.nome }}
                  </option>
                </select>
                <div class="invalid-feedback">{{ erros.categoria }}</div>
              </div>
              <div class="mb-4">
                <label for="local" class="form-label">Local</label>
                <input
                  id="local"
                  v-model="dados.local"
                  class="form-control"
                  :class="{ 'is-invalid': erros.local }"
                  maxlength="200"
                  required
                />
                <div class="invalid-feedback">{{ erros.local }}</div>
              </div>
              <div class="d-flex justify-content-end gap-2">
                <RouterLink class="btn btn-outline-secondary" to="/organizador">Voltar</RouterLink>
                <button class="btn btn-primary-custom" type="submit">
                  {{ salvando ? 'Salvando...' : 'Salvar rascunho' }}
                </button>
              </div>
            </div>
          </fieldset>
        </form>
      </div>
      <aside class="col-lg-4">
        <div class="card border-0 bg-primary-subtle">
          <div class="card-body p-4">
            <h2 class="h5">Como funciona</h2>
            <ol class="small ps-3">
              <li>Preencha as informações do evento.</li>
              <li>Salve o rascunho no banco de dados.</li>
              <li>Publique no painel para aparecer no catálogo.</li>
            </ol>
            <p class="small mb-0">
              Imagens, inscrições e edição da programação ainda não estão disponíveis neste
              formulário.
            </p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>
