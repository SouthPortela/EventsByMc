import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardLayout from '@/components/layout/DashboardLayout.vue'
import { possuiNivel } from '@/features/auth/types/perfil'
import { useAuthStore } from '@/stores/auth'
import { pinia } from '@/stores'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView, meta: { title: 'Eventos' } },
    {
      path: '/eventos/:id',
      name: 'event-details',
      component: () => import('../views/EventDetailsView.vue'),
      meta: { title: 'Detalhes do evento' },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: 'Entrar' },
    },
    {
      path: '/cadastro',
      name: 'register',
      component: () => import('../views/RegisterView.vue'),
      meta: { title: 'Cadastro' },
    },
    {
      path: '/acesso-negado',
      name: 'access-denied',
      component: () => import('../views/AccessDeniedView.vue'),
      meta: { title: 'Acesso negado' },
    },
    {
      path: '/participante',
      component: DashboardLayout,
      meta: { minRole: 'PARTICIPANTE', dashboard: true },
      children: [
        {
          path: '',
          name: 'participant-dashboard',
          component: () => import('../views/ParticipantDashboardView.vue'),
          meta: { title: 'Painel do participante' },
        },
        {
          path: 'inscricoes',
          name: 'participant-registrations',
          component: () => import('../views/ParticipantRegistrationsView.vue'),
          meta: { title: 'Minhas inscrições' },
        },
        {
          path: 'agenda',
          name: 'agenda',
          component: () => import('../views/AgendaView.vue'),
          meta: { title: 'Minha agenda' },
        },
        {
          path: 'conta',
          name: 'account',
          component: () => import('../views/AccountView.vue'),
          meta: { title: 'Minha conta' },
        },
      ],
    },
    {
      path: '/organizador',
      component: DashboardLayout,
      meta: { minRole: 'ORGANIZADOR', dashboard: true },
      children: [
        {
          path: '',
          name: 'organizer-dashboard',
          component: () => import('../views/OrganizerDashboardView.vue'),
          meta: { title: 'Painel do organizador' },
        },
        {
          path: 'eventos/novo',
          name: 'event-create',
          component: () => import('../views/EventFormView.vue'),
          meta: { title: 'Criar evento' },
        },
        {
          path: 'inscricoes',
          name: 'organizer-registrations',
          component: () => import('../views/OrganizerRegistrationsView.vue'),
          meta: { title: 'Gestão de inscrições' },
        },
        {
          path: 'frequencia',
          name: 'attendance-management',
          component: () => import('../views/AttendanceManagementView.vue'),
          meta: { title: 'Gestão de frequência' },
        },
        {
          path: 'relatorios',
          name: 'reports',
          component: () => import('../views/ReportsView.vue'),
          meta: { title: 'Relatórios' },
        },
      ],
    },
    {
      path: '/admin',
      component: DashboardLayout,
      meta: { minRole: 'ADMINISTRADOR', dashboard: true },
      children: [
        {
          path: '',
          name: 'admin-dashboard',
          component: () => import('../views/AdminDashboardView.vue'),
          meta: { title: 'Administração' },
        },
        {
          path: 'usuarios',
          name: 'admin-users',
          component: () => import('../views/UserManagementView.vue'),
          meta: { title: 'Gestão de usuários' },
        },
        {
          path: 'eventos',
          name: 'admin-events',
          component: () => import('../views/AdminEventsView.vue'),
          meta: { title: 'Eventos da plataforma' },
        },
        {
          path: 'auditoria',
          name: 'admin-audit',
          component: () => import('../views/AuditView.vue'),
          meta: { title: 'Auditoria' },
        },
      ],
    },
    { path: '/agenda', redirect: '/participante/agenda' },
    { path: '/conta', redirect: '/participante/conta' },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('../views/NotFoundView.vue'),
      meta: { title: 'Página não encontrada' },
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore(pinia)
  const minimo = to.matched.reduce(
    (perfil, record) => record.meta.minRole ?? perfil,
    to.meta.minRole,
  )

  document.title = `${to.meta.title ?? 'EventsByMc'} | EventsByMc`

  if (minimo && !possuiNivel(auth.perfil, minimo)) {
    return { name: 'access-denied', query: { destino: to.fullPath } }
  }

  return true
})

export default router
