import { describe, expect, it } from 'vitest'
import { proximaAtividade, resumirParticipacao } from './resumoParticipacao'
import type { AtividadeAgenda, Participacao } from '../services/participacaoService'

const atividade = (id: string, dataInicio: string | null): AtividadeAgenda => ({
  id,
  eventoId: 'evento',
  eventoTitulo: 'Encontro',
  titulo: `Atividade ${id}`,
  dataInicio,
  dataFim: null,
  local: null,
  presencaRegistradaEm: null,
})

describe('resumo da participação', () => {
  it('conta inscrições ativas, atividades futuras e preserva o histórico de presenças', () => {
    const atividades = [
      atividade('tarde', '2026-10-10T15:00:00'),
      atividade('passada', '2026-10-09T15:00:00'),
      atividade('manha', '2026-10-10T13:00:00'),
    ]
    const dados: Participacao = {
      inscricoes: [
        {
          id: '1',
          eventoId: 'a',
          eventoTitulo: 'A',
          eventoInicio: null,
          eventoFim: null,
          eventoLocal: null,
          eventoEstado: 'PUBLICADO',
          estado: 'ATIVA',
          criadaEm: '',
        },
        {
          id: '2',
          eventoId: 'b',
          eventoTitulo: 'B',
          eventoInicio: null,
          eventoFim: null,
          eventoLocal: null,
          eventoEstado: 'ENCERRADO',
          estado: 'CANCELADA',
          criadaEm: '',
        },
      ],
      atividades,
      totalPresencas: 4,
    }
    expect(resumirParticipacao(dados, new Date('2026-10-10T12:00:00').getTime())).toMatchObject({
      inscricoesAtivas: 1,
      atividadesFuturas: 2,
      presencasConfirmadas: 4,
      proxima: { id: 'manha' },
    })
    expect(atividades.map((item) => item.id)).toEqual(['tarde', 'passada', 'manha'])
  })

  it('não inventa atividade futura quando só existem atividades passadas', () => {
    expect(
      proximaAtividade(
        [atividade('passada', '2026-10-09T10:00:00'), atividade('sem-data', null)],
        new Date('2026-10-10T12:00:00').getTime(),
      ),
    ).toBeNull()
  })
})
