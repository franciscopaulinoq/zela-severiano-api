CREATE TABLE avisos (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    conteudo TEXT NOT NULL,
    criado_em TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE aviso_leituras (
    id BIGSERIAL PRIMARY KEY,
    aviso_id BIGINT NOT NULL REFERENCES avisos(id),
    perfil_id UUID NOT NULL REFERENCES perfis(id),
    lida_em TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_aviso_leitura_perfil UNIQUE (aviso_id, perfil_id)
);
CREATE INDEX idx_aviso_leituras_perfil ON aviso_leituras(perfil_id, lida_em);
