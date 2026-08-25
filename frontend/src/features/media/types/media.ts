export type FinalidadeMidia = 'BANNER_EVENTO' | 'FOTO_PERFIL' | 'FOTO_PALESTRANTE'

export interface SolicitacaoUpload {
  finalidade: FinalidadeMidia
  nomeArquivo: string
  tipoConteudo: string
  tamanhoBytes: number
}

export interface SessaoUpload {
  uploadId: string
  uploadUrl: string
  metodo: 'PUT'
  cabecalhos: Record<string, string>
  expiraEm: string
}

export interface MidiaSalva {
  id: string
  url: string
  textoAlternativo: string
}
