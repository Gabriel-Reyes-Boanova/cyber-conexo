-- Jogadores: cada nome (sem diferenciar maiúsculas/minúsculas) é uma pessoa, e as partidas ficam ligadas a ela.
CREATE TABLE player (
 id UUID PRIMARY KEY,
 name VARCHAR(80) NOT NULL,
 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX uq_player_name_ci ON player (lower(name));

-- Cria um jogador para cada nome que já existe nas partidas antigas (mantém o histórico e o ranking)
INSERT INTO player (id, name)
SELECT gen_random_uuid(), n.player_name
FROM (SELECT DISTINCT ON (lower(player_name)) player_name
      FROM game_session
      ORDER BY lower(player_name), started_at) n;

ALTER TABLE game_session ADD COLUMN player_id UUID REFERENCES player(id);
UPDATE game_session g SET player_id = p.id FROM player p WHERE lower(p.name) = lower(g.player_name);
ALTER TABLE game_session ALTER COLUMN player_id SET NOT NULL;
ALTER TABLE game_session DROP COLUMN player_name;

CREATE INDEX idx_game_session_player ON game_session (player_id, puzzle_id);
