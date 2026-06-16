CREATE TABLE "usuarios" (
                            "id" uuid PRIMARY KEY,
                            "cpf" varchar(11) NOT NULL,
                            "senha_hash" varchar(255) NOT NULL,
                            "criado_em" timestamp with time zone DEFAULT (now()),

                            CONSTRAINT uq_usuario_cpf UNIQUE ("cpf")
);