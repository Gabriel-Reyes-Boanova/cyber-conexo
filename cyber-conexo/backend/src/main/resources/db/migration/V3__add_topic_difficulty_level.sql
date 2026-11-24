-- Novos campos para organizar o conteúdo (só Linux e cibersegurança)
ALTER TABLE puzzle ADD COLUMN topic VARCHAR(20) NOT NULL DEFAULT 'LINUX';
ALTER TABLE puzzle ADD COLUMN difficulty VARCHAR(20) NOT NULL DEFAULT 'INICIANTE';
ALTER TABLE puzzle ADD CONSTRAINT ck_puzzle_topic CHECK (topic IN ('LINUX', 'SEGURANCA', 'MISTO'));
ALTER TABLE puzzle ADD CONSTRAINT ck_puzzle_difficulty CHECK (difficulty IN ('INICIANTE', 'INTERMEDIARIO', 'AVANCADO'));

-- Nível do grupo (1 = mais fácil ... 4 = mais difícil), usado para colorir no front
ALTER TABLE word_group ADD COLUMN level INTEGER NOT NULL DEFAULT 1;
ALTER TABLE word_group ADD CONSTRAINT ck_word_group_level CHECK (level BETWEEN 1 AND 4);

-- Duração da partida (gravada ao concluir) para o ranking
ALTER TABLE game_session ADD COLUMN duration_seconds INTEGER;
ALTER TABLE game_session ADD CONSTRAINT ck_game_session_duration CHECK (duration_seconds IS NULL OR duration_seconds >= 0);

CREATE INDEX idx_game_session_ranking
    ON game_session (puzzle_id, completed, score DESC, mistakes, duration_seconds);
