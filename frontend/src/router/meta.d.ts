import 'vue-router'
import type { PerfilUsuario } from '@/features/auth/types/perfil'

declare module 'vue-router' {
  interface RouteMeta {
    minRole?: PerfilUsuario
    dashboard?: boolean
    title?: string
  }
}

export {}
