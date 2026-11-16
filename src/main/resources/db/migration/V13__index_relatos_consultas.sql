CREATE INDEX "idx_relatos_perfil_status"
    ON "relatos" ("perfil_id", "status");

CREATE INDEX "idx_relatos_perfil_criado_em_id"
    ON "relatos" ("perfil_id", "criado_em" DESC, "id" DESC);

CREATE INDEX "idx_relatos_categoria_criado_em_id"
    ON "relatos" ("categoria_id", "criado_em" DESC, "id" DESC);

CREATE INDEX "idx_relatos_status_criado_em_id"
    ON "relatos" ("status", "criado_em" DESC, "id" DESC);

CREATE INDEX "idx_relatos_criado_em_id"
    ON "relatos" ("criado_em" DESC, "id" DESC);
