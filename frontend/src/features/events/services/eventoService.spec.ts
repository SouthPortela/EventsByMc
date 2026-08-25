import { describe, expect, it } from 'vitest'
import { buscarEventoPorId, listarEventos } from './eventoService'

describe('eventoService', () => {
  it('lista os resumos dos eventos simulados', async () => {
    const eventos = await listarEventos()

    expect(eventos.length).toBeGreaterThan(0)
    expect(eventos[0]).not.toHaveProperty('atividades')
  })

  it('busca os detalhes de um evento pelo identificador', async () => {
    const evento = await buscarEventoPorId(1)

    expect(evento?.titulo).toBe('Simpósio de Cibersegurança e Defesa')
    expect(evento?.atividades).toHaveLength(3)
  })

  it('retorna undefined quando o evento não existe', async () => {
    expect(await buscarEventoPorId(999)).toBeUndefined()
  })
})
