<script setup lang="ts">
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'

const registros = [
  {
    id: 'AUD-9041',
    horario: '19 ago 2026 · 14:32',
    ator: 'Administrador #4',
    acao: 'Perfil de organizador revisado',
    recurso: 'Usuário #203',
    resultado: 'SUCESSO',
  },
  {
    id: 'AUD-9040',
    horario: '19 ago 2026 · 14:18',
    ator: 'Sistema',
    acao: 'Token de frequência expirado',
    recurso: 'Atividade #18',
    resultado: 'BLOQUEADO',
  },
  {
    id: 'AUD-9039',
    horario: '19 ago 2026 · 13:56',
    ator: 'Organizador #19',
    acao: 'Evento enviado para revisão',
    recurso: 'Evento #23',
    resultado: 'SUCESSO',
  },
  {
    id: 'AUD-9038',
    horario: '19 ago 2026 · 12:41',
    ator: 'Administrador #2',
    acao: 'Publicação aprovada',
    recurso: 'Evento #21',
    resultado: 'SUCESSO',
  },
]
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Segurança"
      title="Trilha de auditoria"
      description="Consulte operações administrativas sem registrar tokens ou dados pessoais sensíveis."
    >
      <template #actions
        ><button class="btn btn-outline-primary-custom" type="button">
          <AppIcon name="report" :size="17" /> Exportar
        </button></template
      >
    </DashboardPageHeader>
    <div class="alert alert-primary border-0 d-flex gap-2 align-items-center" role="note">
      <AppIcon name="shield" :size="20" /><span
        >Os dados apresentados são exemplos anonimizados para preparar o contrato da futura
        API.</span
      >
    </div>
    <section class="card border-0 shadow-sm">
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th class="ps-4" scope="col">Horário</th>
              <th scope="col">Ator</th>
              <th scope="col">Operação</th>
              <th scope="col">Recurso</th>
              <th class="pe-4" scope="col">Resultado</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="registro in registros" :key="registro.id">
              <td class="ps-4 small">
                {{ registro.horario }}<span class="d-block text-secondary">{{ registro.id }}</span>
              </td>
              <td>{{ registro.ator }}</td>
              <td>{{ registro.acao }}</td>
              <td>{{ registro.recurso }}</td>
              <td class="pe-4">
                <span
                  class="badge"
                  :class="
                    registro.resultado === 'SUCESSO' ? 'text-bg-success' : 'text-bg-secondary'
                  "
                  >{{ registro.resultado.toLocaleLowerCase('pt-BR') }}</span
                >
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
