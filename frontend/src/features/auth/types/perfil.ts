export type PerfilUsuario = 'VISITANTE' | 'PARTICIPANTE' | 'ORGANIZADOR' | 'ADMINISTRADOR'

export const PERFIS: PerfilUsuario[] = ['VISITANTE', 'PARTICIPANTE', 'ORGANIZADOR', 'ADMINISTRADOR']

export const PERFIL_LABEL: Record<PerfilUsuario, string> = {
  VISITANTE: 'Visitante',
  PARTICIPANTE: 'Participante',
  ORGANIZADOR: 'Organizador',
  ADMINISTRADOR: 'Administrador',
}

const NIVEL_PERFIL: Record<PerfilUsuario, number> = {
  VISITANTE: 0,
  PARTICIPANTE: 1,
  ORGANIZADOR: 2,
  ADMINISTRADOR: 3,
}

export function possuiNivel(perfil: PerfilUsuario, minimo: PerfilUsuario): boolean {
  return NIVEL_PERFIL[perfil] >= NIVEL_PERFIL[minimo]
}
