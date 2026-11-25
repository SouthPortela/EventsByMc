<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import { listarContas, alterarPerfis, type ContaAdmin, type PerfilConta } from '@/features/admin/services/adminService'
import { normalizarTexto } from '@/features/events/utils/normalizarTexto'
import { ApiError } from '@/shared/services/httpClient'
import { useAuthStore } from '@/stores/auth'

const usuarios = ref<ContaAdmin[]>([])
const busca = ref('')
const carregando = ref(false)
const salvando = ref(false)
const erro = ref('')
const mensagem = ref('')
const selecionado = ref<ContaAdmin | null>(null)
const perfis = ref<PerfilConta[]>([])
const opcoes: PerfilConta[] = ['PARTICIPANTE', 'ORGANIZADOR', 'ADMINISTRADOR']
const auth = useAuthStore()
const router = useRouter()
const resultados = computed(() => usuarios.value.filter(u => normalizarTexto(`${u.nome} ${u.email} ${u.id}`).includes(normalizarTexto(busca.value))))
async function carregar(): Promise<void> {
  carregando.value = true
  erro.value = ''
  try { usuarios.value = await listarContas() }
  catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível consultar os usuários.' }
  finally { carregando.value = false }
}
function revisar(usuario: ContaAdmin): void {
  selecionado.value = usuario
  perfis.value = [...usuario.perfis]
  mensagem.value = ''
}
async function salvar(): Promise<void> {
  if (!selecionado.value || salvando.value || !perfis.value.length) return
  salvando.value = true
  erro.value = ''
  const id = selecionado.value.id
  try {
    const atualizado = await alterarPerfis(id, perfis.value)
    usuarios.value = usuarios.value.map(u => u.id === id ? atualizado : u)
    selecionado.value = null
    mensagem.value = 'Perfis atualizados. O usuário deve entrar novamente para atualizar seu menu.'
    if (id === auth.usuarioId) { auth.sair(); await router.replace('/login') }
  } catch (e) { erro.value = e instanceof ApiError ? e.message : 'Não foi possível confirmar a alteração. Atualize a lista.' }
  finally { salvando.value = false }
}
onMounted(carregar)
</script>
<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader eyebrow="Controle de acesso" title="Gestão de usuários" description="Consulte contas e altere seus perfis. Organizadores não precisam de aprovação." />
    <p v-if="erro" class="alert alert-warning" role="alert">{{ erro }}</p>
    <p v-if="mensagem" class="alert alert-success" role="status">{{ mensagem }}</p>
    <form v-if="selecionado" class="card card-body mb-4" @submit.prevent="salvar">
      <h2 class="h5">Perfis de {{ selecionado.nome }}</h2>
      <p class="small text-muted text-break">{{ selecionado.id }}</p>
      <label v-for="opcao in opcoes" :key="opcao" class="form-check">
        <input v-model="perfis" class="form-check-input" type="checkbox" :value="opcao" :disabled="salvando" />
        <span class="form-check-label">{{ opcao }}</span>
      </label>
      <p class="small mt-2">Mantenha ao menos um perfil. O último administrador não pode ser removido.</p>
      <div class="d-flex gap-2"><button class="btn btn-primary" :disabled="salvando || !perfis.length">Salvar perfis</button><button class="btn btn-outline-secondary" type="button" :disabled="salvando" @click="selecionado = null">Cancelar</button></div>
    </form>
    <div class="d-flex gap-2 mb-3">
      <input v-model="busca" class="form-control" type="search" aria-label="Buscar usuário" placeholder="Nome, e-mail ou UUID" />
      <button class="btn btn-outline-primary" :disabled="carregando || salvando" @click="carregar">Atualizar</button>
    </div>
    <p v-if="carregando" role="status">Carregando usuários...</p>
    <div v-else class="table-responsive card"><table class="table align-middle mb-0">
      <thead><tr><th>Usuário</th><th>Perfis</th><th>Ações</th></tr></thead>
      <tbody><tr v-for="usuario in resultados" :key="usuario.id">
        <td><strong>{{ usuario.nome }}</strong><div>{{ usuario.email }}</div><small class="text-muted">{{ usuario.id }}</small></td>
        <td><span v-for="perfil in usuario.perfis" :key="perfil" class="badge text-bg-primary me-1">{{ perfil }}</span></td>
        <td><button class="btn btn-sm btn-outline-primary" :disabled="salvando" @click="revisar(usuario)">Revisar perfis</button></td>
      </tr><tr v-if="!resultados.length"><td colspan="3">Nenhum usuário encontrado.</td></tr></tbody>
    </table></div>
  </div>
</template>
