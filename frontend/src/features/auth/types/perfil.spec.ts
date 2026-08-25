import { describe, expect, it } from 'vitest'
import { possuiNivel } from './perfil'

describe('possuiNivel', () => {
  it('permite que administrador acesse todos os níveis inferiores', () => {
    expect(possuiNivel('ADMINISTRADOR', 'PARTICIPANTE')).toBe(true)
    expect(possuiNivel('ADMINISTRADOR', 'ORGANIZADOR')).toBe(true)
  })

  it('impede participante de acessar área de organizador', () => {
    expect(possuiNivel('PARTICIPANTE', 'ORGANIZADOR')).toBe(false)
  })
})
