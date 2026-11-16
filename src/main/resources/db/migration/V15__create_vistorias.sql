CREATE TABLE vistorias (
    id BIGSERIAL PRIMARY KEY,
    relato_id BIGINT NOT NULL REFERENCES relatos (id) ON DELETE RESTRICT,
    gestor_id UUID NOT NULL REFERENCES usuarios (id) ON DELETE RESTRICT,
    inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    fim TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT vistoria_intervalo_valido CHECK (inicio < fim),
    CONSTRAINT vistoria_status_valido CHECK (status IN ('ATIVA', 'CANCELADA'))
);

CREATE INDEX idx_vistorias_relato ON vistorias (relato_id, inicio);
CREATE INDEX idx_vistorias_gestor_agenda ON vistorias (gestor_id, inicio, fim) WHERE status = 'ATIVA';
