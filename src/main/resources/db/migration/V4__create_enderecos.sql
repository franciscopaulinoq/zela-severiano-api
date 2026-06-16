CREATE TABLE "enderecos" (
                             "id" uuid PRIMARY KEY,
                             "perfil_id" uuid,
                             "cep" varchar(8) NOT NULL DEFAULT '59910000',
                             "logradouro" varchar(150) NOT NULL,
                             "numero" varchar(20) NOT NULL,
                             "complemento" varchar(100),
                             "bairro" varchar(100) NOT NULL,
                             "cidade" varchar(100) NOT NULL DEFAULT 'Doutor Severiano',
                             "uf" varchar(2) NOT NULL DEFAULT 'RN',
                             "criado_em" timestamp DEFAULT (now()),

                             CONSTRAINT uq_endereco_perfil_id UNIQUE ("perfil_id"),
                             CONSTRAINT fk_endereco_perfil
                                 FOREIGN KEY ("perfil_id")
                                     REFERENCES "perfis" ("id")
                                     ON DELETE CASCADE
);