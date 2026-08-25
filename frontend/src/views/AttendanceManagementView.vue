<script setup lang="ts">
import { ref } from 'vue'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'

const atividade = ref('1')
const codigo = ref('')
const mensagem = ref('')

function confirmarManualmente(): void {
  mensagem.value = codigo.value.trim()
    ? 'Código preparado para validação pela API REST.'
    : 'Informe o código da inscrição.'
}
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Operação do evento"
      title="Controle de frequência"
      description="Valide a presença por QR Code ou faça uma confirmação manual auditável."
    />

    <div class="row g-3 mb-4">
      <div class="col-md-4"><StatCard label="Inscritos" :value="148" icon="users" /></div>
      <div class="col-md-4">
        <StatCard label="Presentes" :value="96" icon="check" detail="64,8% de comparecimento" />
      </div>
      <div class="col-md-4"><StatCard label="Aguardando" :value="52" icon="ticket" /></div>
    </div>

    <section class="card border-0 shadow-sm mb-4">
      <div class="card-body p-4">
        <label class="form-label fw-bold" for="atividade">Atividade em atendimento</label>
        <select id="atividade" v-model="atividade" class="form-select form-select-lg">
          <option value="1">Abertura e Tendências de Ameaças — 19:00</option>
          <option value="2">Segurança em APIs REST e OAuth2 — 19:45</option>
        </select>
      </div>
    </section>

    <div class="row g-4">
      <div class="col-lg-7">
        <section class="card border-0 shadow-sm h-100">
          <div class="card-body p-4 p-xl-5 text-center">
            <span
              class="d-inline-flex bg-primary-subtle text-primary-custom rounded-circle p-4 mb-3"
            >
              <AppIcon name="check" :size="38" />
            </span>
            <h2 class="h4 fw-bold">Leitura de QR Code</h2>
            <p class="text-secondary col-lg-10 mx-auto">
              O acesso à câmera será ativado somente depois que o backend disponibilizar a rota
              protegida de validação JWT.
            </p>
            <button class="btn btn-primary-custom btn-lg px-5" type="button" disabled>
              Iniciar leitor
            </button>
          </div>
        </section>
      </div>
      <div class="col-lg-5">
        <section class="card border-0 shadow-sm h-100">
          <div class="card-body p-4">
            <h2 class="h5 fw-bold">Confirmação manual</h2>
            <p class="small text-secondary">Use apenas quando a leitura não estiver disponível.</p>
            <div v-if="mensagem" class="alert alert-info py-2" role="status">{{ mensagem }}</div>
            <label class="form-label fw-semibold" for="codigo-inscricao">Código da inscrição</label>
            <input
              id="codigo-inscricao"
              v-model="codigo"
              class="form-control form-control-lg mb-3"
              maxlength="40"
            />
            <button
              class="btn btn-outline-primary-custom w-100"
              type="button"
              @click="confirmarManualmente"
            >
              Validar presença
            </button>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
