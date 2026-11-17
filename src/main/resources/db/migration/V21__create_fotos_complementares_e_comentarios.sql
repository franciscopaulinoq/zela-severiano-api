CREATE TABLE relato_fotos_complementares (
    id BIGSERIAL PRIMARY KEY,
    relato_id BIGINT NOT NULL REFERENCES relatos(id) ON DELETE CASCADE,
    autor_id UUID NOT NULL REFERENCES usuarios(id),
    url_foto VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_relato_fotos_relato ON relato_fotos_complementares(relato_id, criado_em, id);

CREATE TABLE comentarios_relato (
    id BIGSERIAL PRIMARY KEY,
    relato_id BIGINT NOT NULL REFERENCES relatos(id) ON DELETE CASCADE,
    autor_id UUID NOT NULL REFERENCES usuarios(id),
    texto TEXT NOT NULL CHECK (length(trim(texto)) > 0),
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_comentarios_relato_relato ON comentarios_relato(relato_id, criado_em, id);
