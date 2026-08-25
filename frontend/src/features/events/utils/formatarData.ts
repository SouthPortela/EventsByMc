export function formatarData(dataIso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'long',
    timeStyle: 'short',
  }).format(new Date(dataIso))
}
