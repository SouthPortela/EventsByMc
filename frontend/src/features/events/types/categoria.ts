export const categoriasEvento = [
  { codigo: 'TECNOLOGIA', nome: 'Tecnologia' },
  { codigo: 'CURSOS_E_WORKSHOPS', nome: 'Cursos e workshops' },
  { codigo: 'NEGOCIOS_E_CARREIRAS', nome: 'Negócios e carreiras' },
  { codigo: 'ACADEMICO', nome: 'Acadêmico' },
  { codigo: 'OUTROS', nome: 'Outros' },
] as const

export type CategoriaEvento = (typeof categoriasEvento)[number]['codigo']

export function ehCategoriaEvento(valor: string): valor is CategoriaEvento {
  return categoriasEvento.some((categoria) => categoria.codigo === valor)
}

export function nomeCategoria(valor: CategoriaEvento): string {
  return categoriasEvento.find((categoria) => categoria.codigo === valor)?.nome ?? 'Outros'
}
