import { describe, expect, it } from 'vitest'
import { validarLogin } from './validarLogin'

describe('validarLogin', () => {
  it('aceita credenciais com formato válido', () => {
    expect(validarLogin('participante@exemplo.com', '123456')).toEqual({})
  })

  it('informa os campos obrigatórios', () => {
    expect(validarLogin('', '')).toEqual({
      email: 'Informe seu e-mail.',
      senha: 'Informe sua senha.',
    })
  })

  it('rejeita e-mail inválido e senha curta', () => {
    expect(validarLogin('email-invalido', '123')).toEqual({
      email: 'Informe um e-mail válido.',
      senha: 'A senha deve possuir pelo menos 6 caracteres.',
    })
  })
})
