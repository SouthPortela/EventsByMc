import { describe, expect, it } from 'vitest'
import { validarEvento } from './validarEvento'

const base = {
  titulo: 'Evento válido',
  descricao: 'Descrição suficientemente detalhada do evento.',
  dataInicio: '2026-09-10T19:00',
  dataFim: '2026-09-10T21:00',
  local: 'Auditório',
}
describe('validarEvento', () => {
  it('aceita os campos suportados pela API', () => expect(validarEvento(base)).toEqual({}))
  it('exige término posterior ao início', () => {
    expect(validarEvento({ ...base, dataFim: base.dataInicio }).dataFim).toBeTruthy()
  })
  it('rejeita datas inválidas e local vazio', () => {
    expect(validarEvento({ ...base, dataInicio: 'inválido', local: ' ' })).toHaveProperty(
      'dataInicio',
    )
    expect(validarEvento({ ...base, local: ' ' })).toHaveProperty('local')
  })
  it('respeita os limites do banco e do servidor', () => {
    expect(validarEvento({ ...base, titulo: 'x'.repeat(201), descricao: 'curta' })).toEqual({
      titulo: expect.any(String),
      descricao: expect.any(String),
    })
  })
})
