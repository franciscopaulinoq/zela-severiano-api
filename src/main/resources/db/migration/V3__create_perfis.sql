CREATE TABLE "perfis" (
                          "id" uuid PRIMARY KEY,
                          "usuario_id" uuid NOT NULL,
                          "nome_completo" varchar(150) NOT NULL,
                          "email" varchar(255),
                          "criado_em" timestamp with time zone DEFAULT (now()),

                          CONSTRAINT uq_perfil_usuario_id UNIQUE ("usuario_id"),
                          CONSTRAINT fk_perfil_usuario
                              FOREIGN KEY ("usuario_id")
                                  REFERENCES "usuarios" ("id")
                                  ON DELETE CASCADE
);