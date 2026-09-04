const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api'
const MENSAGEM_ERRO_GENERICA = 'Não foi possível concluir a operação.'

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
  }
}

interface ErroRespostaAPI {
  mensagem?: string
  instante?: string
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers: {
      Accept: 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    let mensagem = MENSAGEM_ERRO_GENERICA
    try {
      const corpo = (await response.json()) as ErroRespostaAPI
      if (corpo?.mensagem) mensagem = corpo.mensagem
    } catch {
      // corpo de erro vazio ou não-JSON: mantém a mensagem genérica
    }
    throw new ApiError(response.status, mensagem)
  }

  const texto = await response.text()
  if (!texto) return undefined as T

  try {
    return JSON.parse(texto) as T
  } catch {
    return texto as unknown as T
  }
}
