<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import {
  consultarPolitica, salvarPolitica, listarTrilhas, criarTrilha, listarEspacos, criarEspaco,
  listarPessoas, criarPessoa, listarProgramacaoGestao, criarAtividadeProgramacao, vincularPessoa,
  type PoliticaEvento, type Trilha, type Espaco, type PessoaEvento,
  type AtividadeProgramacao, type NovaAtividadeProgramacao,
} from '@/features/events/services/programacaoService'
import { formatarData } from '@/features/events/utils/formatarData'
import { ApiError } from '@/shared/services/httpClient'
import { baixarDeclaracao, enviarDeclaracao } from '@/features/certificates/services/certificadoService'

const route = useRoute()
const eventoId = String(route.params.id ?? '')
const trilhas = ref<Trilha[]>([])
const espacos = ref<Espaco[]>([])
const pessoas = ref<PessoaEvento[]>([])
const atividades = ref<AtividadeProgramacao[]>([])
const carregando = ref(true)
const salvando = ref(false)
const emitindoDeclaracao = ref(false)
const erro = ref('')
const mensagem = ref('')
const politica = reactive<PoliticaEvento>({ inscricoesAbertas: true, inscricoesInicio: null,
  inscricoesFim: null, limiteInscritos: null, permitirCancelamento: true,
  frequenciaMinimaPercentual: 75 })
const inicioInscricao = ref('')
const fimInscricao = ref('')
const trilhaNova = ref('')
const espacoNovo = ref('')
const espacoCapacidade = ref<number | null>(null)
const pessoaNome = ref('')
const pessoaEmail = ref('')
const vinculo = reactive({ atividadeId: '', pessoaId: '', papel: 'PALESTRANTE' })
const atividade = reactive<NovaAtividadeProgramacao>({ titulo: '', descricao: '', dataInicio: '',
  dataFim: '', local: '', capacidade: null, trilhaId: null, espacoId: null,
  tipo: 'PALESTRA', presencaObrigatoria: true, politicaFrequencia: 'CHECKIN_UNICO',
  permanenciaMinimaPercentual: 75 })

function paraLocal(iso: string | null): string {
  if (!iso) return ''
  const data = new Date(iso)
  return new Date(data.getTime() - data.getTimezoneOffset() * 60000).toISOString().slice(0, 16)
}
function mensagemFalha(e: unknown): string {
  return e instanceof ApiError ? e.message : 'Não foi possível salvar. Tente novamente.'
}
async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try {
    const [configuracao, listaTrilhas, listaEspacos, listaPessoas, listaAtividades] = await Promise.all([
      consultarPolitica(eventoId), listarTrilhas(eventoId), listarEspacos(eventoId),
      listarPessoas(eventoId), listarProgramacaoGestao(eventoId),
    ])
    Object.assign(politica, configuracao)
    inicioInscricao.value = paraLocal(configuracao.inscricoesInicio)
    fimInscricao.value = paraLocal(configuracao.inscricoesFim)
    trilhas.value = listaTrilhas
    espacos.value = listaEspacos
    pessoas.value = listaPessoas
    atividades.value = listaAtividades
  } catch (e) {
    erro.value = mensagemFalha(e)
  } finally {
    carregando.value = false
  }
}
async function executar(operacao: () => Promise<unknown>, sucesso: string): Promise<boolean> {
  if (salvando.value) return false
  salvando.value = true
  erro.value = ''
  mensagem.value = ''
  try {
    await operacao()
    mensagem.value = sucesso
    await carregar()
    return true
  } catch (e) {
    erro.value = mensagemFalha(e)
    return false
  } finally {
    salvando.value = false
  }
}
async function guardarPolitica(): Promise<void> {
  const dados = { ...politica,
    inscricoesInicio: inicioInscricao.value ? new Date(inicioInscricao.value).toISOString() : null,
    inscricoesFim: fimInscricao.value ? new Date(fimInscricao.value).toISOString() : null,
    limiteInscritos: Number(politica.limiteInscritos) || null,
  }
  await executar(() => salvarPolitica(eventoId, dados), 'Regras do evento atualizadas.')
}
async function guardarTrilha(): Promise<void> {
  if (await executar(() => criarTrilha(eventoId, trilhaNova.value.trim()), 'Trilha criada.')) trilhaNova.value = ''
}
async function guardarEspaco(): Promise<void> {
  if (await executar(() => criarEspaco(eventoId, espacoNovo.value.trim(), Number(espacoCapacidade.value) || null), 'Espaço criado.')) {
    espacoNovo.value = ''
    espacoCapacidade.value = null
  }
}
async function guardarPessoa(): Promise<void> {
  if (await executar(() => criarPessoa(eventoId, pessoaNome.value.trim(), pessoaEmail.value.trim()), 'Pessoa cadastrada.')) {
    pessoaNome.value = ''
    pessoaEmail.value = ''
  }
}
async function guardarAtividade(): Promise<void> {
  if (await executar(() => criarAtividadeProgramacao(eventoId, { ...atividade,
    titulo: atividade.titulo.trim(), descricao: atividade.descricao.trim(), local: atividade.local.trim(),
    capacidade: Number(atividade.capacidade) || null,
    trilhaId: atividade.trilhaId || null, espacoId: atividade.espacoId || null,
  }), 'Atividade adicionada à programação.')) {
    atividade.titulo = ''
    atividade.descricao = ''
    atividade.dataInicio = ''
    atividade.dataFim = ''
    atividade.local = ''
  }
}
async function guardarVinculo(): Promise<void> {
  if (await executar(() => vincularPessoa(eventoId, vinculo.atividadeId, vinculo.pessoaId, vinculo.papel), 'Papel vinculado à atividade.')) {
    vinculo.atividadeId = ''
    vinculo.pessoaId = ''
  }
}
async function declaracao(pessoaId: string, papel: 'PALESTRANTE' | 'APRESENTADOR', enviar: boolean): Promise<void> {
  if (emitindoDeclaracao.value) return
  emitindoDeclaracao.value = true
  erro.value = ''
  mensagem.value = ''
  try {
    if (enviar) {
      await enviarDeclaracao(eventoId, pessoaId, papel)
      mensagem.value = 'Declaração enviada por e-mail.'
    } else await baixarDeclaracao(eventoId, pessoaId, papel)
  } catch (e) { erro.value = mensagemFalha(e) }
  finally { emitindoDeclaracao.value = false }
}
onMounted(carregar)
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Organização" title="Programação e regras" description="Configure inscrições, vagas, trilhas, espaços, atividades e pessoas do evento." />
    <RouterLink class="btn btn-link ps-0 mb-3" to="/organizador">Voltar aos meus eventos</RouterLink>
    <div v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</div>
    <div v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</div>
    <p v-if="carregando" role="status">Carregando configuração...</p>
    <div v-else class="row g-4">
      <div class="col-xl-6">
        <section class="card border-0 shadow-sm h-100">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold">Regras de inscrição e frequência</h2>
            <form class="vstack gap-3" @submit.prevent="guardarPolitica">
              <label class="form-check"><input v-model="politica.inscricoesAbertas" class="form-check-input" type="checkbox" /> <span class="form-check-label">Inscrições abertas</span></label>
              <div><label for="inscricao-inicio" class="form-label">Abertura (opcional)</label><input id="inscricao-inicio" v-model="inicioInscricao" class="form-control" type="datetime-local" /></div>
              <div><label for="inscricao-fim" class="form-label">Encerramento (opcional)</label><input id="inscricao-fim" v-model="fimInscricao" class="form-control" type="datetime-local" /></div>
              <div><label for="limite-inscritos" class="form-label">Limite de inscritos (vazio = sem limite)</label><input id="limite-inscritos" v-model.number="politica.limiteInscritos" class="form-control" type="number" min="1" /></div>
              <label class="form-check"><input v-model="politica.permitirCancelamento" class="form-check-input" type="checkbox" /> <span class="form-check-label">Permitir cancelamento</span></label>
              <div><label for="frequencia-minima" class="form-label">Frequência mínima (%)</label><input id="frequencia-minima" v-model.number="politica.frequenciaMinimaPercentual" class="form-control" type="number" min="0" max="100" required /></div>
              <button class="btn btn-primary-custom" :disabled="salvando" type="submit">Salvar regras</button>
            </form>
          </div>
        </section>
      </div>
      <div class="col-xl-6">
        <section class="card border-0 shadow-sm h-100"><div class="card-body p-4">
          <h2 class="h5 fw-bold">Trilhas</h2>
          <form class="input-group mb-3" @submit.prevent="guardarTrilha"><input v-model="trilhaNova" class="form-control" maxlength="120" placeholder="Nome da trilha" required /><button class="btn btn-primary-custom" :disabled="salvando" type="submit">Adicionar</button></form>
          <p v-for="trilha in trilhas" :key="trilha.id" class="badge text-bg-primary-subtle me-2">{{ trilha.nome }}</p>
          <h2 class="h5 fw-bold mt-4">Espaços</h2>
          <form class="row g-2" @submit.prevent="guardarEspaco"><div class="col-sm-6"><input v-model="espacoNovo" class="form-control" maxlength="120" placeholder="Nome do espaço" required /></div><div class="col-sm-3"><input v-model.number="espacoCapacidade" class="form-control" type="number" min="1" placeholder="Vagas" /></div><div class="col-sm-3 d-grid"><button class="btn btn-primary-custom" :disabled="salvando" type="submit">Adicionar</button></div></form>
          <p v-for="espaco in espacos" :key="espaco.id" class="small mt-2 mb-0">{{ espaco.nome }}<span v-if="espaco.capacidade"> · {{ espaco.capacidade }} vagas</span></p>
          <h2 class="h5 fw-bold mt-4">Pessoas</h2>
          <form class="row g-2" @submit.prevent="guardarPessoa"><div class="col-sm-5"><input v-model="pessoaNome" class="form-control" maxlength="150" placeholder="Nome" required /></div><div class="col-sm-5"><input v-model="pessoaEmail" class="form-control" type="email" placeholder="E-mail (opcional)" /></div><div class="col-sm-2 d-grid"><button class="btn btn-primary-custom" :disabled="salvando" type="submit">Adicionar</button></div></form>
          <p v-for="pessoa in pessoas" :key="pessoa.id" class="small mt-2 mb-0">{{ pessoa.nome }}</p>
        </div></section>
      </div>
      <div class="col-12">
        <section class="card border-0 shadow-sm"><div class="card-body p-4">
          <h2 class="h5 fw-bold">Nova atividade</h2>
          <form class="row g-3" @submit.prevent="guardarAtividade">
            <div class="col-md-6"><label for="atividade-titulo-gestao" class="form-label">Título</label><input id="atividade-titulo-gestao" v-model="atividade.titulo" class="form-control" maxlength="200" required /></div>
            <div class="col-md-6"><label for="atividade-tipo-gestao" class="form-label">Tipo</label><select id="atividade-tipo-gestao" v-model="atividade.tipo" class="form-select"><option value="PALESTRA">Palestra</option><option value="OFICINA">Oficina</option><option value="APRESENTACAO">Apresentação</option><option value="MESA_REDONDA">Mesa-redonda</option><option value="OUTRO">Outro</option></select></div>
            <div class="col-12"><label for="atividade-descricao-gestao" class="form-label">Descrição</label><textarea id="atividade-descricao-gestao" v-model="atividade.descricao" class="form-control" rows="2" maxlength="10000"></textarea></div>
            <div class="col-md-6"><label for="atividade-inicio-gestao" class="form-label">Início</label><input id="atividade-inicio-gestao" v-model="atividade.dataInicio" class="form-control" type="datetime-local" required /></div>
            <div class="col-md-6"><label for="atividade-fim-gestao" class="form-label">Fim</label><input id="atividade-fim-gestao" v-model="atividade.dataFim" class="form-control" type="datetime-local" required /></div>
            <div class="col-md-4"><label for="atividade-local-gestao" class="form-label">Local</label><input id="atividade-local-gestao" v-model="atividade.local" class="form-control" maxlength="200" required /></div>
            <div class="col-md-4"><label for="atividade-trilha-gestao" class="form-label">Trilha</label><select id="atividade-trilha-gestao" v-model="atividade.trilhaId" class="form-select"><option :value="null">Sem trilha</option><option v-for="trilha in trilhas" :key="trilha.id" :value="trilha.id">{{ trilha.nome }}</option></select></div>
            <div class="col-md-4"><label for="atividade-espaco-gestao" class="form-label">Espaço</label><select id="atividade-espaco-gestao" v-model="atividade.espacoId" class="form-select"><option :value="null">Sem espaço</option><option v-for="espaco in espacos" :key="espaco.id" :value="espaco.id">{{ espaco.nome }}</option></select></div>
            <div class="col-md-4"><label for="atividade-vagas-gestao" class="form-label">Vagas (opcional)</label><input id="atividade-vagas-gestao" v-model.number="atividade.capacidade" class="form-control" type="number" min="1" /></div>
            <div class="col-md-4 align-self-end"><label class="form-check"><input v-model="atividade.presencaObrigatoria" class="form-check-input" type="checkbox" /> <span class="form-check-label">Conta para frequência</span></label></div>
            <div class="col-md-4"><label for="atividade-politica" class="form-label">Política de frequência</label><select id="atividade-politica" v-model="atividade.politicaFrequencia" class="form-select"><option value="CHECKIN_UNICO">QR / código único</option><option value="VALIDACAO_MANUAL">Validação manual</option><option value="ENTRADA_SAIDA">Entrada e saída</option><option value="PERCENTUAL_PERMANENCIA">Percentual de permanência</option></select></div>
            <div v-if="atividade.politicaFrequencia === 'PERCENTUAL_PERMANENCIA'" class="col-md-4"><label for="permanencia-minima" class="form-label">Permanência mínima (%)</label><input id="permanencia-minima" v-model.number="atividade.permanenciaMinimaPercentual" class="form-control" type="number" min="1" max="100" required /></div>
            <div class="col-md-4 d-grid align-self-end"><button class="btn btn-primary-custom" :disabled="salvando" type="submit">Criar atividade</button></div>
          </form>
        </div></section>
      </div>
      <div class="col-12">
        <section class="card border-0 shadow-sm"><div class="card-body p-4">
          <h2 class="h5 fw-bold">Palestrantes, apresentadores e responsáveis</h2>
          <form class="row g-2 align-items-end" @submit.prevent="guardarVinculo">
            <div class="col-md-4"><label for="vinculo-atividade" class="form-label">Atividade</label><select id="vinculo-atividade" v-model="vinculo.atividadeId" class="form-select" required><option value="">Selecione</option><option v-for="item in atividades" :key="item.id" :value="item.id">{{ item.titulo }}</option></select></div>
            <div class="col-md-3"><label for="vinculo-pessoa" class="form-label">Pessoa</label><select id="vinculo-pessoa" v-model="vinculo.pessoaId" class="form-select" required><option value="">Selecione</option><option v-for="pessoa in pessoas" :key="pessoa.id" :value="pessoa.id">{{ pessoa.nome }}</option></select></div>
            <div class="col-md-3"><label for="vinculo-papel" class="form-label">Papel</label><select id="vinculo-papel" v-model="vinculo.papel" class="form-select"><option value="PALESTRANTE">Palestrante</option><option value="APRESENTADOR">Apresentador</option><option value="RESPONSAVEL">Responsável</option></select></div>
            <div class="col-md-2 d-grid"><button class="btn btn-primary-custom" :disabled="salvando" type="submit">Vincular</button></div>
          </form>
          <p class="small text-muted mt-3">Declarações ficam disponíveis após encerrar o evento. O envio exige e-mail da pessoa e SMTP configurado.</p>
          <div class="mt-4"><article v-for="item in atividades" :key="item.id" class="border-top py-3"><strong>{{ item.titulo }}</strong> <span class="text-secondary small">{{ formatarData(item.dataInicio) }} · {{ item.trilha ?? 'Sem trilha' }}</span><RouterLink class="btn btn-sm btn-link" :to="`/organizador/atividades/${item.id}/questionario`">Questionário da atividade</RouterLink><div v-for="pessoa in item.pessoas" :key="`${pessoa.pessoaId}-${pessoa.papel}`" class="small mb-2">{{ pessoa.nome }} — {{ pessoa.papel }}
            <template v-if="pessoa.papel === 'PALESTRANTE' || pessoa.papel === 'APRESENTADOR'">
              <button class="btn btn-sm btn-link" :disabled="emitindoDeclaracao" @click="declaracao(pessoa.pessoaId, pessoa.papel, false)">Baixar declaração</button>
              <button class="btn btn-sm btn-link" :disabled="emitindoDeclaracao" @click="declaracao(pessoa.pessoaId, pessoa.papel, true)">Enviar e-mail</button>
            </template>
          </div></article></div>
        </div></section>
      </div>
    </div>
  </div>
</template>
