ALTER TABLE relatos ADD COLUMN identificador_externo VARCHAR(100);

CREATE UNIQUE INDEX uk_relatos_identificador_externo
    ON relatos (identificador_externo)
    WHERE identificador_externo IS NOT NULL;
