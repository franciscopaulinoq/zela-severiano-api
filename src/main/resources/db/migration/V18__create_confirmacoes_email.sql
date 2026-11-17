ALTER TABLE perfis ADD COLUMN email_pendente VARCHAR(255);

CREATE TABLE confirmacoes_email (
    id UUID PRIMARY KEY,
    perfil_id UUID NOT NULL REFERENCES perfis(id),
    email_solicitado VARCHAR(255) NOT NULL,
    codigo_hash VARCHAR(64) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    consumido_em TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_confirmacoes_email_validacao
    ON confirmacoes_email (perfil_id, codigo_hash)
    WHERE consumido_em IS NULL;
