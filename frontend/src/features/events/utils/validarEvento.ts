import type { DadosCriacaoEvento } from '../services/eventoService'
import { ehCategoriaEvento, type CategoriaEvento } from '../types/categoria'
export type DadosFormularioEvento = Omit<DadosCriacaoEvento, 'categoria'> & {
  categoria: CategoriaEvento | ''
}
export type ErrosEvento = Partial<Record<keyof DadosFormularioEvento, string>>

export function validarEvento(dados: DadosFormularioEvento): ErrosEvento {
  const erros: ErrosEvento = {}
  const inicio = Date.parse(dados.dataInicio)
  const fim = Date.parse(dados.dataFim)
  if (dados.titulo.trim().length < 5 || dados.titulo.trim().length > 200)
    erros.titulo = 'Informe um título entre 5 e 200 caracteres.'
  if (dados.descricao.trim().length < 20 || dados.descricao.trim().length > 10000)
    erros.descricao = 'Descreva o evento usando entre 20 e 10.000 caracteres.'
  if (!dados.dataInicio || Number.isNaN(inicio))
    erros.dataInicio = 'Informe uma data de início válida.'
  if (!dados.dataFim || Number.isNaN(fim)) erros.dataFim = 'Informe uma data de término válida.'
  else if (!Number.isNaN(inicio) && fim <= inicio)
    erros.dataFim = 'O término deve ser posterior ao início.'
  if (!dados.local.trim() || dados.local.trim().length > 200)
    erros.local = 'Informe um local com até 200 caracteres.'
  if (!ehCategoriaEvento(dados.categoria)) erros.categoria = 'Selecione uma categoria válida.'
  return erros
}
