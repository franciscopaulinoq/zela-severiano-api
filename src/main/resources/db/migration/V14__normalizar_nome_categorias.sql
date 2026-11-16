ALTER TABLE categorias ADD COLUMN nome_normalizado VARCHAR(100);

UPDATE categorias SET nome_normalizado = lower(btrim(nome));

ALTER TABLE categorias ALTER COLUMN nome_normalizado SET NOT NULL;
ALTER TABLE categorias ADD CONSTRAINT categorias_nome_normalizado_uk UNIQUE (nome_normalizado);
