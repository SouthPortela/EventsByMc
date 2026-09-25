<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarModeracoes } from '@/features/admin/services/administracaoService'
import type { RegistroModeracao } from '@/features/admin/services/administracaoService'
import { ApiError } from '@/shared/services/httpClient'

const registros = ref<RegistroModeracao[]>([])
const carregando = ref(true)
const erro = ref('')
const data = (valor: string): string => new Date(valor).toLocaleString('pt-BR')

onMounted(async () => {
  try {
    registros.value = await listarModeracoes()
  } catch (falha) {
    erro.value = falha instanceof ApiError ? falha.message : 'Não foi possível consultar a auditoria.'
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Segurança" title="Auditoria de moderação"
      description="Histórico das suspensões, restaurações e exclusões de eventos." />
    <p v-if="erro" class="alert alert-danger" role="alert">{{ erro }}</p>
    <p v-if="carregando" role="status">Carregando auditoria...</p>
    <section v-else class="card border-0 shadow-sm"><div class="table-responsive"><table class="table align-middle mb-0">
      <thead class="table-light"><tr><th class="ps-4">Data</th><th>Administrador</th><th>Evento</th><th>Alteração</th><th class="pe-4">Motivo</th></tr></thead>
      <tbody>
        <tr v-for="registro in registros" :key="registro.id">
          <td class="ps-4">{{ data(registro.criadoEm) }}</td>
          <td>{{ registro.administradorNome }}</td>
          <td><RouterLink :to="`/admin/eventos/${registro.eventoId}`">{{ registro.eventoId }}</RouterLink></td>
          <td>{{ registro.estadoAnterior }} → {{ registro.estadoNovo }}</td>
          <td class="pe-4">{{ registro.motivo }}</td>
        </tr>
        <tr v-if="!registros.length"><td colspan="5" class="p-4 text-center text-muted">Nenhuma ação de moderação registrada.</td></tr>
      </tbody>
    </table></div></section>
  </div>
</template>
