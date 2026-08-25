import type { EventoResumo } from '../types/evento'
import { normalizarTexto } from './normalizarTexto'

export interface FiltrosEvento {
  termo: string
  categoria: string
  local: string
}

export function filtrarEventos(eventos: EventoResumo[], filtros: FiltrosEvento): EventoResumo[] {
  const termo = normalizarTexto(filtros.termo)

  return eventos.filter((evento) => {
    const correspondeTermo =
      !termo ||
      normalizarTexto(evento.titulo).includes(termo) ||
      normalizarTexto(evento.local).includes(termo)
    const correspondeCategoria = !filtros.categoria || evento.categoria === filtros.categoria
    const correspondeLocal = !filtros.local || evento.local === filtros.local

    return correspondeTermo && correspondeCategoria && correspondeLocal
  })
}
