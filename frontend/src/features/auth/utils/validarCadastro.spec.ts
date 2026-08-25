import { describe, expect, it } from 'vitest'
import { validarCadastro } from './validarCadastro'

describe('validarCadastro', () => {
  it('aceita um cadastro válido', () => {
    expect(
      validarCadastro({
        nome: 'Maria Silva',
        email: 'maria@exemplo.com',
        senha: 'segura123',
        confirmacaoSenha: 'segura123',
        aceitouTermos: true,
      }),
    ).toEqual({})
  })

  it('rejeita senhas diferentes e termos não aceitos', () => {
    const erros = validarCadastro({
      nome: 'Maria Silva',
      email: 'maria@exemplo.com',
      senha: 'segura123',
      confirmacaoSenha: 'outraSenha',
      aceitouTermos: false,
    })

    expect(erros.confirmacaoSenha).toBe('As senhas não conferem.')
    expect(erros.aceitouTermos).toBe('Você precisa aceitar os termos para continuar.')
  })
})
