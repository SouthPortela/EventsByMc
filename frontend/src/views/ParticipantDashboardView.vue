<script setup lang="ts">
import { RouterLink } from 'vue-router'
import AppIcon from '@/components/icons/AppIcon.vue'
import DashboardPageHeader from '@/components/dashboard/DashboardPageHeader.vue'
import StatCard from '@/components/dashboard/StatCard.vue'
import QrCodePlaceholder from '@/components/brand/QrCodePlaceholder.vue'
</script>

<template>
  <div class="container-fluid p-4 p-xl-5">
    <DashboardPageHeader
      eyebrow="Área do participante"
      title="Olá, participante!"
      description="Acompanhe seus próximos compromissos e comprovantes de participação."
    >
      <template #actions>
        <RouterLink class="btn btn-primary-custom px-4" to="/">Explorar eventos</RouterLink>
      </template>
    </DashboardPageHeader>

    <div class="row g-3 mb-4">
      <div class="col-md-4">
        <StatCard
          label="Inscrições ativas"
          :value="2"
          icon="ticket"
          detail="1 evento nesta semana"
        />
      </div>
      <div class="col-md-4">
        <StatCard
          label="Atividades na agenda"
          :value="3"
          icon="calendar"
          detail="Próxima hoje às 19h"
        />
      </div>
      <div class="col-md-4">
        <StatCard
          label="Presenças confirmadas"
          :value="8"
          icon="check"
          detail="Histórico acumulado"
        />
      </div>
    </div>

    <div class="row g-4">
      <div class="col-xl-8">
        <section class="card border-0 shadow-sm h-100">
          <div class="card-header bg-white d-flex justify-content-between align-items-center p-4">
            <h2 class="h5 fw-bold mb-0">Próxima atividade</h2>
            <RouterLink
              class="small fw-bold text-primary-custom text-decoration-none"
              to="/participante/agenda"
            >
              Ver agenda
            </RouterLink>
          </div>
          <div class="card-body p-4">
            <div class="row align-items-center g-4">
              <div class="col-sm-auto">
                <div class="bg-primary-subtle text-primary-custom rounded-3 text-center px-4 py-3">
                  <span class="small fw-bold d-block">10 SET</span>
                  <span class="h4 fw-bold d-block mb-0">19:00</span>
                </div>
              </div>
              <div class="col">
                <span class="badge text-bg-primary-subtle text-primary-custom mb-2"
                  >Cibersegurança</span
                >
                <h3 class="h4 fw-bold mb-2">Abertura e Tendências de Ameaças</h3>
                <p class="text-secondary mb-2">Simpósio de Cibersegurança e Defesa</p>
                <p class="small d-flex align-items-center gap-2 mb-0">
                  <AppIcon name="map-pin" :size="16" /> Auditório Principal
                </p>
              </div>
            </div>
          </div>
        </section>
      </div>
      <div class="col-xl-4">
        <section class="card border-0 shadow-sm h-100">
          <div class="card-body text-center p-4">
            <span
              class="d-inline-flex rounded-circle bg-primary-subtle text-primary-custom p-3 mb-3"
            >
              <AppIcon name="check" :size="24" />
            </span>
            <h2 class="h5 fw-bold">Check-in por QR Code</h2>
            <p class="small text-secondary">
              Este código será individual, temporário e validado pela API.
            </p>
            <button
              class="btn btn-outline-primary-custom w-100"
              type="button"
              data-bs-toggle="modal"
              data-bs-target="#qr-modal"
            >
              Exibir meu QR Code
            </button>
          </div>
        </section>
      </div>
    </div>

    <div
      id="qr-modal"
      class="modal fade"
      tabindex="-1"
      aria-labelledby="qr-title"
      aria-hidden="true"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 shadow">
          <div class="modal-header border-0">
            <h2 id="qr-title" class="modal-title h5 fw-bold">QR Code de presença</h2>
            <button
              class="btn-close"
              type="button"
              data-bs-dismiss="modal"
              aria-label="Fechar"
            ></button>
          </div>
          <div class="modal-body text-center pb-5">
            <QrCodePlaceholder />
            <p class="small text-secondary mt-3 mb-0">
              Protótipo visual: a API emitirá um token JWT de curta duração.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
