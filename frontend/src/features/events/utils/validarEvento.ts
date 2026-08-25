export interface DadosFormularioEvento {
  titulo: string
  descricao: string
  dataInicio: string
  local: string
  controlaVagas: boolean
  capacidade: number | null
}

export type ErrosEvento = Partial<Record<keyof DadosFormularioEvento, string>>

export function validarEvento(dados: DadosFormularioEvento): ErrosEvento {
  const erros: ErrosEvento = {}

  if (dados.titulo.trim().length < 5)
    erros.titulo = 'Informe um título com pelo menos 5 caracteres.'
  if (dados.descricao.trim().length < 20)
    erros.descricao = 'Descreva o evento com pelo menos 20 caracteres.'
  if (!dados.dataInicio) erros.dataInicio = 'Informe a data de início.'
  if (!dados.local.trim()) erros.local = 'Informe o local ou modalidade.'
  if (dados.controlaVagas && (!dados.capacidade || dados.capacidade <= 0)) {
    erros.capacidade = 'Informe uma capacidade maior que zero.'
  }

  return erros
}
