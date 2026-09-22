const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL ?? '/api').replace(/\/$/, '')

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

interface ConfiguracaoHttp {
  obterToken: () => string | null
  aoNaoAutorizado: () => void
}

interface OpcoesRequisicao extends RequestInit {
  autenticada?: boolean
}

let configuracao: ConfiguracaoHttp = {
  obterToken: () => null,
  aoNaoAutorizado: () => {},
}

export function configurarHttpClient(nova: ConfiguracaoHttp): void {
  configuracao = nova
}

function mensagemDaApi(corpo: unknown): string {
  if (
    typeof corpo == 'object' &&
    corpo !== null &&
    'mensagem' in corpo &&
    typeof corpo.mensagem === 'string'
  ) {
    return corpo.mensagem
  }
  return 'Não foi possível realizar a operação.'
}

export async function apiRequest<T>(path: string, opcoes: OpcoesRequisicao = {}): Promise<T> {
  const { autenticada = false, ...init } = opcoes

  const headers = new Headers(init.headers)
  headers.set('Accept', 'application/json')

  const token = autenticada ? configuracao.obterToken() : null

  if (autenticada) {
    if (!token) {
      configuracao.aoNaoAutorizado()
      throw new ApiError(401, 'Entre na sua conta para continuar.')
    }

    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
  })

  if (!response.ok) {
    if (autenticada && response.status === 401 && token === configuracao.obterToken()) {
      configuracao.aoNaoAutorizado()
    }

    const corpo: unknown = await response.json().catch(() => null)

    throw new ApiError(response.status, mensagemDaApi(corpo))
  }

  if (response.status === 204) {
    return undefined as T
  }

  const tipo = response.headers.get('Content-Type')?.split(';')[0]?.trim()

  if (tipo === 'application/json' || tipo?.endsWith('+json')) {
    return (await response.json()) as T
  }

  if (tipo === 'text/plain') {
    return (await response.text()) as T
  }

  throw new ApiError(response.status, 'Resposta inesperada do servidor.')
}
