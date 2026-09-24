import { apiRequest } from '@/shared/services/httpClient'

export interface MensagemEvento {
  id: string
  eventoId: string
  usuarioId: string
  autor: string
  mensagem: string
  criadaEm: string
}

export function listarMensagens(eventoId: string): Promise<MensagemEvento[]> {
  return apiRequest(`/eventos/${encodeURIComponent(eventoId)}/mensagens`, { autenticada: true })
}

export function publicarMensagem(eventoId: string, mensagem: string): Promise<MensagemEvento> {
  return apiRequest(`/eventos/${encodeURIComponent(eventoId)}/mensagens`, {
    method: 'POST', autenticada: true,
    headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ mensagem }),
  })
}
