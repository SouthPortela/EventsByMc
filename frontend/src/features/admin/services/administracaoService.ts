import { apiRequest } from '@/shared/services/httpClient'
import type { CategoriaEvento } from '@/features/events/types/categoria'
import type { PerfilUsuario } from '@/features/auth/types/perfil'

export type EstadoEvento = 'RASCUNHO' | 'PUBLICADO' | 'ENCERRADO' | 'SUSPENSO' | 'EXCLUIDO'

export interface EventoAdministrativo {
  evento: {
    id: string
    titulo: string
    descricao: string | null
    local: string | null
    dataInicio: string | null
    dataFim: string | null
    estado: EstadoEvento
    categoria: CategoriaEvento
    atividades: {
      id: string
      titulo: string
      local: string | null
      dataInicio: string | null
      dataFim: string | null
    }[]
  }
  organizadorId: string
  organizadorNome: string
}

export interface UsuarioAdministrativo {
  id: string
  nome: string
  emailMascarado: string
  perfis: PerfilUsuario[]
}

export interface RegistroModeracao {
  id: string
  eventoId: string
  administradorId: string
  administradorNome: string
  estadoAnterior: EstadoEvento
  estadoNovo: EstadoEvento
  motivo: string
  criadoEm: string
}

const baseEvento = (id: string): string => `/admin/eventos/${encodeURIComponent(id)}`

export function listarEventosAdministrativos(): Promise<EventoAdministrativo[]> {
  return apiRequest('/admin/eventos', { autenticada: true })
}

export function consultarEventoAdministrativo(id: string): Promise<EventoAdministrativo> {
  return apiRequest(baseEvento(id), { autenticada: true })
}

export function listarUsuariosAdministrativos(): Promise<UsuarioAdministrativo[]> {
  return apiRequest('/admin/usuarios', { autenticada: true })
}

export function listarModeracoes(): Promise<RegistroModeracao[]> {
  return apiRequest('/admin/moderacoes', { autenticada: true })
}

export function moderarEvento(
  id: string,
  acao: 'suspensao' | 'restauracao' | 'exclusao',
  motivo: string,
): Promise<EventoAdministrativo> {
  const exclusao = acao === 'exclusao'
  return apiRequest(exclusao ? baseEvento(id) : `${baseEvento(id)}/${acao}`, {
    method: exclusao ? 'DELETE' : 'POST',
    autenticada: true,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ motivo }),
  })
}
