<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarAuditoria, exportarAuditoria, type RegistroAdmin } from '@/features/admin/services/adminService'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { ApiError } from '@/shared/services/httpClient'

const registros = ref<RegistroAdmin[]>([])
const busca = ref('')
const ocupado = ref(false)
const erro = ref('')
const resultados = computed(() => registros.value.filter(r => normalizarTexto(`${r.acao} ${r.responsavelId} ${r.recursoId} ${r.detalhe}`).includes(normalizarTexto(busca.value))))
async function executar(exportar = false): Promise<void> {
  if (ocupado.value) return
  ocupado.value = true
  erro.value = ''
  try { if (exportar) await exportarAuditoria(); else registros.value = await listarAuditoria() }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar ou exportar a auditoria.' }
  finally { ocupado.value = false }
}
onMounted(() => executar())
</script>
<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Segurança" title="Auditoria administrativa" description="Últimas 500 alterações de perfis, bloqueios e desbloqueios de eventos. O CSV exporta esses registros, sem o filtro da tela." />
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <div class="d-flex gap-2 mb-3">
      <input v-model="busca" type="search" class="form-control" aria-label="Filtrar auditoria" placeholder="Ação, UUID ou detalhe" />
      <button class="btn btn-outline-primary" :disabled="ocupado" @click="executar()">Atualizar</button>
      <button class="btn btn-primary" :disabled="ocupado" @click="executar(true)">Exportar CSV</button>
    </div>
    <p v-if="ocupado" role="status">Processando...</p>
    <div class="card table-responsive"><table class="table align-middle mb-0">
      <thead><tr><th>Data</th><th>Responsável</th><th>Ação / recurso</th><th>Detalhe</th></tr></thead>
      <tbody><tr v-for="registro in resultados" :key="registro.id">
        <td>{{ new Date(registro.registradoEm).toLocaleString('pt-BR') }}</td>
        <td class="small text-break">{{ registro.responsavelId }}</td>
        <td>{{ registro.acao }}<div class="small text-break">{{ registro.recursoId }}</div></td>
        <td class="text-break" style="white-space: pre-wrap">{{ registro.detalhe }}</td>
      </tr><tr v-if="!resultados.length && !ocupado"><td colspan="4">Nenhum registro encontrado.</td></tr></tbody>
    </table></div>
  </div>
</template>
