import { eventosMock } from '../mocks/eventosMock'
import type { EventoDetalhe, EventoResumo } from '../types/evento'

export async function listarEventos(): Promise<EventoResumo[]> {
  return eventosMock.map((evento) => ({
    id: evento.id,
    titulo: evento.titulo,
    local: evento.local,
    vagas: evento.vagas,
    dataInicio: evento.dataInicio,
    categoria: evento.categoria,
    modalidade: evento.modalidade,
    banner: evento.banner,
    preco: evento.preco,
    gratuito: evento.gratuito,
    destaque: evento.destaque,
  }))
}

export async function buscarEventoPorId(id: number): Promise<EventoDetalhe | undefined> {
  return eventosMock.find((evento) => evento.id === id)
}
