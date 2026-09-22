<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ApiError } from '@/shared/services/httpClient'
import { confirmarPresenca, type Confirmacao } from '@/features/attendance/services/presencaService'
import { codigoValido, normalizarCodigo } from '@/features/attendance/utils/codigoPresenca'
const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const codigo = ref('')
const origem = ref<'QR' | 'CODIGO'>('CODIGO')
const resultado = ref<Confirmacao | null>(null)
const erro = ref('')
const enviando = ref(false)
const pronto = ref(false)
const chave = 'events-presenca-pendente'
function editarCodigo(): void {
  origem.value = 'CODIGO'
  resultado.value = null
}
function guardar(): void {
  try {
    sessionStorage.setItem(
      chave,
      JSON.stringify({
        codigo: normalizarCodigo(codigo.value),
        origem: origem.value,
        ate: Date.now() + 300000,
      }),
    )
  } catch {
    /* A digitação continua disponível sem armazenamento. */
  }
}
onMounted(async () => {
  const recebido = new URLSearchParams(route.hash.slice(1)).get('codigo')
  if (recebido && codigoValido(recebido)) {
    codigo.value = normalizarCodigo(recebido)
    origem.value = 'QR'
    guardar()
  } else if (!route.hash) {
    try {
      const salvo: unknown = JSON.parse(sessionStorage.getItem(chave) ?? 'null')
      if (
        salvo &&
        typeof salvo === 'object' &&
        'codigo' in salvo &&
        typeof salvo.codigo === 'string' &&
        'ate' in salvo &&
        typeof salvo.ate === 'number' &&
        salvo.ate > Date.now() &&
        codigoValido(salvo.codigo)
      ) {
        codigo.value = salvo.codigo
        origem.value = 'origem' in salvo && salvo.origem === 'QR' ? 'QR' : 'CODIGO'
      }
    } catch {
      /* Conteúdo inválido é ignorado. */
    }
  } else {
    erro.value = 'O link contém um código inválido. Digite o código atual da chamada.'
    try {
      sessionStorage.removeItem(chave)
    } catch {
      /* Não reutilizar outro código. */
    }
  }
  // O redirecionamento de login não transporta o código na query.
  if (route.hash) await router.replace({ name: 'confirm-attendance' })
  pronto.value = true
})
async function confirmar(): Promise<void> {
  if (enviando.value || !auth.autenticado || !pronto.value) return
  erro.value = ''
  resultado.value = null
  if (!codigoValido(codigo.value)) {
    erro.value = 'Informe os 12 caracteres do código exibido pelo organizador.'
    return
  }
  guardar()
  enviando.value = true
  try {
    resultado.value = await confirmarPresenca(normalizarCodigo(codigo.value), origem.value)
    try {
      sessionStorage.removeItem(chave)
    } catch {
      /* Não interfere na confirmação. */
    }
  } catch (e) {
    erro.value =
      e instanceof ApiError
        ? e.message
        : 'Não foi possível confirmar. Tente novamente; sua presença não será duplicada.'
  } finally {
    enviando.value = false
  }
}
</script>
<template>
  <div class="container py-5">
    <div class="card border-0 shadow-sm mx-auto" style="max-width: 560px">
      <div class="card-body p-4 p-md-5">
        <h1 class="h3 fw-bold">Confirmar presença</h1>
        <p class="text-muted">
          Escaneie o QR com a câmera do celular ou digite o código exibido pelo organizador. É
          necessário estar inscrito no evento.
        </p>
        <div v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</div>
        <div v-if="resultado" class="alert alert-success" role="status">
          {{
            resultado.jaRegistrada ? 'Sua presença já estava registrada.' : 'Presença registrada!'
          }}
          <strong class="d-block">{{ resultado.atividade }}</strong>
          <span>{{ new Date(resultado.registradaEm).toLocaleString('pt-BR') }}</span>
        </div>
        <form @submit.prevent="confirmar">
          <label class="form-label" for="codigo-presenca">Código da chamada</label>
          <input
            id="codigo-presenca"
            v-model="codigo"
            class="form-control form-control-lg font-monospace mb-3"
            maxlength="16"
            autocomplete="off"
            autocapitalize="characters"
            spellcheck="false"
            :disabled="enviando"
            @input="editarCodigo"
          />
          <button
            v-if="auth.autenticado"
            class="btn btn-primary-custom w-100"
            :disabled="enviando || !pronto"
            type="submit"
          >
            {{ enviando ? 'Confirmando...' : 'Confirmar minha presença' }}
          </button>
          <RouterLink
            v-else
            class="btn btn-primary-custom w-100"
            :to="{ name: 'login', query: { redirect: '/presenca' } }"
            @click="guardar"
            >Entrar para confirmar</RouterLink
          >
        </form>
        <p class="small text-muted mt-3 mb-0">
          Abrir o QR não marca presença automaticamente. Nunca digite sua senha no campo do código.
        </p>
      </div>
    </div>
  </div>
</template>
