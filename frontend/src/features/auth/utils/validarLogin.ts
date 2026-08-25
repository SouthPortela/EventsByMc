export interface ErrosLogin {
  email?: string
  senha?: string
}

export function validarLogin(email: string, senha: string): ErrosLogin {
  const erros: ErrosLogin = {}
  const emailNormalizado = email.trim()

  if (!emailNormalizado) {
    erros.email = 'Informe seu e-mail.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailNormalizado)) {
    erros.email = 'Informe um e-mail válido.'
  }

  if (!senha) {
    erros.senha = 'Informe sua senha.'
  } else if (senha.length < 6) {
    erros.senha = 'A senha deve possuir pelo menos 6 caracteres.'
  }

  return erros
}
