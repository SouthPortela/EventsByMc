import { onMounted, ref } from 'vue'
import { ApiError } from '@/shared/services/httpClient'
import { consultarParticipacao, type Participacao } from '../services/participacaoService'

export function useParticipacao() {
  const dados = ref<Participacao | null>(null)
  const carregando = ref(true)
  const erro = ref('')

  async function recarregar(): Promise<void> {
    carregando.value = true
    erro.value = ''
    try {
      dados.value = await consultarParticipacao()
    } catch (e) {
      dados.value = null
      erro.value = e instanceof ApiError ? e.message : 'Não foi possível carregar sua participação.'
    } finally {
      carregando.value = false
    }
  }

  onMounted(recarregar)
  return { dados, carregando, erro, recarregar }
}
