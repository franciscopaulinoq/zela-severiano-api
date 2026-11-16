CREATE TABLE auditoria_gestao (
    id BIGSERIAL PRIMARY KEY,
    gestor_id UUID NOT NULL REFERENCES usuarios(id),
    metodo VARCHAR(10) NOT NULL,
    caminho VARCHAR(500) NOT NULL,
    momento TIMESTAMP WITH TIME ZONE NOT NULL,
    status INTEGER NOT NULL
);

CREATE INDEX idx_auditoria_gestao_consulta ON auditoria_gestao(momento DESC, id DESC);
CREATE INDEX idx_auditoria_gestao_gestor_metodo ON auditoria_gestao(gestor_id, metodo);
