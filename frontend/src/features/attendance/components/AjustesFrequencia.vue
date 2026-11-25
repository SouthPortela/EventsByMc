<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { listarAjustes, ajustarFrequencia, type AjusteFrequencia } from '../services/frequenciaService'
import { listarRelatorioAtividades, type RelatorioAtividade } from '@/features/reports/services/relatoriosService'
import { ApiError } from '@/shared/services/httpClient'
const props = defineProps<{ atividadeId: string; eventoId: string }>()
const registros = ref<AjusteFrequencia[]>([])
const participantes = ref<RelatorioAtividade[]>([])
const dados = reactive({ usuarioId: '', confirmada: true, motivo: '' })
const ocupado = ref(false)
const carregando = ref(false)
const erro = ref('')
const mensagem = ref('')
let consulta = 0
let pendente: { atividade: string; corpo: string; id: string } | null = null
const nome = (id: string): string => participantes.value.find(p => p.usuarioId === id)?.nome ?? 'Participante'
const ativos = computed(() => participantes.value.filter(p => p.inscricaoEstado === 'ATIVA'))
async function carregar(): Promise<void> {
  const atual = ++consulta
  const atividade = props.atividadeId
  const evento = props.eventoId
  registros.value = []; participantes.value = []; erro.value = ''; mensagem.value = ''; dados.usuarioId = ''; pendente = null
  if (!atividade || !evento) return
  carregando.value = true
  try {
    const [ajustes, linhas] = await Promise.all([listarAjustes(atividade), listarRelatorioAtividades(evento)])
    if (atual === consulta) { registros.value = ajustes; participantes.value = linhas.filter(l => l.atividadeId === atividade) }
  } catch (e) { if (atual === consulta) erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar a frequência.' }
  finally { if (atual === consulta) carregando.value = false }
}
watch(() => [props.atividadeId, props.eventoId], carregar, { immediate: true })
async function salvar(): Promise<void> {
  if (ocupado.value || !dados.usuarioId || !props.atividadeId) return
  const atividade = props.atividadeId
  const corpo = JSON.stringify({ usuarioId: dados.usuarioId, confirmada: dados.confirmada, motivo: dados.motivo.trim() })
  if (!pendente || pendente.atividade !== atividade || pendente.corpo !== corpo) pendente = { atividade, corpo, id: crypto.randomUUID() }
  ocupado.value = true; erro.value = ''; mensagem.value = ''
  try {
    await ajustarFrequencia(atividade, { id: pendente.id, usuarioId: dados.usuarioId, confirmada: dados.confirmada, motivo: dados.motivo.trim() })
    if (atividade !== props.atividadeId) return
    await carregar()
    dados.motivo = ''
    mensagem.value = 'Ajuste registrado. O histórico anterior foi preservado e os relatórios já refletem a situação atual.'
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível confirmar. Tente novamente mantendo os dados para evitar duplicidade.' }
  finally { ocupado.value = false }
}
</script>
<template>
  <section v-if="atividadeId" class="card card-body mt-4">
    <h2 class="h5">Lançamento alternativo e retificação</h2>
    <p class="small text-muted">Confirme ou desconsidere a presença nesta atividade, em qualquer política. O motivo e o responsável ficam registrados sem apagar marcações anteriores.</p>
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p><p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <p v-if="carregando" role="status">Carregando...</p>
    <form @submit.prevent="salvar"><fieldset :disabled="ocupado || carregando">
      <label for="ajuste-participante" class="form-label">Participante</label>
      <select id="ajuste-participante" v-model="dados.usuarioId" class="form-select mb-2" required><option value="">Selecione</option><option v-for="p in ativos" :key="p.usuarioId" :value="p.usuarioId">{{ p.nome }} — {{ p.confirmada ? 'Presença confirmada' : 'Presença não confirmada' }}</option></select>
      <label for="ajuste-situacao" class="form-label">Nova situação</label>
      <select id="ajuste-situacao" v-model="dados.confirmada" class="form-select mb-2"><option :value="true">Confirmar presença</option><option :value="false">Desconsiderar presença</option></select>
      <label for="ajuste-motivo" class="form-label">Motivo obrigatório</label>
      <textarea id="ajuste-motivo" v-model="dados.motivo" class="form-control mb-2" required minlength="5" maxlength="1000" />
      <button class="btn btn-primary-custom" type="submit">{{ ocupado ? 'Registrando...' : 'Registrar ajuste' }}</button>
    </fieldset></form>
    <h3 class="h6 mt-4">Histórico de ajustes</h3>
    <p v-if="!registros.length" class="text-muted">Nenhum ajuste registrado.</p>
    <ul class="list-group list-group-flush"><li v-for="a in registros" :key="a.id" class="list-group-item text-break">
      {{ new Date(a.registradaEm).toLocaleString('pt-BR') }} · {{ nome(a.usuarioId) }} · {{ a.confirmada ? 'Presença confirmada' : 'Presença desconsiderada' }} — {{ a.motivo }}
      <span class="small d-block">Responsável: {{ a.responsavelId }}</span>
    </li></ul>
  </section>
</template>
