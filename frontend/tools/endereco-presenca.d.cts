import type { NetworkInterfaceInfo } from 'node:os'

export function escolherEndereco(
  interfaces: Record<string, NetworkInterfaceInfo[] | undefined>,
  enderecoRota: string | null,
): string | null
export function detectarEnderecoRede(): Promise<string | null>
