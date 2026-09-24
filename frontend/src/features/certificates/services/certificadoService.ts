import { apiDownload, apiRequest } from '@/shared/services/httpClient'

export interface Certificado {
  id: string
  eventoId: string
  eventoTitulo: string
  destinatario: string
  email: string | null
  tipo: 'PARTICIPANTE' | 'PALESTRANTE' | 'APRESENTADOR'
  emitidoEm: string
  enviadoEm: string | null
}

const base = (eventoId: string): string => `/eventos/${encodeURIComponent(eventoId)}/certificados`

export function baixarMeuCertificado(eventoId: string): Promise<void> {
  return apiDownload(`${base(eventoId)}/me.pdf`, 'certificado.pdf')
}
export function enviarMeuCertificado(eventoId: string): Promise<Certificado> {
  return apiRequest(`${base(eventoId)}/me/envio`, { method: 'POST', autenticada: true })
}
export function baixarDeclaracao(eventoId: string, pessoaId: string, papel: 'PALESTRANTE' | 'APRESENTADOR'): Promise<void> {
  return apiDownload(`${base(eventoId)}/pessoas/${encodeURIComponent(pessoaId)}/${papel}/pdf`, 'declaracao.pdf')
}
export function enviarDeclaracao(eventoId: string, pessoaId: string, papel: 'PALESTRANTE' | 'APRESENTADOR'): Promise<Certificado> {
  return apiRequest(`${base(eventoId)}/pessoas/${encodeURIComponent(pessoaId)}/${papel}/envio`, { method: 'POST', autenticada: true })
}
