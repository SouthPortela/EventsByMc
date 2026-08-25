<script setup lang="ts">
import { computed, ref } from 'vue'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'

interface UsuarioAdmin {
  id: number
  nome: string
  emailMascarado: string
  perfil: 'PARTICIPANTE' | 'ORGANIZADOR' | 'ADMINISTRADOR'
  status: 'ATIVO' | 'PENDENTE' | 'BLOQUEADO'
}
const busca = ref('')
const usuarios = ref<UsuarioAdmin[]>([
  {
    id: 201,
    nome: 'Ana Martins',
    emailMascarado: 'a***@exemplo.com',
    perfil: 'PARTICIPANTE',
    status: 'ATIVO',
  },
  {
    id: 202,
    nome: 'Carlos Souza',
    emailMascarado: 'c***@exemplo.com',
    perfil: 'ORGANIZADOR',
    status: 'ATIVO',
  },
  {
    id: 203,
    nome: 'Beatriz Lima',
    emailMascarado: 'b***@exemplo.com',
    perfil: 'ORGANIZADOR',
    status: 'PENDENTE',
  },
  {
    id: 204,
    nome: 'Conta suspensa',
    emailMascarado: 'u***@exemplo.com',
    perfil: 'PARTICIPANTE',
    status: 'BLOQUEADO',
  },
])
const resultados = computed(() => {
  const termo = normalizarTexto(busca.value)
  return !termo
    ? usuarios.value
    : usuarios.value.filter((usuario) => normalizarTexto(usuario.nome).includes(termo))
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Controle de acesso"
      title="Gestão de usuários"
      description="Revise perfis e estados sem expor informações pessoais desnecessárias."
    />
    <section class="card border-0 shadow-sm">
      <div class="card-header bg-white p-4">
        <div class="input-group search-input-group col-lg-5">
          <span class="input-group-text"><AppIcon name="search" :size="17" /></span>
          <input v-model="busca" class="form-control" type="search" placeholder="Buscar por nome" />
        </div>
      </div>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Usuário</th>
              <th scope="col">Perfil</th>
              <th scope="col">Status</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="usuario in resultados" :key="usuario.id">
              <td class="ps-4 py-3">
                <span class="fw-bold d-block">{{ usuario.nome }}</span
                ><span class="small text-secondary"
                  >{{ usuario.emailMascarado }} · #{{ usuario.id }}</span
                >
              </td>
              <td>
                <span class="badge text-bg-primary-subtle text-primary-custom">{{
                  usuario.perfil.toLocaleLowerCase('pt-BR')
                }}</span>
              </td>
              <td>
                <span
                  class="badge"
                  :class="{
                    'text-bg-success': usuario.status === 'ATIVO',
                    'text-bg-warning': usuario.status === 'PENDENTE',
                    'text-bg-secondary': usuario.status === 'BLOQUEADO',
                  }"
                  >{{ usuario.status.toLocaleLowerCase('pt-BR') }}</span
                >
              </td>
              <td class="text-end pe-4">
                <button class="btn btn-sm btn-outline-primary-custom" type="button">Revisar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
