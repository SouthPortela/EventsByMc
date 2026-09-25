<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarUsuariosAdministrativos } from '@/features/admin/services/administracaoService'
import type { UsuarioAdministrativo } from '@/features/admin/services/administracaoService'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { ApiError } from '@/shared/services/httpClient'

const busca = ref('')
const usuarios = ref<UsuarioAdministrativo[]>([])
const carregando = ref(true)
const erro = ref('')
const resultados = computed(() => {
  const termo = normalizarTexto(busca.value)
  return !termo ? usuarios.value : usuarios.value.filter((usuario) =>
    normalizarTexto(usuario.nome).includes(termo))
})

onMounted(async () => {
  try {
    usuarios.value = await listarUsuariosAdministrativos()
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível consultar usuários.'
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Controle de acesso" title="Usuários da plataforma"
      description="Consulte contas e perfis sem expor endereços de e-mail completos." />
    <p v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</p>
    <p v-if="carregando" role="status">Carregando usuários...</p>
    <section v-else class="card border-0 shadow-sm">
      <div class="card-header bg-white p-4">
        <label for="busca-usuario" class="form-label">Buscar por nome</label>
        <input id="busca-usuario" v-model="busca" class="form-control" type="search" />
      </div>
      <div class="table-responsive"><table class="table align-middle mb-0">
        <thead class="table-light"><tr><th class="ps-4">Usuário</th><th>Perfis</th></tr></thead>
        <tbody>
          <tr v-for="usuario in resultados" :key="usuario.id"><td class="ps-4 py-3">
            <strong class="d-block">{{ usuario.nome }}</strong>
            <small class="text-muted">{{ usuario.emailMascarado }} · {{ usuario.id }}</small>
          </td><td><span v-for="perfil in usuario.perfis" :key="perfil" class="badge text-bg-primary me-1">{{ perfil }}</span></td></tr>
          <tr v-if="!resultados.length"><td colspan="2" class="p-4 text-center text-muted">Nenhum usuário encontrado.</td></tr>
        </tbody>
      </table></div>
    </section>
  </div>
</template>
