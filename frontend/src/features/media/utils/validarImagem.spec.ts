import { describe, expect, it } from 'vitest'
import { validarImagem } from './validarImagem'

describe('validarImagem', () => {
  it('aceita imagem WebP dentro do limite do banner', () => {
    expect(validarImagem({ type: 'image/webp', size: 1024 }, 'BANNER_EVENTO')).toBeNull()
  })

  it('rejeita formato não permitido', () => {
    expect(validarImagem({ type: 'application/pdf', size: 1024 }, 'FOTO_PERFIL')).toBe(
      'Selecione uma imagem JPEG, PNG ou WebP.',
    )
  })

  it('aplica limite menor para foto de perfil', () => {
    expect(validarImagem({ type: 'image/png', size: 3 * 1024 * 1024 }, 'FOTO_PERFIL')).toBe(
      'A imagem deve ter no máximo 2 MB.',
    )
  })
})
