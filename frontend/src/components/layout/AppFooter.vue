<script setup lang="ts">
import { RouterLink } from 'vue-router'
import LogoMark from '@/components/brand/LogoMark.vue'
</script>

<template>
  <footer class="bg-dark text-light py-5 mt-auto">
    <div class="container">
      <div class="row g-4 mb-4">
        <div class="col-md-4">
          <div class="d-flex align-items-center gap-2 mb-3">
            <LogoMark light :size="30" />
            <span class="fs-5 fw-bold"
              >Events<span class="text-secondary fw-medium">ByMc</span></span
            >
          </div>
          <p class="small text-secondary mb-1">Plataforma de Gestão de Eventos</p>
          <p class="small text-muted fst-italic">Uma marca Monkey Corp.</p>
        </div>
        <div class="col-6 col-md-2 offset-md-1">
          <h2 class="text-white fw-bold mb-3 fs-6">Para Participantes</h2>
          <ul class="list-unstyled small d-flex flex-column gap-2">
            <li>
              <RouterLink class="text-decoration-none text-secondary hover-white" to="/"
                >Explorar eventos</RouterLink
              >
            </li>
            <li>
              <RouterLink
                class="text-decoration-none text-secondary hover-white"
                to="/participante/inscricoes"
                >Minhas inscrições</RouterLink
              >
            </li>
            <li>
              <RouterLink
                class="text-decoration-none text-secondary hover-white"
                to="/participante/agenda"
                >Minha agenda</RouterLink
              >
            </li>
          </ul>
        </div>
        <div class="col-6 col-md-2">
          <h2 class="text-white fw-bold mb-3 fs-6">Para Organizadores</h2>
          <ul class="list-unstyled small d-flex flex-column gap-2">
            <li>
              <RouterLink
                class="text-decoration-none text-secondary hover-white"
                to="/organizador/eventos/novo"
                >Criar evento</RouterLink
              >
            </li>
            <li>
              <RouterLink class="text-decoration-none text-secondary hover-white" to="/organizador"
                >Soluções</RouterLink
              >
            </li>
            <li>
              <RouterLink
                class="text-decoration-none text-secondary hover-white"
                to="/organizador/relatorios"
                >Relatórios</RouterLink
              >
            </li>
          </ul>
        </div>
        <div class="col-12 col-md-3">
          <h2 class="text-white fw-bold mb-3 fs-6">Institucional</h2>
          <ul class="list-unstyled small d-flex flex-column gap-2">
            <li><span class="text-secondary">Sobre nós</span></li>
            <li><span class="text-secondary">Termos e políticas</span></li>
            <li><span class="text-secondary">Privacidade</span></li>
          </ul>
        </div>
      </div>
      <div class="border-top border-secondary pt-4">
        <p class="small text-secondary mb-0">
          © 2026 EventsByMc / Monkey Corp. Todos os direitos reservados.
        </p>
      </div>
    </div>
  </footer>
</template>
