CREATE TABLE webhooks (
    id UUID PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    ultimo_evento_id BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE entregas_webhook (
    id UUID PRIMARY KEY,
    webhook_id UUID NOT NULL REFERENCES webhooks(id),
    historico_id BIGINT NOT NULL REFERENCES historico_status_relato(id),
    relato_id BIGINT NOT NULL REFERENCES relatos(id),
    status_anterior VARCHAR(20) NOT NULL,
    status_novo VARCHAR(20) NOT NULL,
    momento_mudanca TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    proxima_tentativa_em TIMESTAMP WITH TIME ZONE NOT NULL,
    entregue_em TIMESTAMP WITH TIME ZONE,
    ultimo_erro TEXT,
    CONSTRAINT uk_entrega_webhook_historico UNIQUE (webhook_id, historico_id)
);

CREATE INDEX idx_entregas_webhook_pendentes
    ON entregas_webhook (proxima_tentativa_em)
    WHERE entregue_em IS NULL;
