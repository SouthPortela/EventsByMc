<script setup lang="ts">
import { ref, watch } from 'vue'
import { consultarHistoricoEvento, type HistoricoEvento } from '../services/eventoService'
import { ApiError } from '@/shared/services/httpClient'
const props = defineProps<{ eventoId: string }>()
const registros = ref<HistoricoEvento[]>([])
const erro = ref('')
let consulta = 0
function descrever(registro: HistoricoEvento): string {
  try {
    const antes = JSON.parse(registro.antes) as Record<string, unknown>
    const depois = JSON.parse(registro.depois) as Record<string, unknown>
    const campos: Record<string, string> = { titulo: 'Título', descricao: 'Descrição', inicio: 'Início', fim: 'Fim', local: 'Local', categoria: 'Categoria', estado: 'Estado', frequencia_minima_percentual: 'Frequência mínima', limite_inscritos: 'Limite de inscritos', inscricoes_abertas: 'Inscrições abertas', permitir_cancelamento: 'Cancelamento', inscricoes_inicio: 'Abertura das inscrições', inscricoes_fim: 'Fim das inscrições', motivo_bloqueio: 'Motivo do bloqueio' }
    return Object.entries(campos).filter(([chave]) => JSON.stringify(antes[chave]) !== JSON.stringify(depois[chave]))
      .map(([chave, nome]) => nome + ': ' + String(antes[chave] ?? '—') + ' → ' + String(depois[chave] ?? '—')).join('; ') || 'Atualização do evento'
  } catch { return 'Atualização registrada' }
}
watch(() => props.eventoId, async id => {
  const atual = ++consulta
  registros.value = []; erro.value = ''
  if (!id) return
  try { const dados = await consultarHistoricoEvento(id); if (consulta === atual) registros.value = dados }
  catch (e) { if (consulta === atual) erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar o histórico.' }
}, { immediate: true })
</script>
<template>
  <section class="card card-body mt-4">
    <h2 class="h5">Histórico do evento</h2>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-else-if="!registros.length" class="text-muted">Nenhuma alteração registrada desde a atualização do sistema.</p>
    <ul v-else class="list-group list-group-flush"><li v-for="registro in registros" :key="registro.id" class="list-group-item text-break">
      <time>{{ new Date(registro.registradaEm).toLocaleString('pt-BR') }}</time>: {{ descrever(registro) }}
    </li></ul>
  </section>
</template>
