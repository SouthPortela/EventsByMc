<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarEventosAdmin, bloquearEvento, desbloquearEvento, type EventoAdmin } from '@/features/admin/services/adminService'
import { alterarEstadoEvento } from '@/features/events/services/eventoService'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { ApiError } from '@/shared/services/httpClient'

const eventos = ref<EventoAdmin[]>([])
const busca = ref('')
const estado = ref('')
const carregando = ref(false)
const ocupado = ref(false)
const erro = ref('')
const mensagem = ref('')
const alvo = ref<EventoAdmin | null>(null)
const motivo = ref('')
const resultados = computed(() => eventos.value.filter(e => (!estado.value || e.evento.estado === estado.value) &&
  normalizarTexto(`${e.evento.titulo} ${e.organizadorNome} ${e.evento.id}`).includes(normalizarTexto(busca.value))))
async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try { eventos.value = await listarEventosAdmin() }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar os eventos.' }
  finally { carregando.value = false }
}
function preparar(item: EventoAdmin): void { alvo.value = item; motivo.value = ''; mensagem.value = '' }
async function executar(item: EventoAdmin, acao: 'bloquear' | 'desbloquear' | 'publicacao' | 'encerramento'): Promise<void> {
  if (ocupado.value || (acao === 'bloquear' && !motivo.value.trim())) return
  if (acao !== 'bloquear' && !window.confirm(`Confirma ${acao === 'desbloquear' ? 'o desbloqueio, restaurando o estado anterior' : acao === 'publicacao' ? 'a publicação' : 'o encerramento'} do evento?`)) return
  ocupado.value = true
  erro.value = ''
  mensagem.value = ''
  try {
    if (acao === 'bloquear') await bloquearEvento(item.evento.id, motivo.value)
    else if (acao === 'desbloquear') await desbloquearEvento(item.evento.id)
    else await alterarEstadoEvento(item.evento.id, acao)
    alvo.value = null
    await carregar()
    mensagem.value = 'Operação concluída.'
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível confirmar a operação. Atualize a lista.' }
  finally { ocupado.value = false }
}
onMounted(carregar)
</script>
<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Administração" title="Eventos da plataforma" description="Gerencie os eventos de todos os organizadores." />
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <form v-if="alvo" class="card card-body mb-4" @submit.prevent="executar(alvo, 'bloquear')">
      <h2 class="h5">Bloquear: {{ alvo.evento.titulo }}</h2>
      <label for="motivo-bloqueio" class="form-label">Motivo do bloqueio (visível para o organizador)</label>
      <textarea id="motivo-bloqueio" v-model="motivo" class="form-control mb-3" rows="3" required maxlength="2000" :disabled="ocupado"></textarea>
      <div class="d-flex gap-2"><button class="btn btn-danger" :disabled="ocupado || !motivo.trim()">Confirmar bloqueio</button><button type="button" class="btn btn-outline-secondary" :disabled="ocupado" @click="alvo = null">Cancelar</button></div>
    </form>
    <div class="d-flex flex-wrap gap-2 mb-3">
      <input v-model="busca" class="form-control" type="search" aria-label="Buscar evento" placeholder="Evento, organizador ou UUID" />
      <select v-model="estado" class="form-select w-auto" aria-label="Filtrar estado"><option value="">Todos os estados</option><option v-for="s in ['RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'BLOQUEADO']" :key="s">{{ s }}</option></select>
      <button class="btn btn-outline-primary" :disabled="carregando || ocupado" @click="carregar">Atualizar</button>
      <RouterLink class="btn btn-primary" to="/organizador/eventos/novo">Criar evento</RouterLink>
    </div>
    <p v-if="carregando" role="status">Carregando eventos...</p>
    <div v-else class="card table-responsive"><table class="table align-middle mb-0">
      <thead><tr><th>Evento / organizador</th><th>Estado</th><th>Ações</th></tr></thead>
      <tbody><tr v-for="item in resultados" :key="item.evento.id">
        <td><strong>{{ item.evento.titulo }}</strong><div>{{ item.organizadorNome }}</div><small class="text-muted text-break">{{ item.evento.id }}</small>
          <p v-if="item.evento.motivoBloqueio" class="alert alert-danger mt-2 mb-0 text-break"><strong>Motivo do bloqueio:</strong> {{ item.evento.motivoBloqueio }}</p></td>
        <td><span class="badge" :class="item.evento.estado === 'BLOQUEADO' ? 'text-bg-danger' : 'text-bg-secondary'">{{ item.evento.estado }}</span></td>
        <td><div class="d-flex flex-wrap gap-2">
          <template v-if="item.evento.estado !== 'BLOQUEADO'">
            <RouterLink class="btn btn-sm btn-outline-secondary" :to="`/organizador/eventos/${item.evento.id}/programacao`">Programação</RouterLink>
            <RouterLink class="btn btn-sm btn-outline-secondary" :to="`/organizador/eventos/${item.evento.id}/questionario`">Questionário</RouterLink>
            <button v-if="item.evento.estado === 'RASCUNHO'" class="btn btn-sm btn-primary" :disabled="ocupado" @click="executar(item, 'publicacao')">Publicar</button>
            <button v-if="item.evento.estado === 'PUBLICADO'" class="btn btn-sm btn-outline-secondary" :disabled="ocupado" @click="executar(item, 'encerramento')">Encerrar</button>
            <button class="btn btn-sm btn-danger" :disabled="ocupado" @click="preparar(item)">Bloquear</button>
          </template>
          <button v-else class="btn btn-sm btn-outline-success" :disabled="ocupado" @click="executar(item, 'desbloquear')">Desbloquear</button>
        </div></td>
      </tr><tr v-if="!resultados.length"><td colspan="3">Nenhum evento encontrado.</td></tr></tbody>
    </table></div>
  </div>
</template>
