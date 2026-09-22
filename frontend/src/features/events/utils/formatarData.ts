export function formatarData(dataIso: string | null | undefined): string {
  if (!dataIso || Number.isNaN(new Date(dataIso).getTime())) return 'Data a divulgar'
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'long',
    timeStyle: 'short',
  }).format(new Date(dataIso))
}
