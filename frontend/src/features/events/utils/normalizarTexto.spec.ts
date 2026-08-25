import { describe, expect, it } from 'vitest'
import { normalizarTexto } from './normalizarTexto'

describe('normalizarTexto', () => {
  it('remove acentos e converte o texto para letras minúsculas', () => {
    expect(normalizarTexto('Simpósio')).toBe('simposio')
    expect(normalizarTexto('Auditório Principal')).toBe('auditorio principal')
  })

  it('remove espaços no início e no final', () => {
    expect(normalizarTexto('  Oficina de Java  ')).toBe('oficina de java')
  })
})
