import { describe, expect, it } from 'vitest'
import { validarEvento } from './validarEvento'

describe('validarEvento', () => {
  it('exige capacidade apenas quando há controle de vagas', () => {
    const base = {
      titulo: 'Evento válido',
      descricao: 'Descrição suficientemente detalhada do evento.',
      dataInicio: '2026-09-10T19:00',
      local: 'Auditório',
    }

    expect(validarEvento({ ...base, controlaVagas: false, capacidade: null })).toEqual({})
    expect(validarEvento({ ...base, controlaVagas: true, capacidade: null }).capacidade).toBe(
      'Informe uma capacidade maior que zero.',
    )
  })
})
