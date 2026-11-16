CREATE TABLE recuperacoes_senha (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    codigo VARCHAR(64) NOT NULL,
    expira_em TIMESTAMP WITH TIME ZONE NOT NULL,
    invalidado_em TIMESTAMP WITH TIME ZONE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX ux_recuperacoes_senha_ativa
    ON recuperacoes_senha (usuario_id)
    WHERE invalidado_em IS NULL;

CREATE INDEX ix_recuperacoes_senha_confirmacao
    ON recuperacoes_senha (usuario_id, codigo)
    WHERE invalidado_em IS NULL;
