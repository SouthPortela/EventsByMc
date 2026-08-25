export interface DadosCadastro {
  nome: string
  email: string
  senha: string
  confirmacaoSenha: string
  aceitouTermos: boolean
}

export type ErrosCadastro = Partial<Record<keyof DadosCadastro, string>>

export function validarCadastro(dados: DadosCadastro): ErrosCadastro {
  const erros: ErrosCadastro = {}

  if (dados.nome.trim().length < 3) erros.nome = 'Informe seu nome completo.'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(dados.email.trim()))
    erros.email = 'Informe um e-mail válido.'
  if (dados.senha.length < 8) erros.senha = 'Use pelo menos 8 caracteres.'
  if (dados.confirmacaoSenha !== dados.senha) erros.confirmacaoSenha = 'As senhas não conferem.'
  if (!dados.aceitouTermos) erros.aceitouTermos = 'Você precisa aceitar os termos para continuar.'

  return erros
}
