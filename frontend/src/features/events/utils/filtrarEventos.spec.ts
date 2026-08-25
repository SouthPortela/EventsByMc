import { describe, expect, it } from 'vitest'
import type { EventoResumo } from '../types/evento'
import { filtrarEventos } from './filtrarEventos'

const eventos: EventoResumo[] = [
  {
    id: 1,
    titulo: 'Simpósio de Segurança',
    local: 'Auditório',
    vagas: 10,
    dataInicio: '2026-09-10T19:00:00-03:00',
    categoria: 'Tecnologia',
  },
  {
    id: 2,
    titulo: 'Oficina de Design',
    local: 'Laboratório',
    vagas: 5,
    dataInicio: '2026-09-11T19:00:00-03:00',
    categoria: 'Design',
  },
]

describe('filtrarEventos', () => {
  it('combina termo, categoria e local', () => {
    const resultado = filtrarEventos(eventos, {
      termo: 'simposio',
      categoria: 'Tecnologia',
      local: 'Auditório',
    })

    expect(resultado.map((evento) => evento.id)).toEqual([1])
  })

  it('retorna todos os eventos quando os filtros estão vazios', () => {
    expect(filtrarEventos(eventos, { termo: '', categoria: '', local: '' })).toHaveLength(2)
  })
})
