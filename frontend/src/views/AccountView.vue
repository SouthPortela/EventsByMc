<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import MediaUploadField from '@/features/media/components/MediaUploadField.vue'

const participante = {
  nome: 'Participante de demonstração',
  email: 'participante@exemplo.com',
}

const fotoSelecionada = ref<File | null>(null)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      title="Minha conta"
      description="Gerencie seus dados e acompanhe sua participação."
    >
      <template #actions>
        <RouterLink class="btn btn-primary-custom" to="/participante/agenda"
          >Ver minha agenda</RouterLink
        >
      </template>
    </DashboardPageHeader>

    <div class="alert alert-primary border-0" role="alert">
      Dados demonstrativos: o perfil real será carregado pelo endpoint do usuário autenticado.
    </div>

    <div class="row g-4">
      <div class="col-lg-8">
        <section class="card border-0 shadow-sm mb-4">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold mb-4">Dados pessoais</h2>
            <dl class="row mb-0">
              <dt class="col-sm-3 text-muted">Nome</dt>
              <dd class="col-sm-9 fw-semibold">{{ participante.nome }}</dd>
              <dt class="col-sm-3 text-muted">E-mail</dt>
              <dd class="col-sm-9 fw-semibold">{{ participante.email }}</dd>
            </dl>
            <button class="btn btn-outline-primary-custom" type="button" disabled>
              Editar após integração
            </button>
          </div>
        </section>

        <section class="card border-0 shadow-sm">
          <div class="card-body p-4">
            <MediaUploadField
              id="foto-perfil"
              label="Foto de perfil"
              finalidade="FOTO_PERFIL"
              ajuda="JPEG, PNG ou WebP, até 2 MB. A fotografia é opcional."
              @selecionar="fotoSelecionada = $event"
            />
            <p v-if="fotoSelecionada" class="small text-muted mt-3 mb-0">
              O arquivo será enviado quando o endpoint de mídia estiver disponível.
            </p>
          </div>
        </section>
      </div>

      <div class="col-lg-4">
        <section class="card border-0 shadow-sm">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold mb-3">Resumo da participação</h2>
            <div class="d-flex justify-content-between border-bottom py-2">
              <span>Eventos inscritos</span>
              <strong class="text-primary-custom">1</strong>
            </div>
            <div class="d-flex justify-content-between border-bottom py-2">
              <span>Atividades na agenda</span>
              <strong class="text-primary-custom">2</strong>
            </div>
            <div class="d-flex justify-content-between py-2">
              <span>Presenças confirmadas</span>
              <strong class="text-primary-custom">0</strong>
            </div>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
