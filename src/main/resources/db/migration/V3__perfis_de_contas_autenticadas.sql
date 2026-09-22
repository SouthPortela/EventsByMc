-- VISITANTE representa apenas a navegação anônima; contas persistidas precisam
-- manter pelo menos um perfil autenticado. Preserve ORGANIZADOR/ADMINISTRADOR.
INSERT INTO usuario_perfis(usuario_id, perfil)
SELECT u.id, 'PARTICIPANTE'
FROM usuarios u
WHERE NOT EXISTS (
    SELECT 1 FROM usuario_perfis p
    WHERE p.usuario_id = u.id
      AND p.perfil IN ('PARTICIPANTE', 'ORGANIZADOR', 'ADMINISTRADOR')
)
ON CONFLICT DO NOTHING;

DELETE FROM usuario_perfis WHERE perfil = 'VISITANTE';

ALTER TABLE usuario_perfis
    ADD CONSTRAINT ck_usuario_perfis_sem_visitante CHECK (perfil <> 'VISITANTE');

INSERT INTO versoes_schema(versao, descricao) VALUES
    (3, 'Visitante anonimo; contas com perfil participante, organizador ou administrador');
