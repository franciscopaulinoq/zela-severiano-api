ALTER TABLE "relatos"
    DROP CONSTRAINT "fk_relato_categoria";

ALTER TABLE "relatos"
    ADD CONSTRAINT "fk_relato_categoria"
        FOREIGN KEY ("categoria_id")
            REFERENCES "categorias" ("id")
            ON DELETE RESTRICT;
