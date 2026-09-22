<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import QRCode from 'qrcode'
import { listarMeusEventos } from '@/features/events/services/eventoService'
import type { EventoResumo } from '@/features/events/types/evento'
import {
  criarAtividade,
  gerarChamada,
  listarAtividades,
  type AtividadePresenca,
  type ChamadaGerada,
} from '@/features/attendance/services/presencaService'
import { linkPresenca } from '@/features/attendance/utils/codigoPresenca'
import { ApiError } from '@/shared/services/httpClient'
const eventos = ref<EventoResumo[]>([])
const eventoId = ref('')
const atividades = ref<AtividadePresenca[]>([])
const atividadeId = ref('')
const chamada = ref<ChamadaGerada | null>(null)
const imagem = ref('')
const erro = ref('')
const ocupado = ref(false)
const carregandoAtividades = ref(false)
const mensagem = ref('')
const agora = ref(Date.now())
let consulta = 0
const temporizador = window.setInterval(() => {
  agora.value = Date.now()
}, 1000)
onUnmounted(() => {
  window.clearInterval(temporizador)
  consulta++
})
const restante = computed(() =>
  chamada.value
    ? Math.max(0, Math.ceil((Date.parse(chamada.value.expiraEm) - agora.value) / 1000))
    : 0,
)
const base =
  import.meta.env.VITE_PUBLIC_APP_URL ||
  new URL(import.meta.env.BASE_URL, window.location.origin).toString()
const enderecoLocal = computed(() => {
  try {
    return ['localhost', '127.0.0.1', '[::1]'].includes(new URL(base).hostname)
  } catch {
    return true
  }
})
const novo = reactive({ titulo: '', descricao: '', dataInicio: '', dataFim: '', local: '' })
function falha(e: unknown): string {
  return e instanceof ApiError
    ? e.message
    : 'Não foi possível concluir a operação. Atualize e tente novamente.'
}
async function carregar(): Promise<void> {
  erro.value = ''
  try {
    eventos.value = (await listarMeusEventos()).filter((e) => e.estado !== 'ENCERRADO')
  } catch (e) {
    erro.value = falha(e)
  }
}
onMounted(carregar)
watch(eventoId, async (id) => {
  const atual = ++consulta
  atividades.value = []
  atividadeId.value = ''
  chamada.value = null
  imagem.value = ''
  erro.value = ''
  mensagem.value = ''
  carregandoAtividades.value = false
  if (!id) return
  carregandoAtividades.value = true
  try {
    const lista = await listarAtividades(id)
    if (atual === consulta) atividades.value = lista
  } catch (e) {
    if (atual === consulta) erro.value = falha(e)
  } finally {
    if (atual === consulta) carregandoAtividades.value = false
  }
})
watch(atividadeId, () => {
  chamada.value = null
  imagem.value = ''
})
async function adicionar(): Promise<void> {
  if (ocupado.value || carregandoAtividades.value || !eventoId.value) return
  erro.value = ''
  mensagem.value = ''
  if (
    !novo.titulo.trim() ||
    !novo.local.trim() ||
    !Number.isFinite(Date.parse(novo.dataInicio)) ||
    !Number.isFinite(Date.parse(novo.dataFim)) ||
    Date.parse(novo.dataFim) <= Date.parse(novo.dataInicio)
  ) {
    erro.value = 'Informe título, local e um término posterior ao início.'
    return
  }
  ocupado.value = true
  try {
    const atividade = await criarAtividade(eventoId.value, { ...novo })
    atividades.value.push(atividade)
    atividadeId.value = atividade.id
    Object.assign(novo, { titulo: '', descricao: '', dataInicio: '', dataFim: '', local: '' })
    mensagem.value = 'Atividade cadastrada.'
  } catch (e) {
    erro.value = falha(e)
  } finally {
    ocupado.value = false
  }
}
async function gerar(): Promise<void> {
  if (ocupado.value || !atividadeId.value) return
  ocupado.value = true
  erro.value = ''
  mensagem.value = ''
  chamada.value = null
  imagem.value = ''
  try {
    const resultado = await gerarChamada(atividadeId.value)
    chamada.value = resultado
    imagem.value = await QRCode.toDataURL(linkPresenca(base, resultado.codigo), {
      width: 320,
      margin: 4,
      errorCorrectionLevel: 'M',
    })
  } catch (e) {
    erro.value = chamada.value
      ? 'A chamada foi criada, mas não foi possível desenhar o QR. Utilize o código abaixo e confira o endereço configurado.'
      : falha(e)
  } finally {
    ocupado.value = false
  }
}
</script>
<template>
  <div class="container-fluid p-4 p-xl-5">
    <h1 class="h2 fw-bold">Chamada de presença</h1>
    <p class="text-muted">Escolha a atividade e exiba um código temporário aos participantes.</p>
    <div v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</div>
    <div v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</div>
    <div v-if="enderecoLocal" class="alert alert-info">
      Localhost não abre no celular. Para testar o QR, configure o endereço do webapp acessível pelo
      celular; a digitação funciona neste navegador.
    </div>
    <div class="row g-4">
      <div class="col-lg-5">
        <section class="card border-0 shadow-sm mb-4">
          <div class="card-body p-4">
            <label for="evento-presenca" class="form-label fw-semibold">Evento</label>
            <select
              id="evento-presenca"
              v-model="eventoId"
              class="form-select mb-3"
              :disabled="ocupado"
            >
              <option value="">Selecione um evento</option>
              <option v-for="e in eventos" :key="e.id" :value="e.id">
                {{ e.titulo }} — {{ e.estado }}
              </option>
            </select>
            <p v-if="!eventos.length" class="small text-muted">
              Nenhum evento disponível. Crie um evento ou atualize a lista.
            </p>
            <button
              class="btn btn-sm btn-outline-secondary mb-3"
              :disabled="ocupado"
              @click="carregar"
            >
              Atualizar eventos
            </button>
            <label for="atividade-presenca" class="form-label fw-semibold d-block">Atividade</label>
            <select
              id="atividade-presenca"
              v-model="atividadeId"
              class="form-select mb-3"
              :disabled="ocupado || carregandoAtividades"
            >
              <option value="">
                {{ carregandoAtividades ? 'Carregando...' : 'Selecione uma atividade' }}
              </option>
              <option v-for="a in atividades" :key="a.id" :value="a.id">{{ a.titulo }}</option>
            </select>
            <button
              class="btn btn-primary-custom w-100"
              :disabled="ocupado || !atividadeId"
              @click="gerar"
            >
              {{ ocupado ? 'Aguarde...' : 'Gerar nova chamada' }}
            </button>
            <p class="small text-muted mt-3 mb-0">
              O evento precisa estar publicado. Uma nova chamada invalida a anterior. O código vale
              por 5 minutos.
            </p>
          </div>
        </section>
        <details v-if="eventoId" class="card border-0 shadow-sm">
          <summary class="p-3 fw-semibold">Cadastrar atividade</summary>
          <form class="card-body" @submit.prevent="adicionar">
            <fieldset :disabled="ocupado || carregandoAtividades">
              <label for="atividade-titulo" class="form-label">Título</label>
              <input
                id="atividade-titulo"
                v-model="novo.titulo"
                class="form-control mb-2"
                maxlength="200"
                required
              />
              <label for="atividade-descricao" class="form-label">Descrição</label>
              <textarea
                id="atividade-descricao"
                v-model="novo.descricao"
                class="form-control mb-2"
                maxlength="10000"
              ></textarea>
              <label for="atividade-inicio" class="form-label">Início</label>
              <input
                id="atividade-inicio"
                v-model="novo.dataInicio"
                class="form-control mb-2"
                type="datetime-local"
                required
              />
              <label for="atividade-fim" class="form-label">Término</label>
              <input
                id="atividade-fim"
                v-model="novo.dataFim"
                class="form-control mb-2"
                type="datetime-local"
                required
              />
              <label for="atividade-local" class="form-label">Local</label>
              <input
                id="atividade-local"
                v-model="novo.local"
                class="form-control mb-3"
                maxlength="200"
                required
              />
              <p class="small text-muted">A atividade deve ocorrer dentro do período do evento.</p>
              <button class="btn btn-outline-primary w-100" type="submit">Salvar atividade</button>
            </fieldset>
          </form>
        </details>
      </div>
      <div class="col-lg-7">
        <section class="card border-0 shadow-sm">
          <div class="card-body text-center p-4 p-lg-5">
            <template v-if="chamada">
              <h2 class="h4">{{ chamada.atividade }}</h2>
              <template v-if="restante > 0">
                <img
                  v-if="imagem"
                  :src="imagem"
                  alt="QR Code para abrir a confirmação de presença"
                  class="img-fluid"
                  width="320"
                  height="320"
                />
                <p class="small text-muted mb-1">
                  Ou digite este código na página Confirmar presença:
                </p>
                <p class="fs-2 fw-bold font-monospace text-primary">
                  {{ chamada.codigo.match(/.{1,4}/g)?.join('-') }}
                </p>
                <p role="status">
                  Expira em {{ Math.floor(restante / 60) }}min {{ restante % 60 }}s
                </p>
              </template>
              <p v-else class="alert alert-warning" role="status">
                Chamada expirada. Gere outra para continuar.
              </p>
            </template>
            <p v-else class="text-muted my-5">
              O QR Code aparecerá aqui após a geração da chamada.
            </p>
            <p class="small text-muted mb-0">
              O participante deve entrar na própria conta e confirmar. Um mesmo código pode atender
              vários inscritos, mas cada pessoa recebe somente uma presença na atividade.
            </p>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
