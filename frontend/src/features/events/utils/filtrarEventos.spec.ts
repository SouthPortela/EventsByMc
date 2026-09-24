import { describe, expect, it } from 'vitest'
import type { EventoResumo } from '../types/evento'
import { filtrarEventos } from './filtrarEventos'

const eventos: EventoResumo[] = [
  {
    id: '1',
    titulo: 'Simpósio de Segurança',
    local: 'Auditório',
    vagas: 10,
    dataInicio: '2026-09-10T19:00:00-03:00',
    categoria: 'TECNOLOGIA',
  },
  {
    id: '2',
    titulo: 'Oficina de Design',
    local: 'Laboratório',
    vagas: 5,
    dataInicio: '2026-09-11T19:00:00-03:00',
    categoria: 'ACADEMICO',
  },
]

describe('filtrarEventos', () => {
  it('combina termo, categoria e local', () => {
    const resultado = filtrarEventos(eventos, {
      termo: 'simposio',
      categoria: 'TECNOLOGIA',
      local: 'Auditório',
    })

    expect(resultado.map((evento) => evento.id)).toEqual(['1'])
  })

  it('retorna todos os eventos quando os filtros estão vazios', () => {
    expect(filtrarEventos(eventos, { termo: '', categoria: '', local: '' })).toHaveLength(2)
  })

  it('filtra pela categoria salva, mesmo quando o título não contém o tema', () => {
    expect(filtrarEventos(eventos, { termo: '', categoria: 'ACADEMICO', local: '' }).map(e => e.id)).toEqual(['2'])
    expect(filtrarEventos(eventos, { termo: 'academico', categoria: '', local: '' }).map(e => e.id)).toEqual(['2'])
  })
})
