<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="navbar">
    <div class="logo">
      <h2>🎟️ Gestão de Eventos</h2>
    </div>

    <nav>
      <!-- Esses links vão dar erro no console por enquanto se as rotas não existirem, mas o visual vai funcionar perfeitamente -->
      <RouterLink to="/">Vitrine</RouterLink>
      <RouterLink to="/agenda">Minha Agenda</RouterLink>
      <RouterLink to="/painel">Painel do Organizador</RouterLink>
    </nav>
  </header>

  <main class="container">
    <!-- É aqui que as outras páginas serão renderizadas dinamicamente -->
    <RouterView />
  </main>
</template>

<style scoped>
/* Estilização base sem depender de bibliotecas externas */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 3rem;
  background-color: #1a1a2e; /* Azul escuro moderno */
  color: white;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: bold;
}

nav {
  display: flex;
  gap: 2rem;
}

nav a {
  color: #e0e0e0;
  text-decoration: none;
  font-weight: 500;
  font-size: 1.1rem;
  transition: 0.3s;
}

nav a:hover, nav a.router-link-exact-active {
  color: #42b983; /* Verde destaque para o link ativo */
  border-bottom: 2px solid #42b983;
}

.container {
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
