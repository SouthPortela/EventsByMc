import type { FinalidadeMidia } from '../types/media'

const TIPOS_PERMITIDOS = ['image/jpeg', 'image/png', 'image/webp']

export function validarImagem(
  arquivo: Pick<File, 'type' | 'size'>,
  finalidade: FinalidadeMidia,
): string | null {
  if (!TIPOS_PERMITIDOS.includes(arquivo.type)) {
    return 'Selecione uma imagem JPEG, PNG ou WebP.'
  }

  const limiteMb = finalidade === 'BANNER_EVENTO' ? 5 : 2

  if (arquivo.size > limiteMb * 1024 * 1024) {
    return `A imagem deve ter no máximo ${limiteMb} MB.`
  }

  return null
}
