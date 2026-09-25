<script setup lang="ts">
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { useParticipacao } from '@/features/attendance/composables/useParticipacao'
import { formatarData } from '@/features/events/utils/formatarData'
import { cancelarInscricao } from '@/features/attendance/services/presencaService'
import { ApiError } from '@/shared/services/httpClient'
import { baixarMeuCertificado, enviarMeuCertificado } from '@/features/certificates/services/certificadoService'

const { dados, carregando, erro, recarregar } = useParticipacao()
const cancelando = ref('')
const erroCancelamento = ref('')
const emitindo = ref('')
const mensagemCertificado = ref('')
async function certificado(eventoId: string, enviar: boolean): Promise<void> {
  if (emitindo.value) return
  emitindo.value = eventoId
  erroCancelamento.value = ''
  mensagemCertificado.value = ''
  try {
    if (enviar) { await enviarMeuCertificado(eventoId); mensagemCertificado.value = 'Certificado enviado ao e-mail cadastrado.' }
    else await baixarMeuCertificado(eventoId)
  } catch (e) { erroCancelamento.value = e instanceof ApiError ? e.message : 'Não foi possível emitir o certificado.' }
  finally { emitindo.value = '' }
}
async function cancelar(eventoId: string): Promise<void> {
  if (cancelando.value || !window.confirm('Cancelar sua inscrição? As atividades desse evento sairão da sua agenda.')) return
  cancelando.value = eventoId
  erroCancelamento.value = ''
  try {
    await cancelarInscricao(eventoId)
    await recarregar()
  } catch (e) {
    erroCancelamento.value = e instanceof ApiError ? e.message : 'Não foi possível cancelar a inscrição.'
  } finally {
    cancelando.value = ''
  }
}
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Participação"
      title="Minhas inscrições"
      description="Consulte os eventos em que você se inscreveu e a situação de cada inscrição."
    >
      <template #actions
        ><RouterLink class="btn btn-primary-custom" to="/">Encontrar eventos</RouterLink></template
      >
    </DashboardPageHeader>

    <div v-if="erroCancelamento" class="alert alert-warning" role="alert">{{ erroCancelamento }}</div>
    <div v-if="mensagemCertificado" class="alert alert-success" role="status">{{ mensagemCertificado }}</div>

    <p v-if="carregando" role="status">Carregando inscrições...</p>
    <div v-else-if="erro" class="alert alert-warning" role="alert">
      {{ erro }}
      <button class="btn btn-outline-primary ms-3" type="button" @click="recarregar">
        Tentar novamente
      </button>
    </div>
    <div v-else-if="!dados?.inscricoes.length" class="card border-0 shadow-sm">
      <div class="card-body p-4">
        Você ainda não se inscreveu em nenhum evento.
        <RouterLink to="/">Explorar eventos</RouterLink>
      </div>
    </div>
    <div v-else class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Evento</th>
              <th scope="col">Data e local</th>
              <th scope="col">Situação</th>
              <th class="text-end pe-4" scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inscricao in dados.inscricoes" :key="inscricao.id">
              <td class="ps-4 py-3">
                <span class="fw-bold d-block">{{ inscricao.eventoTitulo }}</span
                ><span class="small text-secondary">Inscrição {{ inscricao.id.slice(0, 8) }}</span>
              </td>
              <td>
                <span class="small fw-semibold d-block">{{
                  formatarData(inscricao.eventoInicio)
                }}</span
                ><span class="small text-secondary d-flex align-items-center gap-1"
                  ><AppIcon name="map-pin" :size="14" />
                  {{ inscricao.eventoLocal ?? 'Local a divulgar' }}</span
                >
              </td>
              <td>
                <span
                  class="badge"
                  :class="inscricao.estado === 'ATIVA' ? 'text-bg-success' : 'text-bg-secondary'"
                  >{{ inscricao.estado === 'ATIVA' ? 'Ativa' : 'Cancelada' }}</span
                >
              </td>
              <td class="text-end pe-4">
                <RouterLink
                  v-if="inscricao.eventoEstado === 'PUBLICADO'"
                  class="btn btn-sm btn-outline-primary-custom"
                  :to="`/eventos/${inscricao.eventoId}`"
                  >Detalhes</RouterLink
                >
                <span v-else class="small text-secondary">{{
                  inscricao.eventoEstado === 'ENCERRADO' ? 'Evento encerrado'
                    : inscricao.eventoEstado === 'SUSPENSO' ? 'Evento suspenso'
                    : inscricao.eventoEstado === 'EXCLUIDO' ? 'Evento excluído' : 'Fora do catálogo'
                }}</span>
                <button
                  v-if="inscricao.estado === 'ATIVA'"
                  class="btn btn-sm btn-outline-danger ms-2"
                  type="button"
                  :disabled="!!cancelando"
                  @click="cancelar(inscricao.eventoId)"
                >Cancelar inscrição</button>
                <template v-if="inscricao.estado === 'ATIVA' && inscricao.eventoEstado === 'ENCERRADO'">
                  <RouterLink class="btn btn-sm btn-outline-primary ms-2" :to="`/participante/avaliacoes/${inscricao.eventoId}`">Avaliar</RouterLink>
                  <button class="btn btn-sm btn-outline-primary ms-2" :disabled="!!emitindo" @click="certificado(inscricao.eventoId, false)">Baixar certificado</button>
                  <button class="btn btn-sm btn-outline-primary ms-2" :disabled="!!emitindo" @click="certificado(inscricao.eventoId, true)">Enviar por e-mail</button>
                </template>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
