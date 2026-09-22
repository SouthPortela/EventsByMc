export function normalizarCodigo(valor: string): string {
  return valor.replace(/[- ]/g, '').toUpperCase()
}
export function codigoValido(valor: string): boolean {
  return /^[0-9A-HJKMNP-TV-Z]{12}$/.test(normalizarCodigo(valor))
}
export function linkPresenca(base: string, codigo: string): string {
  if (!codigoValido(codigo)) throw new Error('Código inválido.')
  const url = new URL('presenca', base.endsWith('/') ? base : base + '/')
  if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password)
    throw new Error('Configure um endereço HTTP ou HTTPS válido para o webapp.')
  // O fragmento não integra a requisição HTTP ao servidor.
  url.hash = new URLSearchParams({ codigo: normalizarCodigo(codigo) }).toString()
  return url.toString()
}
