import type { AtividadeAgenda, Participacao } from '../services/participacaoService'

export function proximaAtividade(
  atividades: AtividadeAgenda[],
  agora = Date.now(),
): AtividadeAgenda | null {
  return (
    atividades
      .filter(
        (atividade) => atividade.dataInicio && new Date(atividade.dataInicio).getTime() >= agora,
      )
      .sort((a, b) => new Date(a.dataInicio!).getTime() - new Date(b.dataInicio!).getTime())[0] ??
    null
  )
}

export function resumirParticipacao(dados: Participacao, agora = Date.now()) {
  return {
    inscricoesAtivas: dados.inscricoes.filter((inscricao) => inscricao.estado === 'ATIVA').length,
    atividadesFuturas: dados.atividades.filter(
      (atividade) => atividade.dataInicio && new Date(atividade.dataInicio).getTime() >= agora,
    ).length,
    presencasConfirmadas: dados.totalPresencas,
    proxima: proximaAtividade(dados.atividades, agora),
  }
}
