CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE local (
  id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  endereco VARCHAR(100) NOT NULL,
  capacidade INT NOT NULL,
  event_id UUID,
    FOREIGN KEY (event_id) REFERENCES evento(id) ON DELETE CASCADE
);